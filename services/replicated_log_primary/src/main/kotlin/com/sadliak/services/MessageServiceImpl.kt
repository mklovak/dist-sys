package com.sadliak.services

import com.sadliak.dtos.AddMessageRequestDto
import com.sadliak.models.Message
import com.sadliak.models.WriteConcern
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class MessageServiceImpl(val messageStorageService: MessageStorageService,
                         val messageReplicationService: MessageReplicationService) : MessageService {
    private val counter = AtomicInteger(-1)

    override fun addMessage(requestDto: AddMessageRequestDto) {
        val message = Message(counter.incrementAndGet().toLong(), requestDto.message)
        val writeConcern = WriteConcern(requestDto.w)

        val latch = CountDownLatch(writeConcern.value)
        addToStorage(message, latch)
        messageReplicationService.replicateMessage(message, writeConcern, latch)

        // Wait at most 1 week.
        val replicationCompleted = latch.await(7, TimeUnit.DAYS)
        require(replicationCompleted) {
            "Timed out waiting for the replication to complete with write concern set to ${writeConcern.value}"
        }
    }

    private fun addToStorage(message: Message, latch: CountDownLatch) {
        messageStorageService.addMessage(message)
        latch.countDown()
    }

    override fun listMessages(): List<Message> {
        return this.messageStorageService.listMessages()
    }
}
