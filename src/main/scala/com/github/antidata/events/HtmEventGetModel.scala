package com.github.antidata.events

import com.github.antidata.actors.messages.ClusterEvent

case class HtmEventGetModel(HtmModelId: String) extends ClusterEvent
