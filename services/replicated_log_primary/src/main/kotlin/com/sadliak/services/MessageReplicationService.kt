package com.sadliak.services

import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import java.util.concurrent.CountDownLatch

interface MessageReplicationService {
    fun replicateMessage(message: Message, writeConcern: WriteConcern, latch: CountDownLatch)
}
