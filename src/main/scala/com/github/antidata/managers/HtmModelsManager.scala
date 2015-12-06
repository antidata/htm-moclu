package com.github.antidata.managers

import akka.actor._
import com.github.antidata.actors.HtmModelActor.{HtmModelData, HtmModel}
import com.github.antidata.model.HtmModelId

object HtmModelsManager {
  trait  HtmModelsManagerEvent
  case class GetModel(htmModelId: HtmModelId) extends HtmModelsManagerEvent
  case class ModelResponse(modelOption: Option[HtmModel]) extends HtmModelsManagerEvent
  case class AddModel(htmModel: HtmModel) extends HtmModelsManagerEvent
  case class AddModelId(id: String) extends HtmModelsManagerEvent
  case class UpdateModel(htmModel: HtmModel) extends HtmModelsManagerEvent
  case class ResetNetwork(htmModel: String) extends HtmModelsManagerEvent
  case class EditNetworkLearning(htmModel: String, learning: Boolean) extends HtmModelsManagerEvent

  private[this] lazy val system = ActorSystem("HtmModelsSystem")
  lazy val actorInstance = new HtmModelsManagerActor
  private[this] lazy val managerActor: ActorRef = system.actorOf(Props(actorInstance), "HtmModelsManagerActor")

  def apply(): ActorRef = managerActor
}

class HtmModelsManagerActor extends Actor with ActorLogging {
  import HtmModelsManager._
  protected[this] lazy val cache: scala.collection.mutable.Map[HtmModelId, HtmModel] = scala.collection.mutable.Map[HtmModelId, HtmModel]()
  cache // Constructor init
  def receive = {
    case e@GetModel(htmModelId) =>
      sender() ! ModelResponse(getModel(htmModelId))

    case e@AddModel(htmModel) =>
      addModel(htmModel)

    case e@AddModelId(id) =>
      addModel(id)

    case e@UpdateModel(htmModel) =>
      updateModel(htmModel)

    case e@ResetNetwork(id) =>
      resetNetwork(id)

    case e@EditNetworkLearning(id, learning) =>
      editNetworkLearning(id, learning)
  }

  def getModel(htmModelId: HtmModelId): Option[HtmModel] = cache.get(htmModelId)
  def addModel(htmModel: HtmModel): Option[String] = {
    cache.get(HtmModelId(htmModel.HtmModelId)) match {
      case Some(model) =>
        Some(s"Model ${htmModel.HtmModelId} is already in cache")

      case None =>
        cache += (HtmModelId(htmModel.HtmModelId) -> htmModel)
        None
    }
  }
  def addModel(id: String) {
    cache.get(HtmModelId(id)) match {
      case None =>
        val htmModel = HtmModelFactory()
        cache += (HtmModelId(id) -> HtmModel(id, Nil, htmModel))
        None
      case _ =>
        // Already in cache
    }
  }
  def updateModel(htmModel: HtmModel): Unit = {
    getModel(HtmModelId(htmModel.HtmModelId)).foreach { model =>
      val updatedList = model.data ++ htmModel.data
      cache.update(HtmModelId(htmModel.HtmModelId), htmModel.copy(data = updatedList))
    }
  }

  def size = cache.size

  def resetNetwork(htmModelId: String): Unit = {
    cache.get(HtmModelId(htmModelId)).foreach(_.network.net.reset())
  }

  def editNetworkLearning(htmModelId: String, learning: Boolean): Unit = {
    cache.get(HtmModelId(htmModelId)).foreach(_.network.net.setLearn(learning))
  }
}
