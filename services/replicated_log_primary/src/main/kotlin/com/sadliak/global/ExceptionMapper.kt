package com.sadliak.global

import com.sadliak.dtos.ErrorResponseDto
import com.sadliak.utils.buildResponse
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ExceptionHandler : ExceptionMapper<Throwable> {
    override fun toResponse(e: Throwable): Response {
        return buildResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                ErrorResponseDto(
                        Response.Status.INTERNAL_SERVER_ERROR.statusCode,
                        Response.Status.INTERNAL_SERVER_ERROR.reasonPhrase
                )
        )
    }
}
