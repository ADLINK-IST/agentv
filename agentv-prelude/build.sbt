name            := "agentv-prelude"

version		:= "0.5.2-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.7"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/hacking/labs/techo/mvn-repo/snapshots" )) )
