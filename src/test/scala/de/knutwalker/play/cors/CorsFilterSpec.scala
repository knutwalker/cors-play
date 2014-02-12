package de.knutwalker.play.cors

import org.scalacheck.Gen
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ Matchers, FlatSpec }
import play.api.http.HeaderNames
import play.api.mvc.Results
import scala.concurrent.Future
import scala.util.Success

// format: +preserveSpaceBeforeArguments
// format: -rewriteArrowSymbols
class CorsFilterSpec extends FlatSpec with Matchers with PropertyChecks {

  import TestUtils._

  "The CorsFilter" should "directly respond successful to preflight requests" in {

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
      val result = Results.InternalServerError

      val expected = Results.Ok.withHeaders(responseHeaders: _*)

      val corsFilter = CorsFilter(CorsStrategy.Origin)
      val filterResult = corsFilter(rh => Future.successful(result))(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "eventually respond successful to non-preflight requests" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin) ++ maybeHeaders
      }

      val responseHeaders = {
        val maybeHeaders = Seq(HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> (if (addHeader) header else ""))
        Seq(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> "*") ++ maybeHeaders
      }

      val req = request(method, requestHeaders: _*)
      val result = Results.InternalServerError

      val expected = Results.InternalServerError.withHeaders(responseHeaders: _*)

      val corsFilter = CorsFilter(CorsStrategy.Origin)
      val filterResult = corsFilter(rh => Future.successful(result))(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "directly refuse not allowed preflight requests" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin, HeaderNames.ACCESS_CONTROL_REQUEST_METHOD -> method) ++ maybeHeaders
      }

      val req = request("OPTIONS", requestHeaders: _*)
      val result = Results.InternalServerError

      val expected = Results.MethodNotAllowed

      val corsFilter = CorsFilter(CorsStrategy.NoOne)
      val filterResult = corsFilter(rh => Future.successful(result))(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

  it should "eventually (CO-)refuse not allowed non-preflight requests" in {

    forAll(GenOrigin, GenMethod, Gen.alphaStr -> "header", GenBool -> "addHeader") { (origin, method, header, addHeader) =>

      val requestHeaders = {
        val maybeHeaders = if (addHeader) Seq(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS -> header) else Seq.empty
        Seq(HeaderNames.ORIGIN -> origin) ++ maybeHeaders
      }

      val req = request(method, requestHeaders: _*)
      val result = Results.InternalServerError

      val expected = Results.InternalServerError

      val corsFilter = CorsFilter(CorsStrategy.NoOne)
      val filterResult = corsFilter(rh => Future.successful(result))(req)

      eventually {
        filterResult.value shouldBe Some(Success(expected))
      }
    }
  }

}
