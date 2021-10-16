package com.sadliak.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "replication")
interface ReplicationConfig {
    fun nodes(): Map<String, Node>

    interface Node {
        fun isEnabled(): Boolean
        fun host(): String
        fun port(): Int
    }
}
