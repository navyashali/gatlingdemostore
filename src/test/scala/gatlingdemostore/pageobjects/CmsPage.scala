package gatlingdemostore.pageobjects

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object CmsPage {
  def homepage = {
    exec(http("Load_Home_Page")
      .get("/")
      .check(status.is(200))
      .check(regex("<title>Gatling Demo-Store</title>").exists)
      .check(css("#_csrf", "content").saveAs("csrfValue")))
  }

  def aboutUs = {
    exec(http("Load_AboutUs_Page")
      .get("/about-us")
      .check(status.is(200))
      .check(substring("About Us")))
  }
}
