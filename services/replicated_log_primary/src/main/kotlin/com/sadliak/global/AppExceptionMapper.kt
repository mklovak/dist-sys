package com.sadliak.global

import com.sadliak.dtos.ErrorResponseDto
import com.sadliak.exceptions.AppException
import com.sadliak.utils.buildResponse
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class AppExceptionHandler : ExceptionMapper<AppException> {
    override fun toResponse(e: AppException): Response {
        return buildResponse(e.status, ErrorResponseDto(e.status.statusCode, e.message))
    }
}
