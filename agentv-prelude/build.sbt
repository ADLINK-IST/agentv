val jdk7 = true

val jdkver = if (jdk7) "-jdk7" else ""

name            := s"agentv-prelude$jdkver"

version		:= "0.5.3-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.8"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )
