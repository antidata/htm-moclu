package com.github.antidata.managers

import com.github.antidata.actors.HtmModelActor.HtmModelEventData
import com.github.antidata.model.{HtmModelEventsData, HtmStringResponse, HtmModelResponse}

object HtmModelManager extends HtmModelManager

trait HtmModelManager extends HtmModelEventPostManager with HtmModelEventGetManager {
  def sendEventData(modelEventData: HtmModelEventData) = HtmStringResponse("")
  def getEventsData(id: String) = HtmModelEventsData("", null)
}

trait HtmModelEventPostManager {
  def sendEventData(modelEventData: HtmModelEventData): HtmModelResponse
}

trait HtmModelEventGetManager {
  def getEventsData(id: String): HtmModelResponse
}
