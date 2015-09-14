package com.github.antidata.model

import com.github.antidata.actors.messages.ClusterEvent

case class HtmModelEventData(HtmModelId: String, value: Double, timestamp: String) extends ClusterEvent

case class HtmModel(HtmModelId: String, data: List[HtmModelData], network: HtmModelNetwork) extends ClusterEvent

case class HtmModelData(HtmModelId: String, value: Double, timestamp: Long, anomalyScore: Option[Double]) extends ClusterEvent
