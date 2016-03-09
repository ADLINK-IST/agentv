package com.prismtech.agentv.commander

import com.prismtech.agentv.core.types.{NodeInfo, MicrosvcRepoEntry, RunningMicrosvc, NodeError}

trait AgentvEventListener {

  def onNodeError(error: NodeError): Unit

  def onUpdatedRunningMicrosvcs(rms: java.util.List[RunningMicrosvc]): Unit

  def onUpdatedMicrosvcRepository(res: java.util.List[MicrosvcRepoEntry]): Unit

  def onUpdatedNodes(ns: java.util.List[NodeInfo]): Unit

}
