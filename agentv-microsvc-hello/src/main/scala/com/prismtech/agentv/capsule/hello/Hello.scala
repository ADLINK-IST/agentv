package com.prismtech.agentv.capsule.hello

import org.omg.dds.sub.Subscriber;
import org.omg.dds.pub.Publisher;

class Hello extends com.prismtech.agentv.Microsvc {
  var msg: Option[String] = None

  override def init(pub: Publisher, sub: Subscriber, args: Array[String]): Unit = {
    msg =  if (args.length > 0) Some(args.fold(" ")(_ + " " + _)) else Some("Hello from an AgentV Microsvc")

  }

  override def start(): Boolean =  {
    msg.foreach(println)
    Thread.sleep(10000)
    true
  }

  override def stop() {

  }

  override def close(): Unit = {

  }
}
