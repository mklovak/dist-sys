package com.sadliak.services

import com.sadliak.exceptions.AppException
import com.sadliak.grpc.MutinyReplicatedLogGrpc
import com.sadliak.grpc.ReplicateMessageRequest
import com.sadliak.models.Message
import io.grpc.ManagedChannelBuilder
import io.quarkus.grpc.runtime.config.GrpcClientConfiguration
import io.quarkus.grpc.runtime.config.GrpcConfiguration
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class GrpcMessageReplicationService(private val grpcConfiguration: GrpcConfiguration) : MessageReplicationService {

    private val secondaryGrpcClients = this.grpcConfiguration.clients
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

    private fun buildGrpcClient(clientConfig: GrpcClientConfiguration): MutinyReplicatedLogGrpc.MutinyReplicatedLogStub {
        return MutinyReplicatedLogGrpc.newMutinyStub(
                ManagedChannelBuilder.forAddress(clientConfig.host, clientConfig.port)
                        .usePlaintext()
                        .build()
        )
    }
}
