val jdk7 = true

val jdkver = if (jdk7) "-jdk7" else ""

name		:= s"agentv$jdkver"

version		:= "0.5.3-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.8"

val dpfix = jdkver + "_2.11"

resolvers += "nuvo.io maven repo" at "http://nuvo-io.github.com/mvn-repo/snapshots"

resolvers += "PrismTech Snapshot Repo" at " http://prismtech.github.io/mvn-repo/snapshots"

resolvers += "Local Repo" at  "file://"+Path.userHome.absolutePath+"/.ivy2/local"

libraryDependencies += "com.prismtech" % s"agentv-prelude$dpfix" % "0.5.3-SNAPSHOT"

libraryDependencies += "io.nuvo" % s"nuvo-core$dpfix" % "0.3.2-SNAPSHOT"



autoCompilerPlugins := true

scalacOptions ++= Seq(
    "-optimise",
    "-unchecked",
    "-language:postfixOps"
  )

scalacOptions ++=  Seq("-Xelide-below", "INFO")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )




