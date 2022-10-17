package utils

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
import jodd.lagarto.dom.NodeSelector

object CookieCheck {
  def save: CheckBuilder[CssCheckType, NodeSelector, String] = css("input[name='cookie']", "value").saveAs("cookieToken")

  def csrfParameter: String = "cookie"
  def csrfTemplate: String = "${cookieToken}"
}
