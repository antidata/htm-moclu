package com.github.antidata.model

case class HtmModelEventData(modelId: HtmModelId, value: Double, timestamp: Long)

case class HtmModel(id: HtmModelId, data: List[HtmModelData])

case class HtmModelData(value: Double, timestamp: Long, anomalyScore: Option[Double])
