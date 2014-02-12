package de.knutwalker.play.cors

import play.api.GlobalSettings
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.SimpleResult

trait CorsSupport extends GlobalSettings {

  private lazy val corsFilter = CorsFilter(corsStrategy)

  private def optionsOk(request: RequestHeader) = Some(request.method)
    .filter(_.equalsIgnoreCase("OPTIONS"))
    .map(_ ⇒ Future.successful(Results.Ok))

  /** Add the CorsFilter to the Applications' FilterChain
    */
  override def doFilter(a: EssentialAction): EssentialAction =
    Filters(super.doFilter(a), corsFilter)

  /** Enable OPTIONS requests without having to modify the routes file
    */
  override def onHandlerNotFound(request: RequestHeader): Future[SimpleResult] =
    optionsOk(request).getOrElse(super.onHandlerNotFound(request))

  /** Provide the CorsStrategy to be used
    */
  def corsStrategy: CorsStrategy = CorsStrategy.Origin
}
