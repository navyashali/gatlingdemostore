package gatlingdemostore

import scala.concurrent.duration._
import gatlingdemostore.pageobjects._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random

class DemostoreSimulation extends Simulation {

  val domain = "demostore.gatling.io"
  val httpProtocol = http
    .baseUrl("https://" + domain)

  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

//  val categoryFeeder = csv("data/categoryDetails.csv").random
//  val jsonFeeder = jsonFile("data/productDetails.json").random
//  val loginFeeder = csv("data/loginDetails.csv").circular

  val rnd = new Random()

  def randomString(length: Int): String = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  before{
    println(s"Running tests with ${userCount} users")
    println(s"Running users over ${rampDuration} seconds")
    println(s"Total tests duration: ${testDuration} seconds")
  }

  after{
    println("Stress testing complete")
  }

  val initSession = exec(flushCookieJar)
    .exec(session => session.set("randomNumber", rnd.nextInt))
    .exec(session => session.set("customerLoggedIn", false))
    .exec(session => session.set("cartTotal", 0.00))
    .exec(addCookie(Cookie("sessionId", randomString(10)).withDomain(domain)))
  //.exec{session => println(session); session}

  val scn = scenario("DemostoreSimulation")
    .exec(initSession)
    .exec(CmsPage.homepage)
    .pause(2)
    .exec(CmsPage.aboutUs)
    .pause(2)
    .exec(Catelog.Category.view)
    .pause(2)
    .exec(Catelog.Product.add)
    .pause(2)
    .exec(Checkout.viewCart)
    .pause(2)
    .exec(Checkout.viewCart)
    .pause(2)
    .exec(Checkout.completeCheckout)

  object UserJourneys {
    def minPause = 100.milliseconds

    def maxPause = 500.milliseconds

    def browseStore = {
      exec(initSession)
        .exec(CmsPage.homepage)
        .pause(maxPause)
        .exec(CmsPage.aboutUs)
        .pause(minPause, maxPause)
        .repeat(5) {
          exec(Catelog.Category.view)
            .pause(minPause, maxPause)
            .exec(Catelog.Product.view)
        }
    }

    def abandonCart = {
      exec(initSession)
        .exec(CmsPage.homepage)
        .pause(maxPause)
        .exec(Catelog.Category.view)
        .pause(minPause, maxPause)
        .exec(Catelog.Product.view)
        .pause(minPause, maxPause)
        .exec(Catelog.Product.add)
    }

    def completePurchase = {
      exec(initSession)
        .exec(CmsPage.homepage)
        .pause(maxPause)
        .exec(Catelog.Category.view)
        .pause(minPause, maxPause)
        .exec(Catelog.Product.view)
        .pause(minPause, maxPause)
        .exec(Catelog.Product.add)
        .pause(minPause, maxPause)
        .exec(Checkout.viewCart)
        .pause(minPause, maxPause)
        .exec(Checkout.completeCheckout)
    }
  }

  object Scenarios {
    def default = scenario("Default Load Test")
      .during(60.seconds) {
        randomSwitch(
          75d -> exec(UserJourneys.browseStore),
          15d -> exec(UserJourneys.abandonCart),
          10d -> exec(UserJourneys.completePurchase)
        )
      }

    def highPurchase = scenario("High Purchase Load Test")
      .during(60.seconds) {
        randomSwitch(
          25d -> exec(UserJourneys.browseStore),
          25d -> exec(UserJourneys.abandonCart),
          50d -> exec(UserJourneys.completePurchase)
        )
      }
  }

  setUp(
    Scenarios.default
      .inject(rampUsers(userCount) during (rampDuration.seconds)).protocols(httpProtocol),
    Scenarios.highPurchase
      .inject(rampUsers(5) during (10.seconds)).protocols(httpProtocol)
  )
}
//	setUp(scn.inject(
    //open
//    atOnceUsers(3),
//    nothingFor(5.seconds),
//    rampUsers(10).during(20.seconds),
//    nothingFor(10.seconds),
//    constantUsersPerSec(1).during(20.seconds)
    //closed
//    constantConcurrentUsers(10) during(20.seconds),
//    rampConcurrentUsers(10)to(20) during(20.seconds)
    //throttle
//    constantUsersPerSec(1)during(3.minutes))).protocols(httpProtocol).throttle(
//    reachRps(10)in(30.seconds),
//    holdFor(60.seconds),
//    jumpToRps(20),
//    holdFor(60.seconds)
//  ).maxDuration(3.minutes)

