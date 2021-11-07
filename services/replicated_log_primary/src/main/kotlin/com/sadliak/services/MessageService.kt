package com.sadliak.services

import com.sadliak.dtos.AddMessageRequestDto
import com.sadliak.models.Message

interface MessageService {
    fun addMessage(requestDto: AddMessageRequestDto)
    fun listMessages(): List<Message>
}
