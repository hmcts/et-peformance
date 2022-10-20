package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{AuthCheck, Common, CsrfCheck, Environment}

import scala.concurrent.duration._
import scala.util.Random
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random


object ET_MakeAClaim {

  val BaseURL = Environment.baseURL
  val IdamURL = Environment.idamURL


  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader

  val postcodeFeeder = csv("postcodes.csv").circular

  val MakeAClaim = {

    /*
    exec(flushHttpCache)
    .exec(flushCookieJar)
    .exec(flushSessionCookies)

     */

  exec(_.setAll(
      "ETRandomString" -> (Common.randomString(7))))

  //.exec(getCookieValue(CookieKey("et-sya-session,et-sya.perftest.platform.hmcts.net,/")))

    /*======================================================================================
    * Load the home page
    ======================================================================================*/

    .group("ET_010_Home") {

      exec(http("ET_010_005_Home")
        .get(BaseURL)
        .headers(CommonHeader)
        .header("sec-fetch-site", "none")
        .check(substring("Make a claim to an employment tribunal")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*======================================================================================
    * Click on 'Start now'
    ======================================================================================*/

    .group("ET_020_Start") {
      exec(http("ET_020_005_Start")
        .get(BaseURL + "/checklist")
        .headers(CommonHeader)
        .check(substring("Before you continue")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*======================================================================================
    * Click on 'Continue'
    ======================================================================================*/

    .group("ET_030_Before_You_Continue") {
      exec(http("ET_030_005_Before_You_Continue")
        .get(BaseURL + "/work-postcode")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("the postcode where you worked or work?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*======================================================================================
    * Enter your work postcode
    ======================================================================================*/

    .group("ET_040_Work_Postcode") {
      feed(postcodeFeeder)

        .exec(http("ET_040_005_Work_Postcode")
          .post(BaseURL + "/work-postcode")
          .headers(CommonHeader)
          .formParam("_csrf", "${csrf}")
          .formParam("workPostcode", "${postcode}")
          .check(CsrfCheck.save)
          .check(substring("Are you making the claim for yourself, or representing someone else?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*======================================================================================
    * Are you making the claim for yourself, or representing someone else? - my own claim
    ======================================================================================*/

    .group("ET_050_Claim_Yourself") {
      exec(http("ET_050_005_Claim_Yourself")
        .post(BaseURL + "/lip-or-representative")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimantRepresentedQuestion", "No")
        .check(CsrfCheck.save)
        .check(substring("Are you making a claim on your own or with others?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*======================================================================================
    * Are you making a claim on your own or with others? - Own claim
    ======================================================================================*/

    .group("ET_060_Claim_Own_Or_Others") {
      exec(http("ET_060_005_Claim_Own_Or_Others")
        .post(BaseURL + "/single-or-multiple-claim")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("caseType", "Single")
        .check(CsrfCheck.save)
        .check(substring("Acas early conciliation certificate")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Do you have an ‘Acas early conciliation certificate’ for the respondent or respondents you're claiming against? - Yes
    ===============================================================================================*/

    .group("ET_070_ACAS_Certificate") {
      exec(http("ET_070_005_ACAS_Certificate")
        .post(BaseURL + "/do-you-have-an-acas-no-many-resps")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("acasMultiple", "Yes")
        .check(CsrfCheck.save)
        .check(substring("What type of claim are you making?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What type of claim are you making? - Discrimination, Whistle blowing
    ===============================================================================================*/

    .group("ET_080_Representative") {
      exec(http("ET_080_005_Representative")
        .post(BaseURL + "/type-of-claim")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("typeOfClaim", "discrimination")
        .formParam("typeOfClaim", "whistleBlowing")
        .formParam("otherClaim", "")
        .check(regex("""callback&state=(\w{8}-\w{4}-\w{4}-\w{4}-\w{12})""").saveAs("state"))
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Log in
    ===============================================================================================*/

    .group("ET_090_Log_In") {
      exec(http("ET_090_005_Log_In")
        .post(IdamURL + "/login?client_id=et-sya&response_type=code&redirect_uri=" + BaseURL + "/oauth2/callback&state=${state}")
        .headers(CommonHeader)
        .formParam("username", "${username}")
        .formParam("password", "${password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "true")
        .formParam("_csrf", "${csrf}")
        .check(substring("You do not have to complete your claim in one go")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


      /*===============================================================================================
        * You do not have to complete your claim in one go - Continue
        ===============================================================================================*/

      .group("ET_100_One_Go") {
        exec(http("ET_100_005_One_Go")
          .get(BaseURL + "/steps-to-making-your-claim")
          .headers(CommonHeader)
          .check(substring("Steps to making your claim")))
      }
      .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Personal Details - Click Link
    ===============================================================================================*/

    .group("ET_110_Personal_Details") {
      exec(http("ET_110_005_Personal_Details")
        .get(BaseURL + "/dob-details")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("What is your date of birth?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What is your date of birth?
    ===============================================================================================*/

    .group("ET_120_DoB") {
      exec(http("ET_120_005_DoB")
        .post(BaseURL + "/dob-details")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("dobDate-day", Common.getDay())
        .formParam("dobDate-month", Common.getMonth())
        .formParam("dobDate-year", Common.getDobYear())
        .check(CsrfCheck.save)
        .check(substring("Sex and preferred title")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Sex and preferred title
    ===============================================================================================*/

    .group("ET_130_Sex") {
      exec(http("ET_130_005_Sex")
        .post(BaseURL + "/sex-and-title")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimantSex", "Male")
        .formParam("preferredTitle", "")
        .check(CsrfCheck.save)
        .check(substring("What is your contact or home address?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What is your contact or home address? - Postcode look up
    ===============================================================================================*/

    .group("ET_140_Your_Address_LookUp") {
        exec(Common.postcodeLookup)
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Enter address details
    ===============================================================================================*/

    .group("ET_145_Your_Address_Select") {
      exec(http("ET_145_005_Your_Address_Select")
        .post(BaseURL + "/address-details")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("address1", "address1" +  "${ETRandomString}")
        .formParam("address2", "address2" + "${ETRandomString}")
        .formParam("addressTown", "addressTown" + "${ETRandomString}")
        .formParam("addressCountry", "addressCountry" + "${ETRandomString}")
        .formParam("addressPostcode", "${postcode}")
        .check(CsrfCheck.save)
        .check(substring("What is your telephone number")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What is your telephone number?
    ===============================================================================================*/

    .group("ET_150_Telephone_Number") {
      exec(http("ET_150_005_Telephone_Number")
        .post(BaseURL + "/telephone-number")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("telNumber", ("07712" + Common.randomNumber(6)))
        .check(CsrfCheck.save)
        .check(substring("How would you like to be contacted about your claim?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)



    /*===============================================================================================
    * How would you like to be contacted about your claim?
    ===============================================================================================*/

    .group("ET_160_Contact_Method") {
      exec(http("ET_160_005_Contact_Method")
        .post(BaseURL + "/how-would-you-like-to-be-updated-about-your-claim")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimantContactPreference", "Email")
        .check(CsrfCheck.save)
        .check(substring("Would you be able to take part in hearings by video and phone?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Would you be able to take part in hearings by video and phone? - Yes, I can take part in video hearings
    ===============================================================================================*/

    .group("ET_170_Hearing_Participation") {
      exec(http("ET_170_005_Hearing_Participation")
        .post(BaseURL + "/would-you-want-to-take-part-in-video-hearings")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("hearingPreferences", "Video")
        .formParam("hearingAssistance", "")
        .check(CsrfCheck.save)
        .check(substring("Do you have a physical, mental or learning disability or long term health condition that means you need support during your case?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Would you be able to take part in hearings by video and phone? - Yes, I can take part in video hearings
    ===============================================================================================*/

    .group("ET_180_Need_Support") {
      exec(http("ET_180_005_Need_Support")
        .post(BaseURL + "/reasonable-adjustments")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("reasonableAdjustmentsDetail", "")
        .formParam("reasonableAdjustments", "No")
        .check(CsrfCheck.save)
        .check(substring("Have you completed this section?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Have you completed this section?
    ===============================================================================================*/

    .group("ET_190_Contact_Completed") {
      exec(http("ET_190_005_Contact_Completed")
        .post(BaseURL + "/personal-details-check")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("personalDetailsCheck", "Yes")
        .check(substring("Personal details\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Contact details\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Your preferences\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Steps to making your claim")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)
  }


}
