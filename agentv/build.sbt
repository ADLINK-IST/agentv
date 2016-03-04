version		:= "0.5.2-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.7"


resolvers += "nuvo.io maven repo" at "http://nuvo-io.github.com/mvn-repo/snapshots"

resolvers += "PrismTech Snapshot Repo" at " http://prismtech.github.io/mvn-repo/snapshots"

resolvers += "Local Repo" at  "file://"+Path.userHome.absolutePath+"/.ivy2/local"

libraryDependencies += "com.prismtech" % "agentv-prelude_2.11" % "0.5.2-SNAPSHOT"

libraryDependencies += "io.nuvo" % "nuvo-core_2.11" % "0.3.0-SNAPSHOT"



autoCompilerPlugins := true

scalacOptions ++= Seq(
    "-optimise",
    "-unchecked",
    "-language:postfixOps"
  )

scalacOptions ++=  Seq("-Xelide-below", "INFO")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )




