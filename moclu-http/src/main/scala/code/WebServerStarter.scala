package code

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.handler.{ContextHandler, HandlerCollection}

// See http://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
object WebServerStarter extends App {
  val serverInstance = new Server

  val channelConnector = new SelectChannelConnector
  val serverPort = Option(System.getProperty("WEBSERVERPORT")).getOrElse("8080").toInt
  channelConnector.setHost(Option(System.getProperty("HOST")).getOrElse("127.0.0.1"))
  channelConnector.setPort(serverPort)
  serverInstance.setConnectors(Array(channelConnector))

  val context = new WebAppContext(getClass.getClassLoader.getResource("webapp").toExternalForm, "/")
  val contextHandler = new ContextHandler
  val handlers = new HandlerCollection

  contextHandler.setHandler(context)
  handlers.setHandlers(Array(contextHandler))

  serverInstance.setHandler(handlers)
  serverInstance.start
  serverInstance.join
}
