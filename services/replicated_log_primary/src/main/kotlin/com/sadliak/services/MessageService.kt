package com.sadliak.services

import com.sadliak.models.Message

interface MessageService {
    fun addMessage(message: Message)
    fun listMessages(): List<Message>
}
