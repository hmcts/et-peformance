package simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scenarios._
import utils.Environment
import io.gatling.http.Predef._

import scala.concurrent.duration._

class ET_Simulation extends Simulation {

  val BaseURL = Environment.baseURL
  val UserFeederET = csv("UserDataET.csv").circular

  /* TEST TYPE DEFINITION */
  /* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
  /* perftest (default) = performance test against the perftest environment */
  val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

  //set the environment based on the test type
  val environment = testType match{
    case "perftest" => "perftest"
    case "pipeline" => "perftest" //updated pipeline to run against perftest - change to aat to run against AAT
    case _ => "**INVALID**"
  }
  /* ******************************** */

  /* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
  val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
  val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
  /* ******************************** */


  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  before{
    println(s"Test Type: ${testType}")
    println(s"Test Environment: ${env}")
    println(s"Debug Mode: ${debugMode}")
  }

  val ETCreateClaim = scenario( "ETCreateClaim")
    .exitBlockOnFail {
 //   .repeat(1) {
      exec(  _.set("env", s"${env}"))
        .exec(flushHttpCache)
        .exec(flushCookieJar)
        .exec(flushSessionCookies)
        .repeat(1) {
          feed(UserFeederET)
          .exec(flushHttpCache)
            .exec(flushCookieJar)
            .exec(flushSessionCookies)
          .exec(ET_MakeAClaim.MakeAClaim)
            .exec(ET_MakeAClaimPt2.MakeAClaim)
        }

    }


    .exec {
      session =>
        println(session)
        session
    }



  //setUp(
  //  NFDCitizenSoleApp.inject(simulationProfile(testType, divorceRatePerSecSole, numberOfPipelineUsersSole)).pauses(pauseOption),
   // NFDCitizenJointApp.inject(simulationProfile(testType, divorceRatePerSecJoint, numberOfPipelineUsersJoint)).pauses(pauseOption)
  //).protocols(httpProtocol)
   // .assertions(assertions(testType))

  setUp(ETCreateClaim.inject(rampUsers(25).during(500)))
    .protocols(httpProtocol)
    .maxDuration(4400)

}
