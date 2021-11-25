package com.sadliak.grpc

import com.sadliak.services.HeartbeatService
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni
import java.time.Instant

@GrpcService
class GrpcResource(private val heartbeatService: HeartbeatService) : ReplicatedLog {
    override fun replicateMessage(request: ReplicateMessageRequest?): Uni<ReplicateMessageResponse> {
        throw NotImplementedError("Won't implement this method on primary node")
    }

    override fun heartBeat(request: HeartbeatRequest?): Uni<EmptyResponse> {
        this.heartbeatService.recordNodeHeartbeat(request!!.nodeId, Instant.now())

        return Uni.createFrom().item { EmptyResponse.newBuilder().build() };
    }
}
