package com.sadliak.services

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InMemoryHeartbeatService : HeartbeatService {
    private val lastHeartbeats: MutableMap<String, Instant> = ConcurrentHashMap();

    override fun record(nodeId: String, heartbeatReceivedTimestamp: Instant) {
        lastHeartbeats[nodeId] = heartbeatReceivedTimestamp;
    }
}
