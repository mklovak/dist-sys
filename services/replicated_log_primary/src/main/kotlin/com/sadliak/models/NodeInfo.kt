package com.sadliak.models

import com.sadliak.enums.NodeStatus
import java.time.Instant

data class NodeInfo(val nodeId: String, val status: NodeStatus, val lastHeartbeatTimestamp: Instant?)
