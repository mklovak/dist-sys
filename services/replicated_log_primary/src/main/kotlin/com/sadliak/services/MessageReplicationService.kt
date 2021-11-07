package com.sadliak.services

import com.sadliak.models.Message
import com.sadliak.models.WriteConcern

interface MessageReplicationService {
    fun replicateMessage(message: Message, writeConcern: WriteConcern)
}
