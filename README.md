# AgentV: A Microservice Framework for Vortex

AgentV provides a very lightweight microservice framework for
Vortex. At the preent time microservices can only be packaged as Jar
and implemented in a langauge targeting the JVM such as Java, Scala,
etc. Future version of AgentV may provide support for other
programming languages and packages.

## Building and Installing AgentV

The first step toward experimenting with AgentV is to build it. The
AgentV runtime, its GUI and the demo can all be built and installed by
issuing the following commands:

	$./build-all
	$./install
	
	
## Running AgentV
To start playing with AgentV you need to start the agent and the UI:

	$./bin/agentv your-node-name some-description &
	$./bin/commander &
	
At this point you can use the GUI to deploy microservices and manage them. The sample microservices are located under the ./lib directory.



