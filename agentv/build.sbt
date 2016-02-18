version		:= "0.5.0-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.7"


resolvers += "nuvo.io maven repo" at "http://nuvo-io.github.com/mvn-repo/snapshots"


libraryDependencies += "io.nuvo" % "nuvo-core_2.11" % "0.3.0-SNAPSHOT"


autoCompilerPlugins := true

scalacOptions ++= Seq(
    "-optimise",
    "-unchecked",
    "-language:postfixOps"
  )

scalacOptions ++=  Seq("-Xelide-below", "INFO")

publishTo := Some(Resolver.file("file",  new File( "/Users/veda/hacking/labs/techo/mvn-repo/snapshots" )) )



