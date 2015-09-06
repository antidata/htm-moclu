package com.github.antidata.managers

import com.github.antidata.model.{HtmModelId, HtmModel}
import com.twitter.util.LruMap

object HtmModelsManager extends HtmModelsManager

trait HtmModelsManager {
  protected[this] lazy val cache: LruMap[HtmModelId, HtmModel] = new LruMap[HtmModelId, HtmModel](AppConfiguration.values.getInt("app.cache.size"))

  def getModel(htmModelId: HtmModelId): Option[HtmModel] = cache.get(htmModelId)
  def addModel(htmModel: HtmModel): Option[String] = {
    cache.get(htmModel.id) match {
      case Some(model) =>
        Some(s"Model ${htmModel.id} is already in cache")

      case None =>
        cache += (htmModel.id -> htmModel)
        None
    }
  }
  def updateModel(htmModel: HtmModel): Unit = {
    cache.update(htmModel.id, htmModel)
  }
  def init() {
    cache.maxSize
  }
}
