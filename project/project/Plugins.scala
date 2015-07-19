import sbt._
import Keys._

object Plugins extends Build {
  lazy val root = project.in(file(".")).dependsOn(lwjglPlugin)
  lazy val lwjglPlugin = uri("git://github.com/philcali/sbt-lwjgl-plugin.git")
}
