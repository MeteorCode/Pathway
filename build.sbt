enablePlugins(ScalaJSPlugin)

lazy val root = project.in(file("."))
  .aggregate(pathwayJS, pathwayJVM)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val pathway = crossProject.in(file("."))
  .settings(
    name := "pathway",
    organization  := "com.meteorcode",
    version := "0.1-SNAPSHOT",
    resolvers += "Hawk's Bintray Repo" at "https://dl.bintray.com/hawkw/maven",
    libraryDependencies ++= Seq(
      "org.beanshell"   %  "bsh"         % "2+",
      // --- test dependencies ------------------------------
      "org.scalacheck"  %% "scalacheck"  % "1.12.2+"            % "test",
      "org.scalatest"   %% "scalatest"   % "2.2.4+"             % "test",
      "org.mockito"     %  "mockito-all" % "1.10.19+"           % "test",
      "me.hawkweisman"  %% "util"        % "0.0.2-ed7622b.37"   % "test"
    )
  )
  .jvmSettings(
    scalaVersion  := "2.11.7"
  )
  .jsSettings(
    scalaVersion  := "2.11.6"
  )

  lazy val pathwayJVM = pathway.jvm
  lazy val pathwayJS  = pathway.js
