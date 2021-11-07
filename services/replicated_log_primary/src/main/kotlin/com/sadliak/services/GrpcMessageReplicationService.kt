package com.sadliak.services

import com.sadliak.config.ReplicationConfig
import com.sadliak.exceptions.AppException
import com.sadliak.grpc.MutinyReplicatedLogGrpc
import com.sadliak.grpc.ReplicateMessageRequest
import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import io.grpc.ManagedChannelBuilder
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GrpcMessageReplicationService(private val replicationConfig: ReplicationConfig) : MessageReplicationService {

    private val secondaryGrpcClients = this.replicationConfig.enabledNodes()
            .map { (name, config) -> name to this.buildGrpcClient(config) }
            .toMap()

    override fun replicateMessage(message: Message, writeConcern: WriteConcern) {
        if (this.secondaryGrpcClients.isEmpty()) {
            throw AppException("There are no secondary nodes to replicate messages to")
        }

        try {
            val replicationRequest = ReplicateMessageRequest.newBuilder().setMessage(message.text).build()

            // All replica calls should be asynchronous.
            if (writeConcern.w == 1) {
                replicateAsynchronously(replicationRequest, this.secondaryGrpcClients.values)
            } else {
                val synchronousReplicasCount = writeConcern.w - 1
                val synchronousReplicaClients = this.secondaryGrpcClients.values.take(synchronousReplicasCount)
                val asynchronousReplicaClients = this.secondaryGrpcClients.values.drop(synchronousReplicasCount)

                replicateSynchronously(replicationRequest, synchronousReplicaClients)
                replicateAsynchronously(replicationRequest, asynchronousReplicaClients)
            }
        } catch (e: Throwable) {
            throw AppException("Error while replicating message to secondary nodes", cause = e)
        }
    }

    private fun replicateSynchronously(replicationRequest: ReplicateMessageRequest,
                                       replicaClients: Collection<MutinyReplicatedLogGrpc.MutinyReplicatedLogStub>) {
        if (replicaClients.isNotEmpty()) {
            println("Replicating synchronously to ${replicaClients.size} nodes")
            Uni.join().all(replicaClients.map { grpcClient -> grpcClient.replicateMessage(replicationRequest) })
                    .andCollectFailures()
                    .await()
                    .indefinitely()
        } else {
            println("No nodes to replicate synchronously to")
        }
    }

    private fun replicateAsynchronously(replicationRequest: ReplicateMessageRequest,
                                        replicaClients: Collection<MutinyReplicatedLogGrpc.MutinyReplicatedLogStub>) {
        if (replicaClients.isNotEmpty()) {
            println("Replicating asynchronously to ${replicaClients.size} nodes")
            replicaClients.map { grpcClient -> grpcClient.replicateMessage(replicationRequest).subscribe() }
        } else {
            println("No nodes to replicate asynchronously to")
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
