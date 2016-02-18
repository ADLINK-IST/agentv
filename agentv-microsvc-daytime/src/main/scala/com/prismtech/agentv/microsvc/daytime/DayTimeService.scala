package com.prismtech.agentv.microsvc.daytime

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.prismtech.agentv.{Duration, PeriodicMicrosvc}
import com.prismtech.agentv.microsvc.daytime.types.DayTime
import org.omg.dds.pub.Publisher
import org.omg.dds.sub.Subscriber

import dds._
import dds.prelude._
import io.nuvo.runtime.Config.Logger

/**
  * com.prismtech.agentv.capsule.daytime.DayTimeService
  * Created by veda on 09/02/16.
  */
class DayTimeService extends PeriodicMicrosvc {
  val logger = new Logger("DayTime")
  val topicName = "DayTime"
  val running = new AtomicBoolean(false)
  var duration: Option[Duration] = None
  var dayTime: Option[SoftState[DayTime]] = None

  override def init(rpub: Publisher, rsub: Subscriber, args: Array[String]): Unit = {
    implicit val dp = rpub.getParent
    implicit val (pub, sub)= (rpub, rsub)
    implicit val pf = pub.getEnvironment.getSPI.getPolicyFactory
    dayTime = Some(SoftState[DayTime](topicName))
    duration = Some(new Duration(args(0).toLong, TimeUnit.MILLISECONDS))
  }

  override def stop(): Unit = {
    running.set(false)
  }

  override def start(): Boolean = {
    logger.info("Starting DayTime microsvc")
    if (running.getAndSet(true))
      throw new IllegalStateException("Cannot start a running microservice")
    false
  }

  override def schedule(): Unit = {
    if (running.get()) {

      val currentTime = new DayTime(0, 0)
      currentTime.currentTimeMillis = System.currentTimeMillis()
      currentTime.nanoTime = System.nanoTime()
      dayTime.foreach(_.writer.write(currentTime))
    } else throw new IllegalStateException("Cannot schedule a stopped service")
  }

  override def getPeriod: Duration = {
    duration.get
  }

  override def close(): Unit = {
    dayTime.foreach(dt => {
      dt.reader.close()
      dt.writer.close()
      dayTime = None
    })
  }
}
