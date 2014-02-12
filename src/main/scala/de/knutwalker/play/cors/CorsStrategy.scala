package de.knutwalker.play.cors

import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

/** A CorsStrategy is just a `RequestHeader ⇒ Option[String]`
  * If it returns None, the request should not be allowed.
  * If it returns Some(x), x should be the allowed origin.
  * This might still differ from the requests' Origin
  *
  * @param handler the actual function that is used
  */
abstract class CorsStrategy(handler: RequestHeader ⇒ Option[String]) extends (RequestHeader ⇒ Option[String]) {
  override def apply(v1: RequestHeader): Option[String] = handler(v1)
}

/** Default strategies for typical use cases
  */
object CorsStrategy {
  private val origin = (rh: RequestHeader) ⇒ rh.headers.get(HeaderNames.ORIGIN)
  private def localhost(s: String) = s.contains("localhost") || s.contains("127.0.0.1")
  private def port(ps: Int*) = (s: String) ⇒ ps.exists(p ⇒ s.endsWith(":" + p.toString))

  /** Always allow every request, event if it is malformed
    */
  object Everyone extends CorsStrategy(_ ⇒ Some("*"))

  /** Only allow a request, if it has set an Origin header.
    * For valid CORS requests, this is the same as Everyone
    */
  object Origin extends CorsStrategy(origin)

  /** Do not allow any requests
    */
  object NoOne extends CorsStrategy(_ ⇒ None)

  /** Only allow requests from localhost
    */
  object Localhost extends CorsStrategy(origin andThen (_.filter(localhost)))

  /** Only allow requests from localhost that originate from a given port
    * @param ports a number of ports from which it is allowed to make CORS requests
    */
  case class Localhost(ports: Int*) extends CorsStrategy(origin andThen (_.filter(localhost).filter(port(ports: _*))))

  /** Provide a fixed set of allowed origins.
    * This results in white-listing behaviour on the browser-side
    * @param origins a number of allowed origins
    */
  case class Fixed(origins: String*) extends CorsStrategy(origin andThen (_.map(_ ⇒ origins.mkString(","))))

  /** Provide a white list of origins, that are allow to make a CORS request.
    * The list is resolved on server-side
    * @param domains a number of allowed origins
    */
  case class WhiteList(domains: String*) extends CorsStrategy(origin andThen (_.filter(domains.contains)))

  /** Provide a black list of origins, that are *not* allowed to make CORS requests.
    * The list is resolved on server-side
    * @param domains a number of origins, that are not allowed
    */
  case class BlackList(domains: String*) extends CorsStrategy(origin andThen (_.filterNot(domains.contains)))

  /** Provide a bool-check based on the request headers
    *
    * @param handler given a RequestHeader, return true if the request should be allowed or false if otherwise
    * @param allowed given a RequestHeader, return a string to be used as the allowed origin
    */
  case class Satisfies(handler: (RequestHeader) ⇒ Boolean, allowed: (RequestHeader) ⇒ String = _ ⇒ "*")
      extends CorsStrategy(rh ⇒ if (handler(rh)) Some(allowed(rh)) else None) {
    def allowing(origin: String) = Satisfies(handler, _ ⇒ origin)
    def withOrigin = Satisfies(handler, _.headers(HeaderNames.ORIGIN))
  }

  /** Provide custom logic to determine, whether or not the request should be allowed
    * @param handler a partial function that, given a RequestHeader, should match if the request is allowed and
    *    return the specific allowed origin
    */
  case class CustomPF(handler: PartialFunction[RequestHeader, String]) extends CorsStrategy(handler.lift)

  /** Provide custom logic to determine, whether or not the request should be allowed
    * @param handler a function that, given a RequestHeader, should return Some(origin) if the request is allowed,
    *       otherwise it should return None
    */
  case class Custom(handler: RequestHeader ⇒ Option[String]) extends CorsStrategy(handler)
}
