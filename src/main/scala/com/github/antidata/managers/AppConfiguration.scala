package com.github.antidata.managers

import com.typesafe.config.ConfigFactory

object AppConfiguration extends AppConfiguration

trait AppConfiguration {
  lazy val values = ConfigFactory.load()
}
