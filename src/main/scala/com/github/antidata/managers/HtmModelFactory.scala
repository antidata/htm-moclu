package com.github.antidata.managers

import com.github.antidata.model.HtmModelNetwork
import com.github.antidata.settings.NetworkParameters
import org.numenta.nupic.Parameters.KEY
import org.numenta.nupic.Parameters
import org.numenta.nupic.algorithms.{TemporalMemory, SpatialPooler, Anomaly}
import org.numenta.nupic.network.Network
import org.numenta.nupic.network.sensor.SensorParams.Keys
import org.numenta.nupic.network.sensor._

trait HtmPublisher {
  def getPublisher: Publisher
}

trait HtmModelFactory {
  //self: HtmPublisher =>
  def apply(params: Option[Parameters] = None): HtmModelNetwork
}

object HtmModelFactory extends HtmModelFactory {
  def apply(params: Option[Parameters] = None): HtmModelNetwork = {
    val modelParameters = NetworkParameters.getModelParameters
    val publisher = getPublisher
    val parms: SensorParams = SensorParams.create(Keys.obs.get(), "", publisher)
    val sensor: Sensor[ObservableSensor[String]] = Sensor.create(sensorFactory, parms)
    // TODO Make this parametric
    val net = Network.create("Value Network", modelParameters)
      .add(Network.createRegion("Region 1")
      .add(Network.createLayer("Layer 2/3", modelParameters)
      .alterParameter(KEY.AUTO_CLASSIFY, true)
      .add(Anomaly.create)
      .add(new TemporalMemory)
      .add(new SpatialPooler)
      .add(sensor)))

    net.start()

    HtmModelNetwork(net, publisher, sensor)
  }

  object sensorFactory extends SensorFactory[ObservableSensor[String]] {
    override def create(sensorParams: SensorParams): Sensor[ObservableSensor[String]] = ObservableSensor.create(sensorParams)
  }

  def getPublisher: Publisher = {
    Publisher
      .builder
      .addHeader("timestamp,consumption")
      .addHeader("datetime,float")
      .addHeader("T, ")
      .build
  }
}

object HtmGeoModelFactory extends HtmModelFactory with HtmPublisher {
  def apply(params: Option[Parameters] = None): HtmModelNetwork = {
    val modelParameters = NetworkParameters.getModelParameters
    val publisher = getPublisher
    val parms: SensorParams = SensorParams.create(Keys.obs.get(), "", publisher)
    val sensor: Sensor[ObservableSensor[String]] = Sensor.create(HtmModelFactory.sensorFactory, parms)
    val net = Network.create("Geo-Network", modelParameters)
      .add(Network.createRegion("Region 1")
      .add(Network.createLayer("Layer 2/3", modelParameters)
      .add(Anomaly.create)
      .add(new TemporalMemory)
      .add(new SpatialPooler)
      .add(sensor)))

    net.start()
    HtmModelNetwork(net, publisher, sensor)
  }

  def getPublisher = Publisher.builder.addHeader("timestamp,consumption,location").addHeader("datetime,float,geo").addHeader("T,,").build
}
