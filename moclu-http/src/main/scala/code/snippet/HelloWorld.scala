package code.snippet

import net.liftweb._
import common._
import util._
import Helpers._

object HelloWorld extends SimpleInjector {

  lazy val date = new Inject(() => Full(Helpers.now)) {}

  // replace the contents of the element with id "time" with the date
   def render = {
    "#time *" #> date.vend.map(_.toString)
  }
}
