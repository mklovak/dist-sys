package com.vbarbanyagra

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.pattern.after
import akka.stream.Materializer
import akka.util.Timeout
import com.sadliak.grpc._
import com.vbarbanyagra.MessageRegistry._

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ReplicatedLogGrpcServiceImpl(userRegistry: ActorRef[MessageRegistry.Command])
                                  (implicit mat: Materializer, system: ActorSystem[_]) extends ReplicatedLog {

  import mat.executionContext

  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  private val insertDelay: FiniteDuration =
    FiniteDuration(system.settings.config.getDuration("my-app.insert-delay").toNanos, TimeUnit.NANOSECONDS)

  def appendMessage(in: ReplicateMessageRequest): Future[ReplicateMessageResponse] = {
    after(insertDelay) {
      userRegistry.ask(AppendMessage(Message(in.message, in.messageId), _))
        .map(action => ReplicateMessageResponse(action.description))
    }
  }

  override def heartBeat(in: HeartbeatRequest): Future[EmptyResponse] = {
    Future {
      EmptyResponse()
    }
  }

  override def replicateMessage(in: ReplicateMessageRequest): Future[ReplicateMessageResponse] = appendMessage(in)
}
