package controllers

import java.util.UUID
import javax.inject._

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import epsilon.Api
import models.{Credentials, User}
import play.api._
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class FileUploader @Inject extends Controller {
  import collection.JavaConverters._

  import play.api.libs.concurrent.Execution.Implicits._

  def uploadMaket(uuid: String) = Action.async(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(new File(s"/tmp/picture/$filename"))
      val base64 = BaseEncoding.base64()
        .encode(scala.io.Source.fromFile("file.txt").mkString.getBytes(Charsets.UTF_8))
      import _root_.data.utils.CommonConverters.Futures._
      ResultSetFutureToScalaFuture(Api.cassandraSession.session.executeAsync(
        QueryBuilder.insertInto("finished_contract").values(
          Array("id", "maket"), Array[AnyRef](UUID.fromString(uuid), base64)
        )
      )).map( r => Ok("22"))
    }.getOrElse(Future.successful(InternalServerError(s"Unable to upload file")))
  }

  import play.api.libs.concurrent.Execution.Implicits._
  def uploadResult(uuid: String) = Action.async(parse.tolerantText) { implicit request =>
    models.Result.resultFormat.reads(Json.parse(request.body)).fold(
      errors => {
        Future.successful(BadRequest(JsError.toJson(errors)))
      },
      result => {
        import _root_.data.utils.CommonConverters.Futures._
        ResultSetFutureToScalaFuture(Api.cassandraSession.session.executeAsync(
          QueryBuilder.insertInto("finished_contract").values(
            Array("id","result_url"), Array[AnyRef](UUID.fromString(uuid), result.url)
          )
        )).map( r => Ok("22"))
      }
    )
  }
}
