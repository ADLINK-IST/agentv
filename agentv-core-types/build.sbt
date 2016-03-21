val jdk7 = true

val jdkver = if (jdk7) "-jdk7" else ""

name            := s"agentv-core-types$jdkver"

version		:= "0.5.3-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.8"

// scalaVersion 	:= "2.10.6"

resolvers += "Vortex Snapshot Repo" at "https://dl.dropboxusercontent.com/u/19238968/devel/mvn-repo/vortex"

libraryDependencies += "com.prismtech.cafe" % "cafe" % "2.2.1-SNAPSHOT"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )


