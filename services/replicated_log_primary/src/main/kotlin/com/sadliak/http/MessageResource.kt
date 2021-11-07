package com.sadliak.http

import com.sadliak.dtos.AddMessageRequestDto
import com.sadliak.dtos.AddMessageResponseDto
import com.sadliak.dtos.ListMessagesResponseDto
import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import com.sadliak.services.MessageReplicationService
import com.sadliak.services.MessageService
import com.sadliak.utils.buildResponse
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/messages")
class MessageResource(val messageService: MessageService, val messageReplicationService: MessageReplicationService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun listMessages(): Response {
        val messages = this.messageService.listMessages().map { msg -> msg.text }

        return buildResponse(Response.Status.OK, ListMessagesResponseDto(Response.Status.OK.statusCode, messages))
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun addMessage(requestDto: AddMessageRequestDto): Response {
        val message = Message(requestDto.message)
        val writeConcern = WriteConcern(requestDto.w)

        this.messageService.addMessage(message)
        this.messageReplicationService.replicateMessage(message, writeConcern)

        return buildResponse(Response.Status.OK, AddMessageResponseDto(Response.Status.OK.statusCode))
    }
}
