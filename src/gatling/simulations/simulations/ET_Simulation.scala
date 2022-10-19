package simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import scenarios._
import utils.Environment
import io.gatling.http.Predef._

import scala.concurrent.duration._

class ET_Simulation extends Simulation {

  val BaseURL = Environment.baseURL
  val UserFeederET1 = csv("UserDataET1.csv").circular
  val UserFeederET2 = csv("UserDataET2.csv").circular
  val UserFeederET3 = csv("UserDataET3.csv").circular

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

  val ETCreateClaim1 = scenario( "ETCreateClaim1")
    .exitBlockOnFail {
 //   .repeat(1) {
      exec(  _.set("env", s"${env}"))
        .repeat(15) {
          feed(UserFeederET1)
          .exec(ET_MakeAClaim.MakeAClaim)
            .exec(ET_MakeAClaimPt2.MakeAClaim)
        }
    }

  val ETCreateClaim2 = scenario( "ETCreateClaim2")
    .exitBlockOnFail {
      //   .repeat(1) {
      exec(  _.set("env", s"${env}"))
        .repeat(15) {
          feed(UserFeederET2)
            .exec(ET_MakeAClaim.MakeAClaim)
            .exec(ET_MakeAClaimPt2.MakeAClaim)
        }
    }

  val ETCreateClaim3 = scenario( "ETCreateClaim3")
    .exitBlockOnFail {
      //   .repeat(1) {
      exec(  _.set("env", s"${env}"))
        .repeat(15) {
          feed(UserFeederET3)
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

  setUp(ETCreateClaim1.inject(rampUsers(1).during(1)))
    .protocols(httpProtocol)
    .maxDuration(3600)

 /* setUp(ETCreateClaim2.inject(rampUsers(1).during(1)))
    .protocols(httpProtocol)
    .maxDuration(4400)

  setUp(ETCreateClaim3.inject(rampUsers(1).during(1)))
    .protocols(httpProtocol)
    .maxDuration(4400)

  */

}
