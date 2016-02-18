package com.prismtech.agentv

import io.nuvo.concurrent.Worker


object Launcher {
  def startMainClass(mainCls: Class[_], args: Array[String]): Unit = {
    mainCls.getMethods.find(_.getName() == "main").map {
      m => m.invoke(null, args)
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length > 1) {
      val path = args(1)
      val cl = new RepositoryClassLoader(classOf[Runtime].getClassLoader, path)
      Thread.sleep(5000)
      val cls = cl.loadClass(args(0))

      System.setProperty("dds.runtime.repository.path", path)

      Worker.run { () =>
        Thread.currentThread().setContextClassLoader(cl)
        startMainClass(cls, args.drop(1))
      }
    }
    else {
      println("USAGE:\n\tLauncher <classname> <repo> <args>")
    }
  }

}
