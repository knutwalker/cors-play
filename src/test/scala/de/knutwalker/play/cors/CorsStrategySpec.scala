package de.knutwalker.play.cors

import play.api.mvc.RequestHeader
import org.scalatest.{ Matchers, FlatSpec }
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen

// format: +preserveSpaceBeforeArguments
// format: -rewriteArrowSymbols
class CorsStrategySpec extends FlatSpec with Matchers with PropertyChecks {

  import TestUtils._

  "The Everyone strategy" should "set * as the allowed origin" in {

    forAll(GenOrigin, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.Everyone(r) shouldBe Some("*")
    }

  }

  it should "set * even if there is no Origin header present" in {

    forAll(GenMethod) { method =>
      val r = request(method)

      CorsStrategy.Everyone(r) shouldBe Some("*")
    }
  }

  "The NoOne strategy" should "never allow any request" in {

    forAll(GenOrigin, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.NoOne(r) shouldBe None
    }
  }

  "The Origin strategy" should "always allow the requesting origin" in {

    forAll(GenOrigin, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.Origin(r) shouldBe Some(origin)
    }
  }

  it should "not allow requests with a missing origin" in {

    forAll(GenMethod) { method =>
      val r = request(method)

      CorsStrategy.Origin(r) shouldBe None
    }
  }

  "The Localhost strategy" should "not allow requests not from localhost" in {

    forAll(GenOrigin._1.suchThat(s => !isLocalHost(s)) -> "origin", GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.Localhost(r) shouldBe None
    }
  }

  it should "allow only requests from localhost" in {

    forAll(GenLocalhost, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.Localhost(r) shouldBe Some(origin)
    }
  }

  it should "allow for optional port restrictions" in {

    val GenPort = Gen.chooseNum(1, 65535, 80, 8080, 443, 9000)

    forAll(GenLocalhost, GenMethod, GenPort -> "requestPort", Gen.listOf(GenPort) -> "allowedPorts") { (origin, method, requestPort, allowedPorts) =>

      val requestOrigin = s"$origin:$requestPort"
      val r = request(method, "Origin" -> requestOrigin)

      val expected = Some(allowedPorts.contains(requestPort)).filter(identity).map(_ => requestOrigin)

      CorsStrategy.Localhost(allowedPorts: _*)(r) shouldBe expected
    }
  }

  "The Fixed strategy" should "return a fixed set of origins" in {

    forAll(GenOrigin, Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origin, origins, method) =>
      val r = request(method, "Origin" -> origin)

      CorsStrategy.Fixed(origins: _*)(r) shouldBe Some(origins.mkString(","))
    }
  }

  it should "not allow the request, if there is no Origin header" in {
    forAll(Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origins, method) =>
      val r = request(method)

      CorsStrategy.Fixed(origins: _*)(r) shouldBe None
    }
  }

  "The WhiteList strategy" should "allow the origin, if it is in the white list" in {

    forAll(GenOrigin, Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origin, origins, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(origin).filter(origins.contains)

      CorsStrategy.WhiteList(origins: _*)(r) shouldBe expected
    }

    forAll(GenOrigin, Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origin, oldOrigins, method) =>
      val r = request(method, "Origin" -> origin)

      val origins = origin :: oldOrigins

      CorsStrategy.WhiteList(origins: _*)(r) shouldBe Some(origin)
    }
  }

  it should "not allow the request, if there is no Origin header" in {
    forAll(Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origins, method) =>
      val r = request(method)

      CorsStrategy.WhiteList(origins: _*)(r) shouldBe None
    }
  }

  "The BlackList strategy" should "allow the origin, if it is not in the black list" in {

    forAll(GenOrigin, Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origin, origins, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(origin).filterNot(origins.contains)

      CorsStrategy.BlackList(origins: _*)(r) shouldBe expected
    }

    forAll(GenOrigin, Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origin, oldOrigins, method) =>
      val r = request(method, "Origin" -> origin)

      val origins = origin :: oldOrigins

      CorsStrategy.BlackList(origins: _*)(r) shouldBe None
    }

  }

  it should "not allow the request, if there is no Origin header" in {
    forAll(Gen.listOf(GenOrigin._1) -> "origins", GenMethod) { (origins, method) =>
      val r = request(method)

      CorsStrategy.BlackList(origins: _*)(r) shouldBe None
    }
  }

  "The Satisfies strategy" should "allow everything, based on some custom boolean logic" in {

    forAll(GenOrigin, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some("*").filter(_ => method == "GET")

      CorsStrategy.Satisfies(_.method == "GET")(r) shouldBe expected
    }
  }

  it should "provide a facility, to allow the origin instead of everything" in {

    forAll(GenOrigin, GenMethod) { (origin, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(origin).filter(_ => method == "GET")

      CorsStrategy.Satisfies(_.method == "GET").withOrigin(r) shouldBe expected
    }
  }

  it should "provide a facility, to allow any origin instead of everything" in {

    forAll(GenOrigin, GenOrigin._1 -> "allowedOrigin", GenMethod) { (origin, allowedOrigin, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(allowedOrigin).filter(_ => method == "GET")

      CorsStrategy.Satisfies(_.method == "GET").allowing(allowedOrigin)(r) shouldBe expected
    }
  }

  "The CustomPF strategy" should "allow arbitrary logic as a partial function" in {

    forAll(GenOrigin, GenOrigin._1 -> "allowedOrigin", GenMethod) { (origin, allowedOrigin, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(allowedOrigin).filter(_ => method == "GET")

      CorsStrategy.CustomPF {
        case rh if rh.method == "GET" => allowedOrigin
      }(r) shouldBe expected
    }
  }

  "The Custom strategy" should "allow arbitrary logic" in {

    forAll(GenOrigin, GenOrigin._1 -> "allowedOrigin", GenMethod) { (origin, allowedOrigin, method) =>
      val r = request(method, "Origin" -> origin)

      val expected = Some(allowedOrigin).filter(_ => method == "GET")

      CorsStrategy.Custom { rh =>
        if (rh.method == "GET") Some(allowedOrigin)
        else None
      }(r) shouldBe expected
    }
  }
}
