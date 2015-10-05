package com.github.antidata.managers

import com.github.antidata.actors.HtmModelActor.{HtmModelData, HtmModel}
import com.github.antidata.model.HtmModelId
import com.twitter.util.LruMap

object HtmModelsManager extends HtmModelsManager

trait HtmModelsManager {
  protected[this] lazy val cache: scala.collection.mutable.Map[HtmModelId, HtmModel] = scala.collection.mutable.Map[HtmModelId, HtmModel]()
  //protected[this] lazy val cache: LruMap[HtmModelId, HtmModel] = new LruMap[HtmModelId, HtmModel](AppConfiguration.values.getInt("app.cache.size"))

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
  def init() {
    cache
  }
  def size = cache.size
}
