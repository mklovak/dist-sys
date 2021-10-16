package com.sadliak.dtos

data class ErrorResponseDto(override val status: Int,
                            val reason: String) : ResponseDto
