import sbt._
import Keys._

object AgentV extends Build {

  lazy val coreTypes = Project(id ="agentv-core-types", base = file("agentv-core-types"))

  lazy val prelude = Project(id = "agentv-prelude", base = file("agentv-prelude"))

  lazy val microsvc =  Project(id = "agentv-microsvc", base = file("agentv-microsvc"))

  lazy val agentv = Project(id = "agentv", base = file("agentv"))

  lazy val agentvRuntime = Project(id = "agentv-runtime", base = file("agentv-runtime"))

  lazy val microsvcHello =  Project(id = "agentv-microsvc-hello", base = file("agentv-microsvc-hello"))

  lazy val microsvcDayTime =  Project(id = "agentv-microsvc-daytime", base = file("agentv-microsvc-daytime"))

  lazy val microsvcDayTimeLogger=  Project(id = "agentv-microsvc-daytimelog", base = file("agentv-microsvc-daytimelog")) 

  lazy val commander  =  Project(id = "agentv-commander", base = file("agentv-commander")) 
}
