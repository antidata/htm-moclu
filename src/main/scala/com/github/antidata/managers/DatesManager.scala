package com.github.antidata.managers

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DatesManager {
  lazy val defaultDatePattern = "MM/dd/YY HH:mm"
  lazy val dateTimeFormat = DateTimeFormat.forPattern(defaultDatePattern)
  def toDateTime(stringDate: String): DateTime = {
    dateTimeFormat.parseDateTime(stringDate)
  }
}
