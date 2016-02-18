package com.prismtech.agentv.runtime

import java.util.concurrent.atomic.AtomicBoolean
import io.nuvo.concurrent.Implicits._


import com.prismtech.agentv.Microsvc

class MicrosvcWorker (svc: Microsvc) {
  val running = new AtomicBoolean(false)
  var executor: Option[Thread] = None

  def start(args: Array[String]): Unit = {
    if (!running.getAndSet(true)) {
      executor = Some(new Thread(() => svc.start()))
      executor.foreach(_.start)
    }
  }

  def stop(): Unit = {
    if (running.getAndSet(false)) {
      try {
        svc.stop()
        executor.foreach(_.interrupt())
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }
}
