package de.knutwalker.play.cors

import play.api.GlobalSettings
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.SimpleResult

trait CorsSupport extends GlobalSettings {

  private def optionsOk(method: String) = Some(method)
    .filter(_.equalsIgnoreCase("OPTIONS"))
    .map(_ ⇒ Future.successful(Results.Ok))

  /** Add the CorsFilter to the Applications' FilterChain
    */
  override def doFilter(a: EssentialAction): EssentialAction =
    Filters(super.doFilter(a), CorsFilter(corsStrategy))

  /** Enable OPTIONS requests without having to modify the routes file
    */
  override def onHandlerNotFound(request: RequestHeader): Future[SimpleResult] =
    optionsOk(request.method).getOrElse(super.onHandlerNotFound(request))

  /** Provide the CorsStrategy to be used
    */
  def corsStrategy: CorsStrategy = CorsStrategy.Everyone
}
