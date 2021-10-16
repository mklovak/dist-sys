package com.sadliak.utils

import com.sadliak.dtos.ResponseDto
import javax.ws.rs.core.Response

fun <Entity : ResponseDto> buildResponse(status: Response.Status, entity: Entity): Response {
    return Response
            .status(status)
            .entity(entity)
            .build()
}
