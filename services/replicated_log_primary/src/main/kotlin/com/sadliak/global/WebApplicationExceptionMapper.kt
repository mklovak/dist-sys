package com.sadliak.global

import com.sadliak.dtos.ErrorResponseDto
import com.sadliak.utils.buildResponse
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class WebApplicationExceptionHandler : ExceptionMapper<WebApplicationException> {
    override fun toResponse(e: WebApplicationException): Response {
        return buildResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                ErrorResponseDto(
                        Response.Status.INTERNAL_SERVER_ERROR.statusCode,
                        Response.Status.INTERNAL_SERVER_ERROR.reasonPhrase
                )
        )
    }
}
