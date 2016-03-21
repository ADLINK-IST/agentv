val jdk7 = true

val jdkver = if (jdk7) "-jdk7" else ""

name            := s"agentv-microsvc-hello$jdkver"

version		:= "0.5.3-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.8"


packageOptions += Package.ManifestAttributes(
  "Entry-Point" -> "com.prismtech.agentv.capsule.hello.Hello",
  "Jar-Kind"    -> "vortex-microservice"
)

val dpfix = jdkver + "_2.11"

resolvers += "Vortex Snapshot Repo" at "https://dl.dropboxusercontent.com/u/19238968/devel/mvn-repo/vortex"

resolvers += "PrismTech Snapshot Repo"at " http://prismtech.github.io/mvn-repo/snapshots"

resolvers += "nuvo.io maven repo" at "http://nuvo-io.github.com/mvn-repo/snapshots"

resolvers += "Local Repo"at  "file://"+Path.userHome.absolutePath+"/.ivy2/local"


libraryDependencies += "com.prismtech" % s"agentv-microsvc$dpfix" % "0.5.3-SNAPSHOT"

libraryDependencies += "com.prismtech.cafe" % "cafe" % "2.2.1-SNAPSHOT"

libraryDependencies += "io.nuvo" % s"moliere$dpfix" % "0.12.2-SNAPSHOT"

libraryDependencies += "io.nuvo" % s"nuvo-core$dpfix" % "0.3.2-SNAPSHOT"


publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )
