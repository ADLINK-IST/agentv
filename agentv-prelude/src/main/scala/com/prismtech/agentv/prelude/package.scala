package com.prismtech.agentv

package object prelude {
  val NodePartition               =  "com/prismtech/node"
  val NodeInfoTopicName           =  "NodeInfo"
  val NodeErrorTopicName           =  "NodeError"
  val MicrosvcActionTopicName     =  "MicroserviceAction"
  val RepoActionTopicName         =  "RepositoryAction"
  val RunningMicrosvcTopicName    =  "RunningMicrosvc"
  val MicroSvcRepositoryEntryTopicName    =  "MicrosvcRepositoryEntry"

  val DDS_RUNTIME_PARTITION               = "dds.runtime.node.partition"
  val DDS_RUNTIME_NODE_INFO_TOPIC_NAME   = "dds.runtime.node.topic.info"
  val DDS_RUNTIME_NODE_CMD_TOPIC_NAME    = "dds.runtime.node.topic.cmd"
  val DDS_RUNTIME_REPOSITORY_PATH         = "dds.runtime.repository.path"
  val DDS_RUNTIME_NODE_UUID              = "dds.runtime.node.uuid"
  val DDS_RUNTIME_NODE_MICROSVC_PARTITION = "dds.runtime.node.microsvc.partition"

  val JAR_MICROSVC_ENTRY_POINT  = "Entry-Point"
  val JAR_KIND                  = "Jar-Kind"
  val JAR_KIND_VALUE            = "vortex-microservice"

  val cafe = "com.prismtech.cafe.core.ServiceEnvironmentImpl"

  val props = System.getProperties

  def initProperty(p: java.util.Properties)(key: String, value:String):  java.util.Properties = {
    if (p.getProperty(key) == null) p.setProperty(key, value)
    p
  }
  def setProperty(p: java.util.Properties)(key: String, value:String):  java.util.Properties = {
    p.setProperty(key, value)
    p
  }

  val initRuntimeProp = initProperty(props) _
  val setRuntimeProp = setProperty(props) _

  initRuntimeProp(DDS_RUNTIME_PARTITION, NodePartition)
  initRuntimeProp(DDS_RUNTIME_NODE_INFO_TOPIC_NAME, NodeInfoTopicName)
  initRuntimeProp(DDS_RUNTIME_NODE_CMD_TOPIC_NAME, MicrosvcActionTopicName)

  def getSysProperty(key: String): Option[String] = {
    val value = props.getProperty(key)
    Option(value)
  }
}
