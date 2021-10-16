package com.sadliak.exceptions

import javax.ws.rs.core.Response

class AppException(override val message: String,
                   val status: Response.Status = Response.Status.INTERNAL_SERVER_ERROR,
                   cause: Throwable? = null) : RuntimeException(message, cause)
