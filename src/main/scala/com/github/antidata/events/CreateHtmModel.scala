package com.github.antidata.events

import com.github.antidata.actors.messages.ClusterEvent

case class CreateHtmModel(HtmModelId: String) extends ClusterEvent

