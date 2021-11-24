package com.vbarbanyagra

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

final case class Message(message: String, messageId: Long)
final case class Messages(messages: immutable.Seq[Message],
                          dirtyMessages: immutable.Seq[Message])

object MessageRegistry {
  // actor protocol
  sealed trait Command
  final case class GetMessages(replyTo: ActorRef[Messages]) extends Command
  final case class AppendMessage(message: Message, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Nil)

  private def untilFirstMissingId(messages: Seq[Message]): Seq[Message] =
    messages
      .zip(LazyList.from(0))
      .takeWhile { case (message, i) =>
        message.messageId == i
      }
      .map(_._1)

  private def registry(sortedDistinctDirtyMessages: Seq[Message]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMessages(replyTo) =>
        val visibleMessages = untilFirstMissingId(sortedDistinctDirtyMessages)
        replyTo ! Messages(
          messages = visibleMessages,
          dirtyMessages = sortedDistinctDirtyMessages
        )
        Behaviors.same

      case AppendMessage(message, replyTo) =>
        replyTo ! ActionPerformed("ok")
        registry(
          (sortedDistinctDirtyMessages :+ message)
            .distinctBy(_.messageId)
            .sortBy(_.messageId)
        )
    }
}
