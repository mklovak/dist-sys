package com.sadliak.services

import com.sadliak.models.Message

interface MessageStorageService {
    fun addMessage(message: Message)
    fun listMessages(): List<Message>
}
