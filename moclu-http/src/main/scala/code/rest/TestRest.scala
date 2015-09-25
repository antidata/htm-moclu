package code.rest

import code.managers.ClusterRefs
import com.github.antidata.actors.HtmModelActor._
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.{LiftResponse, JsonResponse, S}
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.http.rest.{RestContinuation, RestHelper}
import akka.pattern.ask
import akka.util.Timeout
import net.liftweb.util.Schedule
import scala.concurrent.{Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import net.liftweb.json.JsonAST.{JString, JDouble, JArray}
import net.liftweb.json.JsonDSL._

object TestRest extends RestHelper {
  implicit val timeout = akka.util.Timeout(10L, java.util.concurrent.TimeUnit.SECONDS)

  serve {
    case "create" :: id :: _ JsonPost json -> _ =>
      // TODO send max and min parameters
      val (min, max): (Option[Double], Option[Double]) = (json \ "min", json \ "max") match {
        case (JDouble(min), JDouble(max)) => Some(min) -> Some(max)
        case (_, JDouble(max)) => None -> Some(max)
        case (JDouble(max), _) => Some(max) -> None
        case _ => None -> None
      }

      val response: Promise[LiftResponse] = Promise()
      val responseFut = response.future

      RestContinuation.async(f => {
        val fut = (ClusterRefs.actorSystem ? CreateHtmModel(id)).mapTo[ClusterEvent]
        fut.onSuccess {
          case CreateModelOk(idM) =>
            f(
              JsonResponse(
                ("status" -> 200) ~ ("msg" -> s"Htm Model $idM created")
              )
            )
          case CreateModelFail(idM) =>
            f(
              JsonResponse(
                ("status" -> 302) ~ ("msg" -> s"Htm Model $idM exists")
              )
            )
          case _ =>
            f(
              JsonResponse(
                ("status" -> 302) ~ ("msg" -> s"Unexpected message from the System")
              )
            )
        }
        Schedule.schedule(() => {
          f(
            JsonResponse(
              ("status" -> 302) ~ ("msg" -> s"Cluster timeout")
            )
          )
        }, net.liftweb.util.Helpers.TimeSpan(10000L))
      })

    case "event" :: id :: _ JsonPost json -> _ =>
      val params: Option[(Double, String)] =
        (json \ "value", json \ "timestamp") match {
          case (JDouble(valueJ), JString(timestampJ)) => Some(valueJ -> timestampJ)
          case _ => None
        }

      if(params.isEmpty)
        JsonResponse(
          ("status" -> 302) ~ ("msg" -> s"Invalid request, expected {value:12, timestamp:'string'}")
        ) else {
        val (value, time): (Double, String) = params.get
        RestContinuation.async(f => {
          val fut = (ClusterRefs.actorSystem ? HtmModelEvent(id, HtmModelEventData(id, value, time))).mapTo[ClusterEvent]
          fut.onSuccess {
            case ModelPrediction(htmModelId, anomalyScore, prediction) =>
              f(
                JsonResponse(
                  ("status" -> 200) ~ ("msg" -> s"Applied event Htm Model $htmModelId") ~ ("data" -> (("id" -> htmModelId) ~ ("anomalyScore" -> anomalyScore) ~ ("prediction" -> prediction.toString)))
                )
              )
            case _ =>
              f(
                JsonResponse(
                  ("status" -> 302) ~ ("msg" -> s"Unexpected message from the System")
                )
              )
          }
          Schedule.schedule(() => {
            f(
              JsonResponse(
                ("status" -> 302) ~ ("msg" -> s"Cluster timeout")
              )
            )
          }, net.liftweb.util.Helpers.TimeSpan(10000L))
        })
      }

    case "getData" :: id :: _ JsonPost json -> _ =>

      val response: Promise[LiftResponse] = Promise()
      val responseFut = response.future

      RestContinuation.async(f => {
        val fut = (ClusterRefs.actorSystem ? HtmEventGetModel(id)).mapTo[ClusterEvent]
        fut.onSuccess {
          case GetModelData(id, data) =>
            f(
              JsonResponse(
                ("status" -> 200) ~ ("data" -> JArray(data.map { item =>
                  ("value" -> item.value) ~ ("timestamp" -> item.timestamp) ~ ("anomalyScore" -> item.anomalyScore.getOrElse(-1D))
                })))
              )
          case ModelNotFound(idM) =>
            f(
              JsonResponse(
                ("status" -> 302) ~ ("msg" -> s"Htm Model $idM exists")
              )
            )
          case _ =>
            f(
              JsonResponse(
                ("status" -> 302) ~ ("msg" -> s"Unexpected message from the System")
              )
            )
        }
        Schedule.schedule(() => {
          f(
            JsonResponse(
              ("status" -> 302) ~ ("msg" -> s"Cluster timeout")
            )
          )
        }, net.liftweb.util.Helpers.TimeSpan(10000L))
      })

    case e =>
      println(e.toString())
      JsonResponse(("status" -> 404) ~ ("msg"-> "unknown url"))
  }
}
