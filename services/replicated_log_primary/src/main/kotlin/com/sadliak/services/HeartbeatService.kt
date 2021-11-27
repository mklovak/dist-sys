package com.sadliak.services

import com.sadliak.models.NodeInfo
import java.time.Instant

interface HeartbeatService {
    fun recordNodeHeartbeat(nodeId: String, heartbeatReceivedTimestamp: Instant)

    fun getNodeInfo(nodeId: String): NodeInfo

    fun getNodeInfos(): List<NodeInfo>

    fun isNodeQuorumLost(): Boolean
}
