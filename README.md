# CORS Play

[![Build Status](https://travis-ci.org/knutwalker/cors-play.png?branch=master)](https://travis-ci.org/knutwalker/cors-play)
[![Coverage Status](https://coveralls.io/repos/knutwalker/cors-play/badge.png?branch=master)](https://coveralls.io/r/knutwalker/cors-play?branch=master)


Easy CORS integration for Play:

 - use CORS with your Playframework application
 - No additional controllers or routes required


## Installation

### Stable Version

_TODO_

### Snapshot Version

    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

    libraryDependencies ++= Seq(
      "de.knutwalker" %% "cors-play" % "0.1.0-SNAPSHOT"
    )


## Usage

You have to mixin the `CorsSupport` trait into your `Global` object

```scala
import de.knutwalker.play.cors.{ CorsStrategy, CorsSupport }
import play.api.GlobalSettings

object Global extends GlobalSettings with CorsSupport {

  override val corsStrategy = CorsStrategy.Everyone
}
```

You can choose different a `CorsStrategy` to customize the behavior

`CorsStrategy` | Behaviour
-------------- | -------------
`Everyone`     | Allow every request, event malformed (not CORS) ones
`Origin`       | Allow requests with the `Origin` header set. For valid CORS requests, this is the same as `Everyone`.
`NoOne`        | Do not allow any requests
`Localhost`    | Only allow requests from localhost
`Localhost([ports])`   | Only allow requests from localhost that originate from a given list of ports
`Fixed([origins])`     | Provide a fixed set of allowed origins. This results in white-listing behaviour on the browser-side (as the origins are sent as is)
`WhiteList([origins])` | Provide a white list of origins, that are allow to make a CORS request. The list is resolved on server-side, only the white-listed origin is sent in the header.
`BlackList([origins])` | Provide a black list of origins, that are *not* allowed to make CORS requests. The list is resolved on server-side, only the not-black-listed origin is sent in the header.
`Satisfies(handler)`    | Define a custom check, that return `true` for allowed requests, and `false` otherwise. (Sent origin will be `*`)
`Satisfies(handler).allowing(origin)` | Define a custom check, that return `true` for allowed requests, and `false` otherwise. (Sent origin will be `origin`)
`Satisfies(handler).withOrigin`       | Define a custom check, that return `true` for allowed requests, and `false` otherwise. (Sent origin will be the value from the `Origin` header)
`CustomPF(handler)`   | Define a custom check as a partial function, that will match and return an origin for allowed requests, and won't match otherwise
`Custom(handler)`     | Define a custom check, that will return `Some(origin)` for allowed requests, and `None` otherwise.


### License

This software is released under the MIT Licence

http://knutwalker.mit-license.org/
