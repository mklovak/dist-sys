package com.sadliak.services

import com.sadliak.enums.NodeStatus
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InMemoryHeartbeatService : HeartbeatService {
    private val lastHeartbeats: MutableMap<String, Instant> = ConcurrentHashMap();

    override fun recordNodeHeartbeat(nodeId: String, heartbeatReceivedTimestamp: Instant) {
        lastHeartbeats[nodeId] = heartbeatReceivedTimestamp;
    }

    override fun getNodeStatus(nodeId: String): NodeStatus {
        val now = Instant.now()
        val lastHeartbeat = lastHeartbeats[nodeId] ?: return NodeStatus.UNHEALTHY

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
