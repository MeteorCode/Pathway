enablePlugins(ScalaJSPlugin)

val projectVersion = "2.0.0" // current release version
val gitHeadCommitSha = settingKey[String]("current git commit short SHA")
gitHeadCommitSha in ThisBuild := Process("git rev-parse --short HEAD")
  .lines
  .headOption
  .getOrElse("")

lazy val root = project.in(file("."))
  .aggregate(pathwayJS, pathwayJVM)
  .settings(
    version := s"$projectVersion-${gitHeadCommitSha.value}",
    publish := {},
    publishLocal := {}
  )

lazy val pathway = crossProject.in(file("."))
  .settings(
    name          := "pathway",
    organization  := "com.meteorcode",
    version       := s"$projectVersion-${gitHeadCommitSha.value}",
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
    scalaVersion  := "2.11.7",
    assemblyJarName in assembly := s"pathway-fat-$projectVersion-${gitHeadCommitSha.value}.jar",
    test in assembly := {} // skip tests, they should have already been run
  )
  .jsSettings(
    scalaVersion  := "2.11.6"
  )

  lazy val pathwayJVM = pathway.jvm
  lazy val pathwayJS  = pathway.js

  wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
    Wart.Any, Wart.Nothing, Wart.Serializable, Wart.NonUnitStatements,
    Wart.Throw, Wart.DefaultArguments, Wart.NoNeedForMonad
  )
