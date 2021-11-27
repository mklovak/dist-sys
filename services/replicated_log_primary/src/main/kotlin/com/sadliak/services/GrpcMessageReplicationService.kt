package com.sadliak.services

import com.sadliak.config.ReplicationConfig
import com.sadliak.exceptions.AppException
import com.sadliak.grpc.MutinyReplicatedLogGrpc
import com.sadliak.grpc.ReplicateMessageRequest
import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import io.grpc.ManagedChannelBuilder
import io.quarkus.logging.Log
import java.time.Duration
import java.util.concurrent.CountDownLatch
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GrpcMessageReplicationService(private val replicationConfig: ReplicationConfig,
                                    private val heartbeatService: HeartbeatService) : MessageReplicationService {

    private val secondaryGrpcClients = this.replicationConfig.enabledNodes()
            .map { (nodeId, config) -> nodeId to this.buildGrpcClient(config) }
            .toMap()

    override fun replicateMessage(message: Message, writeConcern: WriteConcern, latch: CountDownLatch) {
        if (this.secondaryGrpcClients.isEmpty()) {
            throw AppException("There are no secondary nodes to replicate messages to")
        }

        try {
            val replicaClients = this.secondaryGrpcClients.entries
            val replicationRequest = ReplicateMessageRequest.newBuilder()
                    .setMessage(message.text)
                    .setMessageId(message.id)
                    .build()

            replicaClients.map { grpcClient ->
                val nodeId = grpcClient.key
                val (initialBackoffDuration, maxBackoffDuration) = this.getRetryBackoffDurations(nodeId)
                Log.info("Replicating asynchronously to '$nodeId' node...")
                Log.info("Initial retry backoff - $initialBackoffDuration, max retry backoff - $maxBackoffDuration")
                grpcClient.value.replicateMessage(replicationRequest)
                        .onFailure().invoke { e -> Log.info("Retrying because of an error during replication to '${nodeId}': ${e.message}") }
                        .onFailure().retry().withBackOff(initialBackoffDuration, maxBackoffDuration).withJitter(0.3).indefinitely()
                        .onItem().invoke { r ->
                            if (r.response != "ok") {
                                throw AppException("Response from replication was not 'ok'")
                            }

                            Log.info("Successfully replicated to '${nodeId}' node")
                            latch.countDown()
                        }
                        .subscribe()
                        .with(
                                { r -> Log.info("Received final response from '${nodeId}': ${r.response}") },
                                { err -> Log.info("Received final error from '${nodeId}': ${err.message}") }
                        )
            }
        } catch (e: Throwable) {
            throw AppException("Error while replicating message to secondary nodes", cause = e)
        }
    }

    private fun getRetryBackoffDurations(nodeId: String): Pair<Duration, Duration> {
        val nodeStatus = this.heartbeatService.getNodeStatus(nodeId)

        val retryConfig = this.replicationConfig.retryConfig()
        return retryConfig.initialBackoff().getOrDefault(nodeStatus, retryConfig.defaults().initialBackoff()) to
                retryConfig.maxBackoff().getOrDefault(nodeStatus, retryConfig.defaults().maxBackoff())
    }

    private fun buildGrpcClient(nodeConfig: ReplicationConfig.Node): MutinyReplicatedLogGrpc.MutinyReplicatedLogStub {
        return MutinyReplicatedLogGrpc.newMutinyStub(
                ManagedChannelBuilder
                        .forAddress(nodeConfig.host, nodeConfig.port)
                        .usePlaintext()
                        .build()
        )
    }
}
