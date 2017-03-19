package rabbit

import java.util.UUID

import spray.json._

object CustomConverters {
  implicit object UUIDFormat extends JsonFormat[UUID] {
    override def read (json: JsValue): UUID = json match {
      case str : JsString => UUID.fromString(str.value)
    }

    override def write (obj: UUID): JsValue = {
      JsString(obj.toString)
    }
  }

}
