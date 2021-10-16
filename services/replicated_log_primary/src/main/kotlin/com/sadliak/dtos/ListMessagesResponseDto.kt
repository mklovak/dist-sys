package com.sadliak.dtos

data class ListMessagesResponseDto(override val status: Int = 0,
                                   val data: List<String> = listOf()) : ResponseDto
