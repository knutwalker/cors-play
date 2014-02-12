package de.knutwalker.play.cors

import org.scalacheck.{ Shrink, Gen }
import play.api.mvc.{ Headers, RequestHeader }
import scala.concurrent.Future

object TestUtils {

  val GenBool = Gen.oneOf(Seq(true, false))

  val methods = List("OPTIONS", "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "TRACE", "CONNECT")

  val GenMethod = Gen.oneOf(methods) -> "method"
  val GenOrigin = Gen.alphaStr.map(s ⇒ s"http://$s.com") -> "origin"

  val GenLocalhost = Gen.oneOf(List("http://localhost", "http://127.0.0.1")) -> "origin"
  val isLocalHost = (s: String) ⇒ s.contains("localhost") || s.contains("127.0.0.1")

  implicit val noStringShrink: Shrink[String] = Shrink.shrinkAny
  implicit val noIntShrink: Shrink[Int] = Shrink.shrinkAny

  def request(m: String, hs: (String, String)*) = new RequestHeader {
    def id = 1L
    def tags = Map()
    def uri = "http://example.com/foo?bar=baz"
    def path = "/foo"
    def version = "HTTP/1.1"
    def queryString = Map("bar" -> Seq("baz"))
    def remoteAddress = "127.0.0.1"

    def method = m

    def headers = {
      val pairs = hs.groupBy(_._1).mapValues(_.map(_._2))
      new Headers { val data = pairs.toSeq }
    }
  }
}
