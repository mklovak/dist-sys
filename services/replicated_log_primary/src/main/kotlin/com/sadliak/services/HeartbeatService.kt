package com.sadliak.services

import java.time.Instant

interface HeartbeatService {
    fun record(nodeId: String, heartbeatReceivedTimestamp: Instant)
}
