package com.sadliak.services

import com.sadliak.config.ReplicationConfig
import com.sadliak.enums.NodeStatus
import com.sadliak.models.NodeInfo
import io.quarkus.logging.Log
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InMemoryHeartbeatService(private val replicationConfig: ReplicationConfig) : HeartbeatService {
    private val lastHeartbeats: MutableMap<String, Instant> = ConcurrentHashMap();

    override fun recordNodeHeartbeat(nodeId: String, heartbeatReceivedTimestamp: Instant) {
        Log.info("Recorded heartbeat for '$nodeId' at $heartbeatReceivedTimestamp")
        lastHeartbeats[nodeId] = heartbeatReceivedTimestamp;
    }

    // We will keep things simple and treat only 0 HEALTHY/SUSPECTED nodes as a quorum loss.
    override fun isNodeQuorumLost(): Boolean {
        return this.getNodeInfos().all { nodeInfo -> nodeInfo.status == NodeStatus.UNHEALTHY }
    }

    override fun getNodeInfos(): List<NodeInfo> {
        return replicationConfig.enabledNodes().keys.map { nodeId -> this.getNodeInfo(nodeId) }
    }

    override fun getNodeInfo(nodeId: String): NodeInfo {
        val now = Instant.now()
        val lastHeartbeat = lastHeartbeats[nodeId]
        val status = calculateNodeStatus(lastHeartbeat, now)

        return NodeInfo(nodeId, status, lastHeartbeat)
    }

    private fun calculateNodeStatus(lastHeartbeat: Instant?, now: Instant): NodeStatus {
        if (lastHeartbeat == null) {
            return NodeStatus.UNHEALTHY
        }

        val lastHeartbeatDelayInSeconds = Duration.between(lastHeartbeat, now).seconds
        if (lastHeartbeatDelayInSeconds >= 60) {
            return NodeStatus.UNHEALTHY
        }

        if (lastHeartbeatDelayInSeconds >= 30) {
            return NodeStatus.SUSPECTED
        }

        return NodeStatus.HEALTHY
    }
}
