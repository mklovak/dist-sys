package com.sadliak.services

import com.sadliak.config.ReplicationConfig
import com.sadliak.exceptions.AppException
import com.sadliak.grpc.MutinyReplicatedLogGrpc
import com.sadliak.grpc.ReplicateMessageRequest
import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.CountDownLatch
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GrpcMessageReplicationService(private val replicationConfig: ReplicationConfig) : MessageReplicationService {

    private val secondaryGrpcClients = this.replicationConfig.enabledNodes()
            .map { (name, config) -> name to this.buildGrpcClient(config) }
            .toMap()

    override fun replicateMessage(message: Message, writeConcern: WriteConcern, latch: CountDownLatch) {
        if (this.secondaryGrpcClients.isEmpty()) {
            throw AppException("There are no secondary nodes to replicate messages to")
        }

        try {
            val replicaClients = this.secondaryGrpcClients.entries
            val replicationRequest = ReplicateMessageRequest.newBuilder().setMessage(message.text).build()

            println("Replicating asynchronously to ${replicaClients.size} nodes")
            replicaClients.map { grpcClient ->
                grpcClient.value.replicateMessage(replicationRequest)
                        .subscribe()
                        .with { println("Successfully replicated to \"${grpcClient.key}\" node"); latch.countDown() }
            }
        } catch (e: Throwable) {
            throw AppException("Error while replicating message to secondary nodes", cause = e)
        }
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
