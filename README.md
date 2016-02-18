# AgentV: A Microservice Framework for Vortex

AgentV provides a very lightweight microservice framework for
Vortex. At the preent time microservices can only be packaged as Jar
and implemented in a langauge targeting the JVM such as Java, Scala,
etc. Future version of AgentV may provide support for other
programming languages and packages.

## Running AgentV
AgentV comes with batteries included, meaning all the jars required to start playing with it are already included in the repository you check out. The only thing you need to run it is **Java**. 

Before you start AgentV, you should check how many network interface has your computer and understand if you'd like to force communication through one of those. 

By default, an inteface will be detected automatically by the runtime. If you want to explicitely set an interface, such as *en0*, *eth1*, *wlan0*, or anything else that may make sense for your configuration, you may do it by replacing the *auto* below with the appropriate interface name.

	-Dddsi.network.interface=auto # replace auto with iface name
	

To start playing with AgentV you need to start the agent and the UI:

	$./bin/agentv your-node-name some-description &
	$./bin/commander &
	
At this point you can use the GUI to deploy microservices and manage them. The sample microservices are located under the ./lib directory.

## Building and Installing AgentV

The first step toward experimenting with AgentV is to build it. The
AgentV runtime, its GUI and the demo can all be built and installed by
issuing the following commands:

	$./build-all
	$./install
	


