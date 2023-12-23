package gatlingdemostore.pageobjects

import io.gatling.core.Predef._
import io.gatling.http.Predef._



object Catelog {
  val categoryFeeder = csv("data/categoryDetails.csv").random
  val jsonFeeder = jsonFile("data/productDetails.json").random

  object Category {
    def view = {
      feed(categoryFeeder)
        .exec(http("Load_Category_Page_${categoryName}")
          .get("/category/${categorySlug}")
          .check(status.is(200))
          .check(css("#CategoryName").is("${categoryName}")))
    }
  }

  object Product {
    def view = {
      feed(jsonFeeder)
        .exec(http("Load_Product_Page_${name}")
          .get("/product/${slug}")
          .check(status.is(200))
          .check(css("#ProductDescription").is("${description}"))
        )
    }

    def add = {
      exec(view)
        .exec(http("Add_Product_to_Cart")
          .get("/cart/add/${id}")
          .check(status.is(200))
          .check(substring("items in your cart")))
        .exec(session => {
          val currentTotal = session("cartTotal").as[Double]
          val itemPrice = session("price").as[Double]
          session.set("cartTotal", (currentTotal + itemPrice))
        })
      //.exec{session => println(session); session}
    }
  }
}
