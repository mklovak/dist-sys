package com.sadliak.config

import io.quarkus.runtime.Startup
import javax.enterprise.context.ApplicationScoped

@Startup
@ApplicationScoped
class ReplicationConfig {
    private val nodes: Map<String, Node>

    fun enabledNodes(): Map<String, Node> {
        return this.nodes.filter { it.value.isEnabled }
    }

    init {
        val regex = Regex("(SECONDARY_\\d+)_(ID|HOST|PORT|ENABLED)")
        val mappedNodes = System.getenv().entries
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
}
