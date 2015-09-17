package com.github.antidata.events

import com.github.antidata.actors.messages.ClusterEvent
import com.github.antidata.model.HtmModelEventData

case class HtmModelEvent(HtmModelId: String, htmModelEventData: HtmModelEventData) extends ClusterEvent
