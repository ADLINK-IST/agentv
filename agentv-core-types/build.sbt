name            := "agentv-core-types"

version		:= "0.5.0-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.7"

resolvers += "Vortex Snapshot Repo" at "https://dl.dropboxusercontent.com/u/19238968/devel/mvn-repo/vortex"

libraryDependencies += "com.prismtech.cafe" % "cafe" % "2.2.1-SNAPSHOT"

publishTo := Some(Resolver.file("file",  new File( "/Users/veda/hacking/labs/techo/mvn-repo/snapshots" )) )
