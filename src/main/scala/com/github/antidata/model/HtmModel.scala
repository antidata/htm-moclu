package com.github.antidata.model

import org.numenta.nupic.network.Network
import org.numenta.nupic.network.sensor.{Sensor, Publisher}

case class HtmModelNetwork(net: Network, publisher: Publisher, sensor: Sensor[_])