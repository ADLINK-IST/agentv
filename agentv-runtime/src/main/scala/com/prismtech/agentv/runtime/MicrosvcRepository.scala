package com.prismtech.agentv.runtime

import java.io.{FileOutputStream, File}
import java.nio.file._
import java.util.concurrent.atomic.AtomicReference
import java.util.jar.JarFile

import com.prismtech.agentv.core.types.MicrosvcRepoEntry
import com.prismtech.agentv.prelude._
import dds.prelude.HardState
import io.nuvo.concurrent.Worker
import io.nuvo.concurrent.synchronizers._
import io.nuvo.util.log.ConsoleLogger
import io.nuvo.runtime.Config.Logger

import com.prismtech.agentv.prelude._

/**
  * Created by kydos on 14/02/16.
  */
class MicrosvcRepository(baseDir: String, notifier: String => Unit) {
  val JAR_EXT = ".jar"
  val logger = new Logger("MicrosvcRepository")
  val path = baseDir + File.separator + REPO_DIR
  val dir = new File(path)
  val IMPL_TITLE= "Implementation-Title"

  private var microsvcsMap = new AtomicReference(Map[String, String]())

  val watchService = FileSystems.getDefault.newWatchService()

  val watchKey = Paths.get(path).register(watchService,
    StandardWatchEventKinds.ENTRY_CREATE,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_DELETE
  )

  val dirWatcher = Worker.runLoop { () =>
    val usvcs = createJarFiles(dir.list()).filter(isMicroService(_) == true)
    val entries = usvcs.map(getEntryPoints)
    entries.foreach(e => notifier(e._1))
    logger.info("Watching: " + path)
    entries.foreach( e => logger.debug("publishing: " + e._1 + " - " + e._2))
    val usvcsMap = entries.toMap

    usvcs.foreach(_.close())
    val ojars = compareAndSet(microsvcsMap) { js => usvcsMap  }
    val keys = watchService.take()
    keys.pollEvents()
    keys.reset()
    logger.log("The content of the package directory just changed")
  }

  private def getEntryPoints(jf: JarFile): (String, String) = {
    val m = jf.getManifest
    val ma = m.getMainAttributes
    val kind = ma.getValue(JAR_KIND)

    (ma.getValue(IMPL_TITLE), ma.getValue(JAR_MICROSVC_ENTRY_POINT))
  }

  def isMicroService(jf: JarFile): Boolean = {
    val m = jf.getManifest
    val ma = m.getMainAttributes
    val kind = ma.getValue(JAR_KIND)
    val entryPoint = ma.getValue(JAR_MICROSVC_ENTRY_POINT)
    val iname = ma.getValue(IMPL_TITLE)
    (kind != null) && (kind == JAR_KIND_VALUE) && (entryPoint != null) && (iname != null)
  }

  def createJarFiles(xs: Array[String]): List[JarFile] = {
    xs.filter(_.endsWith(JAR_EXT)).map(n => {
      val jarFile = path + File.separator + n
      logger.debug(s"Opening Jar = $jarFile")
      new JarFile(jarFile)
    }).toList
  }

  def install(name: String, content: Array[Byte]): Boolean = {
    val wdir= baseDir + File.separator + STAGING_DIR
    val fname = wdir + File.separator + name
    logger.info(s"Deploying: $fname")
    logger.debug("The Jar is " + content.length + " bytes")
    val os = new FileOutputStream(fname)
    os.write(content)
    os.flush()
    os.close()

    val f = new File(fname)
    val jarf = new JarFile(f)

    if (!isMicroService(jarf)) {
      jarf.close()
      f.delete()
      logger.warning(s"$fname is not a valid microservice, deleting the jar")
      false
    }
    else {
      jarf.close()
      val tfn = path + File.separator + name
      val src = FileSystems.getDefault.getPath(fname)
      val target = FileSystems.getDefault.getPath(tfn)
      Files.move(src, target, StandardCopyOption.ATOMIC_MOVE)
      logger.info(s"Succesfully deployed: $fname")
      true
    }
  }


  def microsvcs = microsvcsMap.get()
}
