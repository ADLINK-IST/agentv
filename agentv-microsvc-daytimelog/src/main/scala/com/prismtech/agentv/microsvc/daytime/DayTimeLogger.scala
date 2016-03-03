package com.prismtech.agentv.microsvc.daytime

import java.util.concurrent.atomic.AtomicBoolean

import com.prismtech.agentv.microsvc.daytime.types.DayTime
import com.prismtech.agentv.prelude._

import scala.collection.JavaConversions._

import com.prismtech.agentv.Microsvc
import org.omg.dds.pub.Publisher
import org.omg.dds.sub.Subscriber

import dds._
import dds.prelude._
import dds.config.DefaultEntities.{defaultDomainParticipant, defaultPolicyFactory}

import io.nuvo.runtime.Config.Logger

/**
  * com.prismtech.agentv.capsule.daytime.DayTimeLogger
  * Created by veda on 09/02/16.
  */
class DayTimeLogger extends Microsvc {
  val logger = new Logger("DayTimeLogger")
  val topicName = "DayTime"
  val running = new AtomicBoolean(false)
  var listenerId: Option[Int] = None
  var dayTime: Option[SoftState[DayTime]] = None

  override def init(rpub: Publisher, rsub: Subscriber, args: Array[String]): Unit = {
    val partition = NodePartition + "*"
    val scope = Scope(partition )
    implicit val (pub, sub) = scope ()
    dayTime = Some(SoftState[DayTime](topicName))
  }

  override def stop(): Unit = {
    logger.log("Stopping DayTimeLogger - running status = " + running.get)
    if (running.getAndSet(false))
      for {
        id <- listenerId
        dt <- dayTime
      } yield {
        dt.reader.deaf(id)
        dt.reader.setListener(null)
        dt.reader.close()
      }
    logger.debug("DayTimeLogger Running Sta" + running.get)
  }


  def show(t: DayTime): String = "(" + t.currentTimeMillis + ", " + t.nanoTime + ")"

  override def start(): Boolean = {
    println("Starting DayTime Logger")
    running.set(true)

    listenerId = dayTime.map(dt => dt.reader.listen {
      case DataAvailable(_) =>
        dt.reader.select()
            .dataState(DataState.newData).read().map(_.getData).foreach(t => logger.info(show(t)))
    })
    false
  }

  override def close(): Unit = {
    dayTime.foreach(dt =>{
      dt.reader.close()
      dt.writer.close()
    })
    dayTime = None
  }



}
