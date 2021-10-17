package com.sadliak

import com.sadliak.config.ReplicationConfig
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
object Entrypoint {
    @JvmStatic
    fun main(args: Array<String>) {
        Quarkus.run(App::class.java, *args)
    }

    class App(private val replicationConfig: ReplicationConfig) : QuarkusApplication {
        @Throws(Exception::class)
        override fun run(vararg args: String): Int {
            val nodes = replicationConfig.enabledNodes().map { "${it.key}(${it.value.host}:${it.value.port})" }
            println("Configured secondary nodes: $nodes")

            Quarkus.waitForExit()
            return 0
        }
    }
}
