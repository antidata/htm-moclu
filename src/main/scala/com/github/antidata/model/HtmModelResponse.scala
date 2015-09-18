package com.github.antidata.model

import com.github.antidata.actors.HtmModelActor.HtmModel

trait HtmModelResponse {
  val msg : String
}

case class HtmStringResponse(msg: String) extends HtmModelResponse

case class HtmModelEventsData(msg: String, events: HtmModel) extends HtmModelResponse
