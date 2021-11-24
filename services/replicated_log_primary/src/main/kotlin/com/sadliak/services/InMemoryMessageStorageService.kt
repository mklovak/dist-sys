package com.sadliak.services

import com.sadliak.models.Message
import java.util.concurrent.CopyOnWriteArrayList
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class InMemoryMessageStorageService : MessageStorageService {
    private val messages: MutableList<Message> = CopyOnWriteArrayList();

    override fun addMessage(message: Message) {
        this.messages.add(message)
    }

    override fun listMessages(): List<Message> {
        return this.messages
    }
}
