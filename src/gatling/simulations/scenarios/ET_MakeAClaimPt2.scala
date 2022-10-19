//do a check to see if the section is complete
package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util.Random


object ET_MakeAClaimPt2 {

  val BaseURL = Environment.baseURL
  val IdamURL = Environment.idamURL

  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternDate = DateTimeFormatter.ofPattern("yyyyMMdd")

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader

  val postcodeFeeder = csv("postcodes.csv").circular
  
  val rnd = new Random()

  val MakeAClaim =

    exec(_.setAll(
      "ETRandomString" -> (Common.randomString(7)),
      "ETDobDay" -> Common.getDay(),
      "ETDobMonth" -> Common.getMonth()))

    /*===============================================================================================
    * Employment Status link click
    ===============================================================================================*/

    .group("ET_200_Employment_Status") {
      exec(http("ET_200_005_Employment_Status")
        .get(BaseURL + "/past-employer")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("Did you work for the organisation or person you’re making your claim against")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Did you work for the organisation or person you’re making your claim against?
    ===============================================================================================*/

    .group("ET_210_Work_For_Org") {
      exec(http("ET_210_005_Work_For_Organisation")
        .post(BaseURL + "/past-employer")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("pastEmployer", "Yes")
        .check(substring("Are you still working for the organisation or person")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Are you still working for the organisation or person you're making your claim against? - I'm working a notice period for the respondent
    ===============================================================================================*/

    .group("ET_220_Still_Work_For_Org") {
      exec(http("ET_220_005_Still_Work_For_Org")
        .post(BaseURL + "/are-you-still-working")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("isStillWorking", "Notice")
        .check(substring("Employment details")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Employment details
    ===============================================================================================*/

    .group("ET_230_Employment_Details") {
      exec(http("ET_230_005_Employment_Details")
        .post(BaseURL + "/job-title")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("jobTitle", "${ETRandomString}" + "Job")
        .check(substring("Employment start date")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Employment start date
    ===============================================================================================*/

    .group("ET_240_Start_Date") {
      exec(http("ET_240_005_Start_Date")
        .post(BaseURL + "/start-date")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("startDate-day", "${ETDobDay}")
        .formParam("startDate-month", "${ETDobMonth}")
        .formParam("startDate-year", Common.getJobStartDate())
        .check(substring("When does your notice period end?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * When does your notice period end?
    ===============================================================================================*/

    .group("ET_250_Notice_End") {
      exec(http("ET_250_005_Notice_End")
        .post(BaseURL + "/notice-end")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("noticeEnds-day", "${ETDobDay}")
        .formParam("noticeEnds-month", "${ETDobMonth}")
        .formParam("noticeEnds-year", Common.getNoticeEndDate())
        //has to be a future date
        .check(substring("Is your notice period in weeks or months?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Is your notice period in weeks or months?
    ===============================================================================================*/

    .group("ET_260_Weeks_Or_Months") {
      exec(http("ET_260_005_Weeks_Or_Months")
        .post(BaseURL + "/notice-type")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("noticePeriodUnit", "Months")
        .check(substring("How many months of your notice period are you being paid for?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * How many months of your notice period are you being paid for?
    ===============================================================================================*/

    .group("ET_270_Number_Of_Months") {
      exec(http("ET_270_005_Number_Of_Months")
        .post(BaseURL + "/notice-length")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("noticePeriodLength", "${ETDobDay}")
        .check(substring("What are your average weekly hours?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What are your average weekly hours?
    ===============================================================================================*/

    .group("ET_280_Weekly_Hours") {
      exec(http("ET_280_005_Weekly_Hours")
        .post(BaseURL + "/average-weekly-hours")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("avgWeeklyHrs","1" + "${ETDobDay}")
        .check(substring("Your pay")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Your pay
    ===============================================================================================*/

    .group("ET_290_Your_Pay") {
      exec(http("ET_290_005_Your_Pay")
        .post(BaseURL + "/pay")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("payBeforeTax", now.plusYears(5 + rnd.nextInt(15)).format(patternYear))
        .formParam("payAfterTax", now.plusYears(2 + rnd.nextInt(3)).format(patternYear))
        .formParam("payInterval", "Months")
        .check(substring("Did the respondent make any contributions to your pension? ")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Did the respondent make any contributions to your pension? - Not sure
    ===============================================================================================*/

    .group("ET_300_Pension_Contributions") {
      exec(http("ET_300_005_Pension_Contributions")
        .post(BaseURL + "/pension")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimantPensionWeeklyContribution", "")
        .formParam("claimantPensionContribution", "Not Sure")
        .check(substring("Do or did you receive any employee benefits?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Do or did you receive any employee benefits? - No
    ===============================================================================================*/

    .group("ET_310_Employee_Benefits") {
      exec(http("ET_310_005_Employee_Benefits")
        .post(BaseURL + "/benefits")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("benefitsCharCount", "")
        .formParam("employeeBenefits", "No")
        .check(substring("What is the name of the respondent")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What is the name of the respondent you're making the claim against?
    ===============================================================================================*/

    .group("ET_320_Respondent_Name") {
      exec(http("ET_320_005_Respondent_Name")
        .post(BaseURL + "/respondent/1/respondent-name")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("respondentName", "${ETRandomString}" + "Respondent")
        .check(substring("What is the address of")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What is the address of Respondent?
    ===============================================================================================*/

    .group("ET_330_Respondent_Address_LookUp") {
      exec(Common.postcodeLookup)
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Respondent Address LookUp
    ===============================================================================================*/

    .group("ET_335_Respondent_Address_LookUp") {
      feed(postcodeFeeder)

      .exec(http("ET_335_Respondent_Address_LookUp")
        .post(BaseURL + "/respondent/1/respondent-address")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("respondentAddress1", "respondentAddress1" + "${ETRandomString}")
        .formParam("respondentAddress2", "respondentAddress2" + "${ETRandomString}")
        .formParam("respondentAddressTown", "respondentAddressTown" + "${ETRandomString}")
        .formParam("respondentAddressCountry", "respondentAddressCountry" + "${ETRandomString}")
        .formParam("respondentAddressPostcode", "${postcode}")
        .check(substring("Did you work at")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Did you work at address? - yes
    ===============================================================================================*/

    .group("ET_340_Work_At") {
      exec(http("ET_340_005_Work_At")
        .post(BaseURL + "/respondent/1/work-address")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimantWorkAddressQuestion", "Yes")
        .check(substring("Do you have an Acas certificate number for")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Do you have an Acas certificate number for respondent? - Yes
    ===============================================================================================*/

    .group("ET_350_Respondent_Acas") {
      exec(http("ET_350_Respondent_Acas")
        .post(BaseURL + "/respondent/1/acas-cert-num")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("acasCert", "Yes")
        .formParam("acasCertNum", "R" + Common.randomNumber(6) + "/" + Common.randomNumber(2) + "/" + Common.randomNumber(2))
        .check(substring("Check the respondent details")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Check the respondent details
    ===============================================================================================*/

    .group("ET_360_Respondent_Check") {
      exec(http("ET_360_005_Respondent_Check")
        .get(BaseURL + "/employment-respondent-task-check")
        .headers(CommonHeader)
        .check(substring("Have you completed this section?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Employment/Respondent submit
    ===============================================================================================*/

    .group("ET_370_Respondent_Submit") {
      exec(http("ET_370_005_Respondent_Submit")
        .post(BaseURL + "/employment-respondent-task-check")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("employmentAndRespondentCheck", "Yes")
        .check(substring("Employment status\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Respondent details\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Steps to making your claim")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Describe what happened to you link
    ===============================================================================================*/

    .group("ET_380_Claim_Details") {
      exec(http("ET_380_005_Claim_Details")
        .get(BaseURL + "/claim-type-discrimination")
        .headers(CommonHeader)
        .check(CsrfCheck.save)
        .check(substring("What type of discrimination are you claiming?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What type of discrimination are you claiming? - age, race and sex
    ===============================================================================================*/

    .group("ET_390_Type_Of_Descrimination") {
      exec(http("ET_390_Type_Of_Descrimination")
        .post(BaseURL + "/claim-type-discrimination")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimTypeDiscrimination", "Age")
        .formParam("claimTypeDiscrimination", "Race")
        .formParam("claimTypeDiscrimination", "Sex")
        .check(substring("Describe what happened to you")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Describe what happened to you
    ===============================================================================================*/

    .group("ET_400_Describe_What_Happened") {
      exec(http("ET_400_005_Describe_What_Happened")
        .post(BaseURL + "/describe-what-happened")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimSummaryText", "${ETRandomString}" + "Description")
        .formParam("claimSummaryFileName", "(binary)")
        .check(substring("What do you want if your claim is successful?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What do you want if your claim is successful? - Compensation only
    ===============================================================================================*/

    .group("ET_410_Outcome_If_Successful") {
      exec(http("ET_410_005_Outcome_If_Successful")
        .post(BaseURL + "/tell-us-what-you-want")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("tellUsWhatYouWant", "compensation")
        .check(substring("What compensation are you seeking?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * What compensation are you seeking?
    ===============================================================================================*/

    .group("ET_420_Compensation") {
      exec(http("ET_420_005_Compensation")
        .post(BaseURL + "/compensation")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("compensationOutcome", "${ETRandomString}" + "Compensation")
        .formParam("compensationAmount", Common.getDobYear())
        .check(substring("Whistleblowing claims")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Whistleblowing claims - no
    ===============================================================================================*/

    .group("ET_430_Whistleblowing") {
      exec(http("ET_430_005_Whistleblowing")
        .post(BaseURL + "/whistleblowing-claims")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("whistleblowingEntityName", "")
        .formParam("whistleblowingClaim", "No")
        .check(substring("Have you completed this section?")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Have you completed this section?
    ===============================================================================================*/

    .group("ET_440_Claim_Section_Complete") {
      exec(http("ET_440_005_Claim_Section_Complete")
        .post(BaseURL + "/claim-details-check")
        .headers(CommonHeader)
        .formParam("_csrf", "${csrf}")
        .formParam("claimDetailsCheck", "Yes")
        .check(substring("Describe what happened to you\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Tell us what you want from your claim\n                  </a>\n                  \n              </span>\n              \n\n              \n                <strong class=\"govuk-tag app-task-list__tag\">\n  COMPLETED"))
        .check(substring("Steps to making your claim")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Check your answers link
    ===============================================================================================*/

    .group("ET_450_Final_Check_Answers") {
      exec(http("ET_450_005_Final_Check_Answers")
        .get(BaseURL + "/check-your-answers")
        .headers(CommonHeader)
        .check(substring("Check your answers")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Final submission
    ===============================================================================================*/

    .group("ET_460_Final_Check_Submit") {
      exec(http("ET_460_005_Final_Check_Submit")
        .get(BaseURL + "/submitDraftCase")
        .headers(CommonHeader)
        .check(substring("Your claim has been submitted"))
         .check(regex("""<dd class="govuk-summary-list__value">
                        |          (\w{16})""".stripMargin).saveAs("submissionReference")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    /*===============================================================================================
    * Log Out
    ===============================================================================================*/

    .group("ET_470_Log_Out") {
      exec(http("ET_470_005_Log_Out")
        .get(BaseURL + "/logout")
        .headers(CommonHeader)
        .check(substring("Sign in or create an account")))
    }
    .pause(MinThinkTime seconds, MaxThinkTime seconds)



}
