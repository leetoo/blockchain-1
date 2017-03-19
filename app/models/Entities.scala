package models

import java.net.URL
import java.sql.Blob
import java.util.UUID

import play.api.libs.json.Json

trait Entity {
  def uuid: UUID
}

case class User(uuid: UUID, login: String, password: String) extends Entity
case class Credentials(login: String, password: String)

case class NewContract(name: String, sustomerUUID: UUID, customerName: String, price: String)
case class Contract(
   uuid: UUID,
   name: String,
   customerUUID: UUID,
   customerName: String,
   executors: Seq[Executor],
   maketUrl: String,
   price: String
) extends Entity

case class ContractByExecutor(uuid: UUID, contractName: String, executorUUID: UUID) extends Entity
case class ContractByCustomer(uuid: UUID, contractName: String, customerUUID: UUID) extends Entity

case class Result(url: String)
object Result {
  implicit val resultFormat = Json.format[Result]
}

object Contract {
  implicit val executorFormat = Json.format[Executor]
  implicit val newContractFormat = Json.format[NewContract]
  implicit val contractFormat = Json.format[Contract]
}

case class Executor(uuid: UUID, name: String)

object User {
  implicit val userFormat = Json.format[User]
}

object Credentials {
  implicit val credentialsFormat = Json.format[Credentials]
}

trait File {
  def name: String
}

case class ExpectedImage(uuid: UUID, name: String) extends File with Entity
case class ActualImage(uuid: UUID, name: String, url: String)
