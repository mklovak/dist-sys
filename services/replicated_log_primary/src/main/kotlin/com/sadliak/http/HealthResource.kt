package com.sadliak.http

import com.sadliak.dtos.HealthResponseDto
import com.sadliak.services.HeartbeatService
import com.sadliak.utils.buildResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/health")
class HealthResource(private val heartbeatService: HeartbeatService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): Response {
        val nodes = this.heartbeatService.getNodeInfos()

        return buildResponse(Response.Status.OK, HealthResponseDto(Response.Status.OK.statusCode, nodes))
    }
}
