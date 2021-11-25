package com.sadliak.services

import com.sadliak.enums.NodeStatus
import java.time.Instant

interface HeartbeatService {
    fun recordNodeHeartbeat(nodeId: String, heartbeatReceivedTimestamp: Instant)

    fun getNodeStatus(nodeId: String): NodeStatus
}
