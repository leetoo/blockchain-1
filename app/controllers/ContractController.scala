package controllers


import java.util.UUID
import javax.inject.{Inject, Singleton}

import epsilon.Api
import play.api._
import play.api.mvc._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils._
import pdi.jwt.JwtPlayImplicits
import models.{Contract, Credentials, User}
import pdi.jwt.JwtPlayImplicits
import models.Contract._

import scala.concurrent.Future

@Singleton
class ContractController @Inject extends Controller {
  import play.api.libs.concurrent.Execution.Implicits._

  def create = Action.async(parse.tolerantText) { implicit request =>
    Contract.newContractFormat.reads(Json.parse(request.body)).fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      newContract => {
        Api.contractOps.create(newContract).map(c => Ok(Contract.contractFormat.writes(c)))
      })
  }

  def update = Action.async(parse.tolerantText) { implicit request =>
    Contract.contractFormat.reads(Json.parse(request.body)).fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      newContract => {
        Api.contractOps.update(newContract)
          .map(_.map(c => Ok(Contract.contractFormat.writes(c)))
            .getOrElse(BadRequest("Cannot update contract")))
      })
  }

  def getAll = Action.async { implicit request =>
    Api.contractOps.getAll().map(all => Ok(Json.toJson(all)))
  }

  def getAllForCustomer(uuid: String) = Action.async(parse.tolerantText) { implicit request =>
        Api.contractOps.getAllForCustomer(UUID.fromString(uuid))
          .map(c => Ok(Json.toJson(c)))
  }

  def getAllForExecutor(uuid: String) = Action.async(parse.tolerantText) { implicit request =>
    Api.contractOps.getAllForExecutor(UUID.fromString(uuid))
      .map(c => Ok(Json.toJson(c)))
  }
}
