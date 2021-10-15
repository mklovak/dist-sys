package com.sadliak.services

import com.sadliak.models.Message

interface MessageReplicationService {
    fun replicateMessage(message: Message)
}
