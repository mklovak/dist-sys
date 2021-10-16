package com.sadliak.services

import com.sadliak.config.ReplicationConfig
import com.sadliak.exceptions.AppException
import com.sadliak.grpc.MutinyReplicatedLogGrpc
import com.sadliak.grpc.ReplicateMessageRequest
import com.sadliak.models.Message
import io.grpc.ManagedChannelBuilder
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GrpcMessageReplicationService(private val replicationConfig: ReplicationConfig) : MessageReplicationService {

    private val secondaryGrpcClients = this.replicationConfig.nodes()
            .filter { (_, config) -> config.isEnabled() }
            .map { (name, config) -> name to this.buildGrpcClient(config) }
            .toMap()

    override fun replicateMessage(message: Message) {
        try {
            val replicationRequest = ReplicateMessageRequest.newBuilder().setMessage(message.text).build()

            Uni.join().all(
                    secondaryGrpcClients.values.map { grpcClient -> grpcClient.replicateMessage(replicationRequest) }
            ).andCollectFailures().await().indefinitely()
        } catch (e: Throwable) {
            throw AppException("Error while replicating message to secondary nodes", cause = e)
        }
    }

    private fun buildGrpcClient(nodeConfig: ReplicationConfig.Node): MutinyReplicatedLogGrpc.MutinyReplicatedLogStub {
        return MutinyReplicatedLogGrpc.newMutinyStub(
                ManagedChannelBuilder.forAddress(nodeConfig.host(), nodeConfig.port())
                        .usePlaintext()
                        .build()
        )
    }
}
