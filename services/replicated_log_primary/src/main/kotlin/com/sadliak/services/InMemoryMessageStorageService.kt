package com.sadliak.services

import com.sadliak.models.Message
import java.util.*
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class InMemoryMessageStorageService : MessageStorageService {

    private val messages: MutableList<Message> = Collections.synchronizedList(mutableListOf());

    override fun addMessage(message: Message) {
        synchronized(this) {
            this.messages.add(message)
        }
    }

    override fun listMessages(): List<Message> {
        synchronized(this) {
            return this.messages
        }
    }
}
