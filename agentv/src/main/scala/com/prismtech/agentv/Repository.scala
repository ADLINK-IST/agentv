package com.prismtech.agentv

import java.io.File
import java.util.concurrent.atomic.AtomicReference
import java.util.jar.JarFile
import scala.collection.JavaConversions._
import io.nuvo.runtime.Config.Logger
import io.nuvo.concurrent.Worker
import io.nuvo.concurrent.synchronizers._

import java.nio.file.{StandardWatchEventKinds, WatchKey, Paths, FileSystems}

object Repository {
  val self = new AtomicReference[Option[Repository]](None)
  def apply(path: String ) =  {

    val repo = new Repository(path)
    compareAndSet(self) { r => Some(repo) }
  }

  def instance = self.get()
}
/**
  * Creates a pacakge repository for the current agent.
  *
  * @param path The path at which the repository will be created.
  */
class Repository (val path: String) {

  val JAR_EXT = ".jar"
  val logger = new Logger("Repository")
  val dir = new File(path)


  private var jars = new AtomicReference(List[JarFile]())

  val watchService = FileSystems.getDefault.newWatchService()

  val watchKey = Paths.get(path).register(watchService,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_DELETE
  )

  val dirWatcher = Worker.runLoop { () =>
    val njars = createJarFiles()
    val ojars = compareAndSet(jars) { js => njars}
    ojars.foreach(_.close())
    watchService.take()
    logger.info("The content of the package directory just changed")
  }

  def packages = jars.get().map(_.getName())

  def createJarFiles(): List[JarFile] = {

    dir.list().filter(_.endsWith(JAR_EXT)).map(n => {
      val jarFile = path + File.separator + n
      logger.info(s"Opening Jar = $jarFile")
      new JarFile(jarFile)
    }).toList
  }

  def microsvcs = jars.get()


  def resolveClass(s: String): Option[Array[Byte]] = {
    val n = s.replace(".", File.separator) + ".class"
    jars.get().map(jar => (jar, jar.getEntry(n))).filter(p => {
      logger.debug(p.toString())
      p._2 != null
    }) match {
      case List() => {
        logger.warning(s"Class $n Not found")
        None
      }
      case x::xs => {
        val jar = x._1
        val zip = x._2
        val is = jar.getInputStream(zip)
        val len = is.available()
        logger.debug(s"Class File length = $len")
        val buf = new Array[Byte](len)
        var offset = 0
        var rb = 0
        do {
          val l = len - rb
          rb += is.read(buf, offset, l)
          offset = rb
        } while (rb != len)
        logger.debug(s"Read from input stream $rb bytes")
        logger.debug("Buffer size = " + buf.length)
        Some(buf)
      }
    }
  }
}


