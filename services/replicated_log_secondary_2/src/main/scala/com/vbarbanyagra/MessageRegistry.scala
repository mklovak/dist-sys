package com.vbarbanyagra

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

final case class Message(message: String)
final case class Messages(messages: immutable.Seq[Message])

object MessageRegistry {
  // actor protocol
  sealed trait Command
  final case class GetMessages(replyTo: ActorRef[Messages]) extends Command
  final case class AppendMessage(message: Message, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Nil)

  private def registry(users: Seq[Message]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMessages(replyTo) =>
        replyTo ! Messages(users)
        Behaviors.same
      case AppendMessage(user, replyTo) =>
        replyTo ! ActionPerformed("ok")
        registry(users :+ user)
    }
}
