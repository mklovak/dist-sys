package com.vbarbanyagra

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.Materializer
import akka.util.Timeout
import com.sadliak.grpc._
import com.vbarbanyagra.MessageRegistry._

import scala.concurrent.Future

class ReplicatedLogGrpcServiceImpl(userRegistry: ActorRef[MessageRegistry.Command])
                                  (implicit mat: Materializer, system: ActorSystem[_]) extends ReplicatedLog {
  import mat.executionContext

  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def appendMessage(in: ReplicateMessageRequest): Future[ReplicateMessageResponse] =
    userRegistry.ask(AppendMessage(Message(in.message), _))
      .map(action => ReplicateMessageResponse(action.description))

  override def replicateMessage(in: ReplicateMessageRequest): Future[ReplicateMessageResponse] = appendMessage(in)
}
