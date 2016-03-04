package com.prismtech.agentv.commander

import java.util.concurrent.atomic.AtomicReference

import dds._
import dds.prelude._
import dds.config.DefaultEntities.{defaultDomainParticipant,defaultPolicyFactory}
import com.prismtech.agentv.prelude._
import com.prismtech.agentv.core.types._
import org.omg.dds.domain.DomainParticipantFactory
import scala.collection.JavaConversions._
import io.nuvo.concurrent.synchronizers._
import io.nuvo.runtime.Config.Logger

class AgentContext(nodeId: String) {
  val microsvcPartition = NodePartition + PartitionSeparator + nodeId
  
  val microSvcScope = Scope(microsvcPartition)
  implicit val (pub, sub) = microSvcScope ()

  val appAction = new Event[MicrosvcAction](MicrosvcActionTopicName, Durability.TransientLocal)
  val repoAction = new Event[RepoAction](RepoActionTopicName, Durability.TransientLocal)


}

object Commander {

  val logger = new Logger("Commander")
  var nodeContextMap      = Map[String, AgentContext]()
  var microSvcRepoEntries = List[(String, String)]()
  var runningMicrosvcsList    = List[RunningMicrosvc]()
  var runningMicrosvcHashMap = Map[String, RunningMicrosvc]()
  var nodeList = List[String]()

  def setupAgentContext(nodeId: String): AgentContext = {
    synchronized {
      nodeContextMap.get(nodeId) match {
        case Some(ctx) => ctx
        case None =>
          val ctx: AgentContext = new AgentContext(nodeId)
          nodeContextMap = nodeContextMap + (nodeId -> ctx)
          ctx
      }
    }
  }

  def getAgentContext(nodeId: String): Option[AgentContext] = nodeContextMap.get(nodeId)

  def startMicrosvc(nodeId: String, microsvc: String, args: Array[String]): Boolean =
    startMicrosvc(nodeId, microsvc, java.util.UUID.randomUUID().toString, args)
  
  def startMicrosvc(nodeId: String, microsvc: String, microsvcId: String, args: Array[String]): Boolean = {
    logger.debug(s"Starting Microsvc with args: $args")
    var ctx = getAgentContext(nodeId)
    val c = new MicrosvcActionCase()
    if (!runningMicrosvcHashMap.contains(microsvcId)) {
      c.start(new StartMicrosvc(microsvc, microsvcId, args))
      val action = new MicrosvcAction(c)
      ctx.foreach(_.appAction.writer.write(action))
      true}
    else  {
      logger.warning(s"Trying to start a new microsvc with an existing process identifier ($microsvcId)")
      false
    }
  }

  def stopMicrosvc(hash: String): Unit = {
    logger.debug(s"Stopping Microsvc: $hash")
    runningMicrosvcHashMap.foreach(e => println(e._1))
    runningMicrosvcHashMap.get(hash).foreach(r => {
      stopMicrosvc(r.nodeId, r.microsvcId)
    })
  }

  def stopMicrosvc(nodeId: String, microSvcId: String): Unit = {
    logger.debug(s"Stopping microsvc ($nodeId/$microSvcId)")
    val ctx = getAgentContext(nodeId)
    val c = new MicrosvcActionCase()
    c.stop(new StopMicrosvc(microSvcId))
    val action = new MicrosvcAction(c)
    ctx.foreach(_.appAction.writer.write(action))

  }

  def deployPackage(nodeId: String, pkg: String, payload: Array[Byte]): Unit = {
    logger.debug(s"Deploying package $pkg on $nodeId")
    var ctx = getAgentContext(nodeId)
    val c = new RepoActionCase()
    c.install(new InstallMicrosvc(pkg, payload ))
    val action = new RepoAction(c)
    ctx.foreach(_.repoAction.writer.write(action))
  }

  def run(window: CommanderWindow) {

    val nodeScope = Scope(NodePartition)
    implicit val (pub, sub) = nodeScope()

    val nodeInfo = SoftState[NodeInfo](NodeInfoTopicName)
    nodeInfo.reader.listen {
      case DataAvailable(_) =>
        val nodes =
          nodeInfo.reader.select().dataState(DataState.allData).read().map(_.getData).toList

        nodes.foreach(a => setupAgentContext(a.uuid))
        val nodeNames = nodes.map(_.uuid)

        val hasNewMembers = !nodeNames.forall(this.nodeList.contains(_))
        if (hasNewMembers) {
          window.setNodeList(nodeNames.toArray)
          this.nodeList = nodeNames
        }
    }

    {
      val allNodesScope = Scope(NodePartition + PartitionSeparator + "*")
      implicit val (pub, sub) = allNodesScope()

      val repoEntry = HardState[MicrosvcRepoEntry](MicroSvcRepositoryEntryTopicName, Durability.TransientLocal)
      repoEntry.reader.listen {
        case DataAvailable(_) =>
          val pkgs =
            repoEntry.reader.select().dataState(DataState.allData).read().filter(_.getData != null).map(_.getData).toList

          microSvcRepoEntries = pkgs.map(e => (e.nodeId, e.microsvc))
          microSvcRepoEntries.foreach(e => logger.info(e.toString()))
          window.updateRepoEntries();
      }

      val errorLog = HardState[NodeError](NodeErrorTopicName, Durability.TransientLocal)
      errorLog.reader.listen {
        case DataAvailable(_) =>
          var elogs = errorLog.reader.select().dataState(DataState.newData).read().filter(_.getData != null).map(_.getData).toList
          elogs.foreach(log => window.addErrorEntry(log.nodeId + "(" + log.errno+ ": " + log.msg))

      }

      val runningMicrosvcs = HardState[RunningMicrosvc](RunningMicrosvcTopicName, Durability.TransientLocal)
      runningMicrosvcs.reader.listen {
        case DataAvailable(_) =>
          runningMicrosvcsList = runningMicrosvcs.reader.select()
            .dataState(DataState.allData)
            .read().filter(_.getData != null).map(_.getData).toList
          runningMicrosvcsList.foreach(r => logger.info(r.microsvcId + "(" + r.nodeId + ")"))
          runningMicrosvcHashMap = runningMicrosvcsList.map(s => (runningMicrosvcHash(s), s)).toMap

          window.updateRunningList();
      }
    }
  }

  def getInstalledMicrosvcs(nodeId: String) : Array[String] = {
    microSvcRepoEntries.filter(_._1 == nodeId).map(e => e._2).toArray
  }

  def runningMicrosvcHash(s: RunningMicrosvc): String = s.microsvc + "(" + s.microsvcId.hashCode + ")"

  def getRunningMicrosvcs(nodeId: String): Array[String] = {
    runningMicrosvcsList.filter(s => s.nodeId == nodeId).map(s => runningMicrosvcHash(s)).toArray
  }
}
