package com.prismtech.agentv.runtime

import java.io.File
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Executors, ScheduledThreadPoolExecutor}

import com.prismtech.agentv.{Microsvc, PeriodicMicrosvc}
import com.prismtech.agentv.prelude._
import com.prismtech.agentv.core.types._
import org.omg.dds.core.ServiceEnvironment
import org.omg.dds.domain.DomainParticipantFactory

import io.nuvo.runtime.Config.Logger
import io.nuvo.concurrent.synchronizers._
import io.nuvo.concurrent.Worker
import io.nuvo.concurrent.Implicits._

import dds._
import dds.prelude._
import dds.config.DefaultEntities.{defaultDomainParticipant, defaultPolicyFactory}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object DDSRuntime {

  val runtimeSchedulerThreadNum= 1

  def main(args: Array[String]): Unit = {
    val rt = new DDSRuntime(args(0), args(1), args(2), args(3), args(4).toInt)

    rt.start()
  }
}

class DDSRuntime(baseDir: String, dds: String, val uuid: String, val info: String, val domain: Int = 0) {

  initRuntimeProp(DDS_RUNTIME_NODE_UUID, uuid)
  setRuntimeProp(DDS_RUNTIME_NODE_MICROSVC_PARTITION, NodePartition + PartitionSeparator + uuid)
  setRuntimeProp(ServiceEnvironment.IMPLEMENTATION_CLASS_NAME_PROPERTY,  dds)

  val scheduler = Executors.newScheduledThreadPool(DDSRuntime.runtimeSchedulerThreadNum)

  val microsvcPartition = NodePartition +  PartitionSeparator + uuid
  val logger = new Logger("DDSRuntime")
  val nodeAdvertisementPeriod = 1000 // 1 sec

  val microsvcMapRef = new AtomicReference[Map[String, Microsvc]](Map[String, Microsvc]())

  val nodeInfo = {
    val nodeScope = Scope(NodePartition)
    implicit val (pub, sub) = nodeScope()
    SoftState[NodeInfo](NodeInfoTopicName)
  }

  val microSvcScope = Scope(microsvcPartition)
  implicit val (cpub, csub) = microSvcScope()

  val nodeError = HardState[NodeError](NodeErrorTopicName, Durability.TransientLocal)
  val microsvcAction = Event[MicrosvcAction](MicrosvcActionTopicName, Durability.TransientLocal)
  val repoAction = Event[RepoAction](RepoActionTopicName, Durability.TransientLocal)

  val runningApps = HardState[RunningMicrosvc](RunningMicrosvcTopicName, Durability.TransientLocal)
    val cl = this.getClass.getClassLoader



  val repoEntry = HardState[MicrosvcRepoEntry](MicroSvcRepositoryEntryTopicName, Durability.TransientLocal)
  val notifier = (microsvc: String) => {
    repoEntry.writer.write(new MicrosvcRepoEntry(uuid, microsvc))
  }

  val microsvcRepo = new MicrosvcRepository(baseDir, notifier)
  var microSvcListenerId  = -1
  var repoListenerId      = -1

  val ainfo = new NodeInfo(uuid, info)


  def start(): Unit = {

    logger.info("Starting")
    Worker.runLoop(() => {
        nodeInfo.writer.write(ainfo)
        Thread.sleep(nodeAdvertisementPeriod)
    })

    import scala.collection.JavaConversions._

    microSvcListenerId = microsvcAction.reader.listen {
      case DataAvailable(_) =>
        logger.debug("Received Microsvc action")
        microsvcAction.reader.select()
          .dataState(DataState.allSamples())
          .take()
            .filter(s => s.getData != null).map(_.getData).foreach { cmd =>
              cmd.action.discriminator().value() match {
                case MicrosvcActionKind._A_START      =>  startMicrosvc(cmd.action.start())
                case MicrosvcActionKind._A_STOP       =>  stopMicrosvc(cmd.action.stop())
              }
            }
    }

    repoListenerId = repoAction.reader.listen {
      case DataAvailable(_) =>
        repoAction.reader.select()
          .dataState(DataState.allSamples())
          .take()
          .filter(s => s.getData != null).map(_.getData)
          .foreach { cmd =>
            cmd.action.discriminator().value() match  {
              case RepoActionKind._A_INSTALL =>
                val action = cmd.action.install()
                val success = microsvcRepo.install(action.microsvc, action.payload)
                if (!success) {
                  nodeError.writer.write(new NodeError(uuid, -1, "Unable to deploy" + action.microsvc))
                }
              case RepoActionKind._A_UPGRADE => logger.warning("Microservice upgrade not implemented yet")
              case RepoActionKind._A_REMOVE  => logger.warning("Microservice remove not implemented yet")
            }
          }
    }
    logger.info("Node Runtime Started")
  }

  def startMicrosvc(action: StartMicrosvc): Unit =  {
    val entryPoint = microsvcRepo.microsvcs.get(action.microsvc)

    entryPoint match {
      case Some(cname) =>
        try {
          val cls = cl.loadClass(cname)
          val microsvc = cls.newInstance().asInstanceOf[Microsvc]
          microsvc.init(cpub, csub, action.args)
          microsvc match {
            case pms: PeriodicMicrosvc =>
              val period = pms.getPeriod.getDuration
              logger.info("Starting PeriodicMicrosvc " + action.microsvc + " with period " + period)
              pms.start()
              val unit = pms.getPeriod.getTimeUnit

              val r: RichRunnable[Unit]= () => {
                pms.schedule()
              }
              try {
                val f = scheduler.scheduleAtFixedRate(r, 0, period, unit)
                compareAndSet(microsvcMapRef) { map => map + (action.microsvcId -> microsvc) }
                val wid = action.microsvcId
                val rmicrosvc = new RunningMicrosvc(uuid, wid, action.microsvc)
                logger.debug("Sending Update on Running Apps for : " + action.microsvc)
                runningApps.writer.write(rmicrosvc)
              } catch {
                case t: Throwable => nodeError.writer.write(new NodeError(uuid, -1, t.toString))
              }

            case ms: Microsvc =>
              logger.info("Starting Microsv: " + action.microsvc)
              val f = Future {
                ms.start()
              }
              compareAndSet(microsvcMapRef) { map => map + (action.microsvcId -> microsvc) }
              val wid = action.microsvcId
              val rmicrosvc = new RunningMicrosvc(uuid, wid, action.microsvc)
              logger.debug("Sending Update on Running Apps for : " + action.microsvc)
              runningApps.writer.write(rmicrosvc)
              f.onSuccess{
                case true =>
                  logger.info("Microsvc completed: " + action.microsvc + "/" + action.microsvcId)
                  removeMicrosvc(action.microsvcId)
                  ms.stop()
                  ms.close()
                case false => // the microsvc is continuing on another thread.
              }
              f.onFailure{
                case t: Throwable =>
                  compareAndSet(microsvcMapRef) { map => map - action.microsvcId  }
                  nodeError.writer.write(new NodeError(uuid, -1, t.toString))
              }
          }



//  == Pre Future Code
//          val microsvc = cls.newInstance().asInstanceOf[Microsvc]
//          microsvc.init(cpub, csub)
//
//
//          val worker: MicrosvcWorker = new MicrosvcWorker(microsvc)
//
//          val wid = action.microsvcId
//          val rmicrosvc = new RunningMicrosvc(uuid, wid, action.microsvc)
//          logger.debug("Sending Update on Running Apps for : " + action.microsvc)
//          runningApps.writer.write(rmicrosvc)
//          worker.start(action.args)
//          compareAndSet(workerMapRef) { map => map + (action.microsvcId -> worker) }

        } catch {
          case e: Exception =>
            compareAndSet(microsvcMapRef) { map => map - action.microsvcId  }
            nodeError.writer.write(new NodeError(uuid, -1, e.toString))
        }
//
//        val worker2: Worker = Worker.run { () =>
//          try {
//            val cls = cl.loadClass(cname)
//            val capsule = cls.newInstance().asInstanceOf[Microsvc]
//            capsule.init(cpub, csub)
//            val wid = action.microsvcId
//            val rmicrosvc = new RunningMicrosvc(uuid, wid, action.microsvc)
//            logger.debug("Sending Update on Running Apps for : " + action.microsvc)
//            runningApps.writer.write(rmicrosvc)
//            capsule.start(action.args)
//          } catch {
//            case e: Exception =>
//              compareAndSet(workerMapRef) { map => map - action.microsvcId  }
//              nodeError.writer.write(new NodeError(uuid, -1, e.toString))
//          }
//        }


      case None =>
        val error = "Unable to start: " + action.microsvc + ", the microservice is not available."
        logger.warning(error)
        nodeError.writer.write(new NodeError(uuid, -1, error))
    }
  }

  private def removeMicrosvc(microsvcId: String): Unit = {
    var res = for {
      worker <- microsvcMapRef.get.get(microsvcId)
      instance <- Some(new RunningMicrosvc(uuid, microsvcId, ""))
      handle <- Some(runningApps.writer.lookupInstance(instance))
    } yield {
        runningApps.writer.dispose(handle)
        compareAndSet(microsvcMapRef) { map => map - microsvcId }
    }
  }

  def stopMicrosvc(action: StopMicrosvc): Unit =  {
    logger.info("Stopping Microsvc: " + action.microsvcId)

    var res = for {
      microsvc <- microsvcMapRef.get.get(action.microsvcId)
      instance <- Some(new RunningMicrosvc(uuid, action.microsvcId, ""))
      handle <- Some(runningApps.writer.lookupInstance(instance))
    } yield {
      try {
        microsvc.stop()
        microsvc.close()
        logger.info("Microservice " + action.microsvcId + " successfully stopped")
      } catch {
        case e: Exception =>
          logger.info("Microservice stopped with exception" + e)
      } finally {
        runningApps.writer.dispose(handle)
        compareAndSet(microsvcMapRef) { map => map - action.microsvcId }
      }


    }
    res match {
      case None =>
        nodeError.writer.write(new NodeError(uuid, -1, "Unable to stop " + action.microsvcId + " as the instance could not be found"))
      case _ =>
    }

  }

  def stop(): Unit = {
    logger.info("Stopping")
    val handle = nodeInfo.writer.lookupInstance(ainfo)
    nodeInfo.writer.dispose(handle)

    if (this.microSvcListenerId != -1) microsvcAction.reader.deaf(this.microSvcListenerId)
    if (this.repoListenerId != -1) repoAction.reader.deaf(this.repoListenerId)
    logger.info("Stopped")
  }
}
