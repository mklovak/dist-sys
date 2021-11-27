package com.sadliak.config

import com.sadliak.enums.NodeStatus
import io.quarkus.runtime.Startup
import io.smallrye.config.ConfigMapping
import java.time.Duration
import javax.enterprise.context.ApplicationScoped


@Startup
@ApplicationScoped
class ReplicationConfig(private val retries: RetryConfig) {
    private val nodes: Map<String, Node>
    private val shouldCheckQuorum: Boolean

    fun enabledNodes(): Map<String, Node> {
        return this.nodes.filter { it.value.isEnabled }
    }

    fun shouldCheckQuorum(): Boolean {
        return this.shouldCheckQuorum
    }

    fun retryConfig(): RetryConfig {
        return this.retries
    }

    init {
        val envVars = System.getenv()
        shouldCheckQuorum = envVars["SHOULD_CHECK_QUORUM"].toBoolean()

        val regex = Regex("(SECONDARY_\\d+)_(ID|HOST|PORT|ENABLED)")
        val mappedNodes = envVars.entries
                .map { (name, value) -> regex.matchEntire(name)?.groupValues to value }
                .filter { (nameRegexGroups) -> nameRegexGroups != null }
                .map { (nameRegexGroups, value) ->
                    val nodeName = nameRegexGroups!![1].lowercase()
                    val nodeProperty = object {
                        val property = nameRegexGroups[2].lowercase()
                        val value = value
                    }

                    nodeName to nodeProperty
                }
                .groupBy { (nodeName) -> nodeName }
                .mapValues { (_, values) ->
                    val nodeProperties = values.map { it.second }
                    val id = nodeProperties.find { it.property == "id" }?.value
                    val host = nodeProperties.find { it.property == "host" }?.value ?: "0.0.0.0"
                    val port = nodeProperties.find { it.property == "port" }?.value?.toIntOrNull() ?: -1
                    val isEnabled = nodeProperties.find { it.property == "enabled" }?.value?.toBoolean() ?: false

                    if (id == null) {
                        throw RuntimeException("All secondary nodes should have ID env variable set (SECONDARY_X_ID)")
                    }

                    Node(id, isEnabled, host, port)
                }

        this.nodes = mappedNodes.values.associateBy { node -> node.id }
    }

    data class Node(val id: String, val isEnabled: Boolean, val host: String, val port: Int)

    @ConfigMapping(prefix = "replication.retries")
    interface RetryConfig {
        fun defaults(): Defaults
        fun initialBackoff(): Map<NodeStatus, Duration>
        fun maxBackoff(): Map<NodeStatus, Duration>

        interface Defaults {
            fun initialBackoff(): Duration
            fun maxBackoff(): Duration
        }
    }
}
