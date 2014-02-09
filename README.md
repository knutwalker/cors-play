# CORS Play

Easy CORS integration for Play:

 - use CORS with your Playframework application
 - No additional controllers or routes required

## Installation

### Stable Version

_TODO_

### Snapshot Version

    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

    libraryDependencies ++= Seq(
      "de.knutwalker" %% "cors-play" % "0.1-SNAPSHOT"
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

_TODO_


### License

This software is released under the MIT Licence

http://knutwalker.mit-license.org/
