package com.github.antidata.model

case class HtmModelEventData(modelId: HtmModelId, value: Double, timestamp: String)

case class HtmModel(id: HtmModelId, data: List[HtmModelData], network: HtmModelNetwork)

case class HtmModelData(value: Double, timestamp: Long, anomalyScore: Option[Double])
