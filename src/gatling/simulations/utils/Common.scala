package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object Common {

  val rnd = new Random()
  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternDate = DateTimeFormatter.ofPattern("yyyyMMdd")
  val BaseURL = Environment.baseURL

  val CommonHeader = Environment.commonHeader
  val PostHeader = Environment.postHeader

  val postcodeFeeder = csv("postcodes.csv").circular


  def postcodeLookup() =
    feed(postcodeFeeder)
      .exec(http("XUI_Common_000_PostcodeLookup")
        .post(BaseURL + "/address-lookup")
        .headers(PostHeader)
      //  .formParam("postcode", "${postcode}")
        .formParam("postcode", "${postcode}")
        .formParam("_csrf", "${csrf}"))
       // .check(regex(""""(street1)_.+" : "(.+?)",(?s).*?"(?:street2)" : "(.+?)",.*?"town" : "(.+?)",.*?"country" : "(.+?)"""")
      //    .check(regex("""^"street1:"(.*\,$)""")
       // .ofType[(String, String, String, String)].findRandom.saveAs("addressLines")))
       // .check(regex("""<option value="([0-9]+)">""").findRandom.saveAs("addressIndex")))

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomNumber(length: Int) = {
    rnd.alphanumeric.filter(_.isDigit).take(length).mkString
  }

  def getDate(): String = {
    now.format(patternDate)
  }

  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString.format(patternDay).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternMonth).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  //Date of Birth >= 35 years
  def getDobYear(): String = {
    now.minusYears(20 + rnd.nextInt(55)).format(patternYear)
  }

  //Date of Birth <= 18 years
  def getJobStartDate(): String = {
    now.minusYears(2 + rnd.nextInt(15)).format(patternYear)
  }

  //Date of Birth <= 18 years
  def getNoticeEndDate(): String = {
    now.plusYears(1).format(patternYear)
  }

  def getPostcode(): String = {
    randomString(2).toUpperCase() + rnd.nextInt(10).toString + " " + rnd.nextInt(10).toString + randomString(2).toUpperCase()
  }

}