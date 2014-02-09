package de.knutwalker.play.cors

import play.api.http.HeaderNames
import play.api.mvc.{ SimpleResult, Results, Filter, RequestHeader }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/** A Filter implementation that applies CORS logic
  * @param strategy the strategy that determines, whether or not a request should be allowed
  */
case class CorsFilter(strategy: RequestHeader ⇒ Option[String]) extends Filter {

  // TODO: improve check, if preflight request
  private def isPreflight(r: RequestHeader) =
    r.method.equalsIgnoreCase("OPTIONS") && r.headers.keys("Access-Control-Request-Method")

  // TODO: decide what to return instead of 405
  private def corsPreflight(request: RequestHeader) = strategy(request) match {
    case Some(x) ⇒ Results.Ok.withHeaders(corsHeaders(x, request): _*)
    case None    ⇒ Results.MethodNotAllowed
  }

  private def corsHeaders(request: RequestHeader): Seq[(String, String)] = strategy(request) match {
    case Some(x) ⇒ corsHeaders(x, request)
    case None    ⇒ Seq()
  }

  // TODO: allow customization of CORS headers
  private def corsHeaders(origin: String, request: RequestHeader): Seq[(String, String)] = Seq(
    HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
    HeaderNames.ACCESS_CONTROL_ALLOW_METHODS -> request.headers.get(HeaderNames.ACCESS_CONTROL_REQUEST_METHOD).getOrElse("*"),
    HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS -> request.headers.get(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS).getOrElse("")
  )

  def apply(next: (RequestHeader) ⇒ Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] =
    if (isPreflight(rh)) Future.successful(corsPreflight(rh))
    else next(rh).map(_.withHeaders(corsHeaders(rh): _*))
}
