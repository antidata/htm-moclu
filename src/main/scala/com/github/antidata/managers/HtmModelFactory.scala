package com.github.antidata.managers

import com.github.antidata.model.{HtmModelNetwork, HtmModel}
import org.numenta.nupic.Parameters
import org.numenta.nupic.Parameters.KEY
import org.numenta.nupic.algorithms.Anomaly
import org.numenta.nupic.network.{Inference, Network}
import org.numenta.nupic.network.sensor.SensorParams.Keys
import org.numenta.nupic.network.sensor._
import rx.Subscriber

object HtmModelFactory extends HtmModelFactory {

}

trait HtmModelFactory {
  def apply(): HtmModelNetwork = {
    // TODO Create model
    HtmModelNetwork(null, null, null)
  }
}