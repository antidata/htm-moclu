package com.github.antidata.managers

import com.github.antidata.model.{HtmModelNetwork, HtmModel}
import com.github.antidata.settings.NetworkParameters
import org.numenta.nupic.Parameters
import org.numenta.nupic.Parameters.KEY
import org.numenta.nupic.algorithms.{TemporalMemory, SpatialPooler, Anomaly}
import org.numenta.nupic.network.{Inference, Network}
import org.numenta.nupic.network.sensor.SensorParams.Keys
import org.numenta.nupic.network.sensor._
import rx.Subscriber

object HtmModelFactory extends HtmModelFactory {

}

trait HtmModelFactory {
  def apply(): HtmModelNetwork = {
    val modelParameters = NetworkParameters.getModelParameters
    val publisher = getPublisher
    val parms: SensorParams = SensorParams.create(Keys.obs.get(), "", publisher)
    val sensor: Sensor[ObservableSensor[String]] = Sensor.create(sensorFactory, parms)
    // TODO Make this parametric
    val net = Network.create("Network API Demo", modelParameters)
      .add(Network.createRegion("Region 1")
      .add(Network.createLayer("Layer 2/3", modelParameters)
      .alterParameter(KEY.AUTO_CLASSIFY, true)
      .add(Anomaly.create)
      .add(new TemporalMemory)
      .add(new SpatialPooler)
      .add(sensor)))

    net.observe().subscribe(new Subscriber[Inference]() {
      def onCompleted() {
        System.out.println("Stream completed, see output: ")
      }

      def onError(e: Throwable) {
        e.printStackTrace()
      }

      def onNext(i: Inference) {
        //(i, "consumption")
        val res = s"${i.getRecordNum}, ${i.getClassifierInput.get("consumption").get("inputValue")}, ${i.getAnomalyScore}"
        println(res)
      }
    })

    net.start()

    HtmModelNetwork(net, publisher, sensor)
  }

  object sensorFactory extends SensorFactory[ObservableSensor[String]] {
    override def create(sensorParams: SensorParams): Sensor[ObservableSensor[String]] = ObservableSensor.create(sensorParams)
  }

  def getPublisher: Publisher = {
    Publisher
      .builder
      .addHeader("timestamp,value")
      .addHeader("datetime,float")
      .addHeader("T, ")
      .build
  }
}