package controllers

import javax.inject._

import epsilon.Api
import play.api._
import play.api.mvc._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils._
import pdi.jwt.JwtPlayImplicits
import models.{Credentials, User}
import pdi.jwt.JwtPlayImplicits
import models.User._

import scala.concurrent.Future

@Singleton
class UserCreatorController @Inject extends Controller
  with JwtPlayImplicits {
  import play.api.libs.concurrent.Execution.Implicits._

  def create = Action.async(parse.tolerantText) { implicit request =>
    Credentials.credentialsFormat.reads(Json.parse(request.body)).fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      credentials => {
        Api.userOps.create(credentials)
          .map(_.map(user => Ok(User.userFormat.writes(user)).addingToJwtSession(
            "userId" -> user.uuid.toString,
            "login" -> user.login,
            "password" -> user.password
          )).getOrElse(BadRequest("User with such credentials already exists.")))
      }
    )
  }
}
