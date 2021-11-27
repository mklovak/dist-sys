package com.sadliak.services

import com.sadliak.enums.NodeStatus
import io.quarkus.logging.Log
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InMemoryHeartbeatService : HeartbeatService {
    private val lastHeartbeats: MutableMap<String, Instant> = ConcurrentHashMap();

    override fun recordNodeHeartbeat(nodeId: String, heartbeatReceivedTimestamp: Instant) {
        Log.info("Recorded heartbeat for '$nodeId' at $heartbeatReceivedTimestamp")
        lastHeartbeats[nodeId] = heartbeatReceivedTimestamp;
    }

    // We will keep things simple and treat only 0 HEALTHY/SUSPECTED nodes as a quorum loss.
    override fun isQuorumLost(): Boolean {
        val now = Instant.now()
        val atLeastOneNodeAlive = lastHeartbeats.keys.any { nodeId ->
            listOf(NodeStatus.HEALTHY, NodeStatus.SUSPECTED)
                    .contains(calculateNodeStatus(lastHeartbeats[nodeId], now))
        }

        return !atLeastOneNodeAlive
    }

    override fun getNodeStatus(nodeId: String): NodeStatus {
        val now = Instant.now()

        return calculateNodeStatus(lastHeartbeats[nodeId], now)
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
