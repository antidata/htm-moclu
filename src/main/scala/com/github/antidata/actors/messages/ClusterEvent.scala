package com.github.antidata.actors.messages

import com.github.antidata.model.HtmModelData

sealed trait ClusterEvent {
  val HtmModelId: String
}

case class CreateModelOk(HtmModelId: String) extends ClusterEvent

case class CreateModelFail(HtmModelId: String) extends ClusterEvent

case class GetModelData(HtmModelId: String, data: List[HtmModelData]) extends ClusterEvent

case class ModelNotFound(HtmModelId: String) extends ClusterEvent

case class ModelPrediction(HtmModelId: String, anomalyScore: Double) extends ClusterEvent
