
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model.{HttpEntity, StatusCodes, _}
import akka.http.scaladsl.server._
import StatusCodes._
import akka.http.scaladsl.model.HttpEntity
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import play.api.libs.json.{ Json, OWrites}

import scala.io.StdIn

case class ProperResponse(code: Int, message: String)
object ProperResponse {
  implicit val ProperResponseWrites: OWrites[ProperResponse] = Json.writes[ProperResponse]
}

case class ErrorResponse(
                          code: Int,
                          message: String,
                          reason: String
                        )

object ErrorResponse {
  implicit val ErrorResponseWrites: OWrites[ErrorResponse] = Json.writes[ErrorResponse]
}

object WebServer {

  def main(args: Array[String]) {
    implicit val system = ActorSystem("rejection-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher



    implicit def rejectionHandler =
      RejectionHandler.newBuilder()
        .handle {
          case MissingQueryParamRejection(param) =>
            complete(HttpResponse(BadRequest,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
              Json.toJson(
                ErrorResponse(
                  BadRequest.intValue, "MissingQueryParamRejection", s"Request has missing required query parameter for = '$param")
              )
            )
            )))
        }
        .handle { case AuthorizationFailedRejection =>
          complete(HttpResponse(BadRequest,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
            Json.toJson(
              ErrorResponse(
                BadRequest.intValue, "AuthorizationFailedRejection", "You have entered invalid credentials")
            )
          )
          )))
        }
        .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete(HttpResponse(MethodNotAllowed,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
          Json.toJson(
            ErrorResponse(
              MethodNotAllowed.intValue, "Method Rejection", s"Method not supported! Supported for : ${names mkString "or"}!"))
        )
        )))
      }
        .handleNotFound {
        complete(HttpResponse(NotFound,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
            Json.toJson(
              ErrorResponse(
                NotFound.intValue, "NotFound", "The requested resource could not be found.")
            )
          )
        )))
      }
        .result()


    val route: Route =
      path("hello") {
        get {
          complete(HttpResponse(OK,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
            Json.toJson(
              ProperResponse(
                OK.intValue,"Hello world")))
          )))}
      } ~ path("paint") {
        parameters('color, 'bgColor) {
          (color, bgColor) =>
            complete(HttpResponse(NotFound,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
              Json.toJson(
                ProperResponse(
                  OK.intValue, s"You mention color is $color and background color is $bgColor")))
            )))}
      }~ path("login") {
          parameters('username, 'password) {
            (username, password) =>
              if (username.equals("knoldus") && password.equals("pune")) {
                complete(HttpResponse(NotFound,   entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(
                  Json.toJson(
                    ProperResponse(
                      OK.intValue, "Login Successful")))
                )))}
               else {
                reject(AuthorizationFailedRejection)
              }
          }
        }

        val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

        println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
        StdIn.readLine() // let it run until user presses return
        bindingFuture
          .flatMap(_.unbind()) // trigger unbinding from the port
          .onComplete(_ => system.terminate()) // and shutdown when done
      }
  }
