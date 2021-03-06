package com.vbarbanyagra

import com.vbarbanyagra.MessageRegistry.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val messageJsonFormat: RootJsonFormat[Message] = jsonFormat2(Message)
  implicit val messagesJsonFormat: RootJsonFormat[Messages] = jsonFormat2(Messages)
  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
}
