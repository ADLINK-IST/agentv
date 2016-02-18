name            := "agentv-prelude"

version		:= "0.5.0-SNAPSHOT"

organization 	:= "com.prismtech"

homepage :=  Some(new java.net.URL("http://prismtech.com"))

scalaVersion 	:= "2.11.7"

publishTo := Some(Resolver.file("file",  new File( "/Users/veda/hacking/labs/techo/mvn-repo/snapshots" )) )
