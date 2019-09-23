package com.github.antidata.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.github.antidata.actors.HtmModelActor._
import com.github.antidata.managers.HtmModelsManager
import com.github.antidata.model.HtmModelId
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps


class HtmModelActorTest extends TestKit(ActorSystem("HtmModelActorTest")) with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {
  val modelId = "testModel"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "HtmModelActor" must {

    "Create a new model and add it to the HtmModelsManager" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      within(10.seconds) {
        probe.send(htmModelActorRef, CreateHtmModel(modelId))
        probe.expectMsg(10.seconds, CreateModelOk(modelId))

        HtmModelsManager.getModel(HtmModelId(modelId)).isDefined shouldBe true
      }
    }

    "Get model data with actor" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      probe.send(htmModelActorRef, CreateHtmModel(modelId))
      probe.expectMsg(10.seconds, CreateModelOk(modelId))

      probe.send(htmModelActorRef, HtmEventGetModel(modelId))
      probe.expectMsg(10.seconds, GetModelData(modelId, List()))
    }

    "Get unknown model data with actor should return NotFound" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      probe.send(htmModelActorRef, HtmEventGetModel("UnknownModelId"))
      probe.expectMsg(10.seconds, ModelNotFound("UnknownModelId"))
    }

    "Add model with actor and new event to get the ModelPrediction" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      probe.send(htmModelActorRef, CreateHtmModel(modelId))
      probe.expectMsg(10.seconds, CreateModelOk(modelId))

      probe.send(htmModelActorRef, HtmModelEvent(modelId, HtmModelEventData(modelId, "10,1,-1,'7/2/10 1:16',1,-1", "10")))
      probe.expectMsg(10.seconds, ModelPrediction(modelId, 1D, 1))
    }

    "Add model with actor and verify new event in HtmModelsManager" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      probe.send(htmModelActorRef, CreateHtmModel(modelId))
      probe.expectMsg(10.seconds, CreateModelOk(modelId))

      within(10 seconds) {
        probe.send(htmModelActorRef, HtmModelEvent(modelId, HtmModelEventData(modelId, "10,1,-1,'7/2/10 1:16',1,-1", "10")))
        probe.expectMsg(10.seconds, ModelPrediction(modelId, 1D, 1))
        HtmModelsManager.getModel(HtmModelId(modelId)).exists(_.data.exists(dm => dm.timestamp == 0L && dm.HtmModelId == modelId)) shouldBe true
      }
    }

    "Sending Event data for a model that wasn't initialized by the Manager should result in a DelayedHtmModelEvent" in {
      val probe = TestProbe()
      val htmModelActorRef = system.actorOf(Props(classOf[HtmModelActor]))

      probe.send(htmModelActorRef, HtmModelEvent(modelId, HtmModelEventData(modelId, "10,1,-1,'7/2/10 1:16',1,-1", "10")))
      probe.expectMsg(10.seconds, ModelNotFound(modelId))
    }
  }
}