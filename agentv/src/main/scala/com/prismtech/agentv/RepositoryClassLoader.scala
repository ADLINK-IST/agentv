package com.prismtech.agentv

import io.nuvo.runtime.Config.Logger

/**
  * Class loader that loads classes dynamically from the agent repository.
  *
  * @author <a href="mailto:angelo.corsaro@prismtech.com">Angelo Corsaro</a>
  * @version 0.1.0
  */
class RepositoryClassLoader(val parent: ClassLoader, path: String) extends ClassLoader(parent) {
  val logger = new Logger("RepositoryClassLoader")

  val repository = new Repository(path)

  override def loadClass(n: String):  Class[_] = {
    logger.debug(s"Trying load class for $n")
    var cls: Class[_] = null
    if (n.contains("com.prismtech")) {
      cls = this.loadClass(n, false)
    } else {
      cls = super.loadClass(n)
    }

    if (cls == null)
      cls = this.loadClass(n, false)
    cls
  }

  override def loadClass(n: String, r: Boolean): Class[_] = {
    logger.debug(s"Trying load class for $n")
    var cls: Class[_] = this.findLoadedClass(n)
    if (cls == null && n.contains("com.prismtech")) {
      cls = this.findClass(n)
    } else {
      try {
        cls = super.loadClass(n, r)
      } catch {
        case e: ClassNotFoundException => {
          this.findClass(n)
        }
      }
    }
    cls
  }

  override def findClass(n: String ): Class[_] = {
    logger.debug(s"Finding Class $n through parent ClassLoader")
    val resolve = () => {
      logger.debug(s"Now trying to find class $n in repository")
      val scls = for {
        bs <- repository.resolveClass(n)
      } yield this.defineClass(n, bs, 0, bs.length)

      scls match {
        case Some(cls) => cls
        case None => throw new ClassNotFoundException(s"Could not find class $n")
      }
    }
    if (!n.contains("com.prismtech")) {
      try {
        super.findClass(n)
      } catch {
        case e: ClassNotFoundException => {
          resolve()
        }
      }
    } else {
      resolve()
    }
  }
}
