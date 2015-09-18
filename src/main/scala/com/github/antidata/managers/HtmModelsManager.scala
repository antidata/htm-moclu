package com.github.antidata.managers

import com.github.antidata.actors.HtmModelActor.HtmModel
import com.github.antidata.model.HtmModelId
import com.twitter.util.LruMap

object HtmModelsManager extends HtmModelsManager

trait HtmModelsManager {
  protected[this] lazy val cache: LruMap[HtmModelId, HtmModel] = new LruMap[HtmModelId, HtmModel](AppConfiguration.values.getInt("app.cache.size"))

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
  def updateModel(htmModel: HtmModel): Unit = {
    cache.update(HtmModelId(htmModel.HtmModelId), htmModel)
  }
  def init() {
    cache.maxSize
  }
}
