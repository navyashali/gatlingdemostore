package gatlingdemostore.pageobjects

import io.gatling.core.Predef._
import io.gatling.http.Predef._



object Customer {
  val loginFeeder = csv("data/loginDetails.csv").circular

  def login = {
    feed(loginFeeder)
      .exec(http("Load User Login Page")
        .get("/login")
        .check(status.is(200))
        .check(substring("Username:")))
      //.exec{session => println(session); session}

      .exec(http("Login_User")
        .post("/login")
        .formParam("_csrf", "${csrfValue}")
        .formParam("username", "${username}")
        .formParam("password", "${password}")
        .check(status.is(200))
        .resources(http("User_Home_Page")
          .get("/"))
      )
      .exec(session => session.set("customerLoggedIn", true))
    //.exec{session => println(session); session}
  }
}
