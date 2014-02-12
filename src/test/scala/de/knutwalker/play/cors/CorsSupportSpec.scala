package de.knutwalker.play.cors

import org.scalacheck.Gen
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ Matchers, FlatSpec }
import play.api.http.HeaderNames
import play.api.mvc.{ Action, Results }
import scala.util.Success

// format: +preserveSpaceBeforeArguments
// format: -rewriteArrowSymbols
class CorsSupportSpec extends FlatSpec with Matchers with PropertyChecks {

  import TestUtils._

  "The CorsSupportSpec" should "have the Origin strategy as default" in {

    val global = new CorsSupport {}

    global.corsStrategy shouldBe CorsStrategy.Origin
  }

  it should "react to OPTIONS requests on a preflight request" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_REQUEST_METHOD -> method) ++ maybeHeaders
      }

      val req = request("OPTIONS", requestHeaders: _*)
      val expected = Results.Ok

      val global = new CorsSupport {}

      val filterResult = global.onHandlerNotFound(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "react to OPTIONS requests on a non-preflight request" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin) ++ maybeHeaders
      }

      val req = request("OPTIONS", requestHeaders: _*)
      val expected = Results.Ok

      val global = new CorsSupport {}

      val filterResult = global.onHandlerNotFound(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "add the CorsFilter to a preflight request" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_REQUEST_METHOD -> method) ++ maybeHeaders
      }

      val responseHeaders = {
        val maybeHeaders = Seq(HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> (if (addHeader) header else ""))
        Seq(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> method) ++ maybeHeaders
      }

      val req = request("OPTIONS", requestHeaders: _*)
      val expected = Results.Ok.withHeaders(responseHeaders: _*)

      val global = new CorsSupport {}

      val filterAction = global.doFilter(Action(Results.InternalServerError))
      val filterResult = filterAction(req).run

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "add the CorsFilter to a non-preflight request" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin) ++ maybeHeaders
      }

      val responseHeaders = {
        val maybeHeaders = Seq(HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> (if (addHeader) header else ""))
        Seq(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> "*") ++ maybeHeaders
      }

      val req = request("OPTIONS", requestHeaders: _*)
      val expected = Results.InternalServerError.withHeaders(responseHeaders: _*)

      val global = new CorsSupport {}

      val filterAction = global.doFilter(Action(Results.InternalServerError))
      val filterResult = filterAction(req).run

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }
}
