package gatlingdemostore.pageobjects

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Checkout {
  def viewCart = {
    doIf(session => !session("customerLoggedIn").as[Boolean]) {
      exec(Customer.login)
    }
      .exec(http("View_Cart")
        .get("/cart/view")
        .check(status.is(200))
        .check(css("#grandTotal").is("$#{cartTotal}")
        ))
  }

  def completeCheckout = {
    exec(http("Check_Out")
      .get("/cart/checkout")
      .check(status.is(200))
      .resources(http("Check_Out_Confirmation_Page")
        .get("/cart/checkoutConfirmation"))
      .check(substring("Thanks for your order! See you soon!"))
    )
  }
}
