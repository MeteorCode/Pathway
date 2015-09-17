import sbtassembly.MappingSet

import scala.util.matching.Regex

import scala.util.matching.Regex.Match

name            := "pathway"

organization    := "com.meteorcode"

version         := s"$projectVersion-${gitHeadCommitSha.value}"

scalaVersion    := "2.11.7"

autoAPIMappings := true // link Scala standard lib in docs

val lwjglVersion = "3.0.0a"

val nativesDir = "lwjgl-natives" // the directory within the jar file for natives

val projectVersion = "2.0.0" // current release version

val gitHeadCommitSha = settingKey[String]("current git commit short SHA")

gitHeadCommitSha in ThisBuild := Process("git rev-parse --short HEAD").lines.headOption.getOrElse("")

lazy val Benchmark = config("bench") extend Test

resolvers += "Hawk's Bintray Repo" at "https://dl.bintray.com/hawkw/maven"

libraryDependencies ++= Seq(
  // --- LWJGL -----------------------------------------
  "org.lwjgl" % "lwjgl"           % lwjglVersion, // main lajiggle library
  "org.lwjgl" % "lwjgl-platform"  % lwjglVersion  // lajiggle natives
    classifier "natives-windows"
    classifier "natives-linux"
    classifier "natives-osx",
  "me.hawkweisman"              %% "util"           % "0.0.3",
  "com.typesafe.scala-logging"  %% "scala-logging"  % "3.1.0",
  "org.json4s"                  %%  "json4s-native" % "3.3.0.RC1",
  // --- test dependencies ------------------------------
  "org.scalacheck"    %% "scalacheck"     % "1.12.2+"   % "test",
  "org.scalatest"     %% "scalatest"      % "2.2.4+"    % "test",
  "org.mockito"       %  "mockito-all"    % "1.10.19+"  % "test",
  "com.storm-enroute" %% "scalameter"     % "0.6"       % "bench"
)

wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
  Wart.Any, Wart.Nothing, Wart.Serializable, Wart.NonUnitStatements,
  Wart.Throw, Wart.DefaultArguments, Wart.NoNeedForMonad, Wart.Var
)

assembledMappings in assembly ~= { mapSets ⇒ mapSets.map {
    _ match {
      case MappingSet(Some(packageName), mings)
        if packageName.getName.startsWith("lwjgl-platform") ⇒
          MappingSet(Some(packageName), mings.map {
            case ((f: File, path: String)) => (f, s"$nativesDir/" + path)
          })
      case m: MappingSet ⇒ m
    }
  }
}

Project.inConfig(Test)(baseAssemblySettings)

mainClass in assembly in Test := Some("com.meteorcode.pathway.test.TempoRedscreenTest")

//-- ScalaMeter performance testing settings ----------------------------------
configs(Benchmark)

val scalaMeter = new TestFramework("org.scalameter.ScalaMeterFramework")

testFrameworks in Benchmark += scalaMeter

logBuffered in Benchmark := false       // ScalaMeter demands these settings
                                        // due to reasons
parallelExecution in Benchmark := false

inConfig(Benchmark)(Defaults.testSettings)

testOptions in Benchmark += Tests.Argument(scalaMeter, "-silent")
//-----------------------------------------------------------------------------

seq(documentationSettings: _*)

assemblyJarName in assembly in Test := "pathway-assembly-test.jar"

test in assembly in Test := {}

assembledMappings in assembly in Test ~= { mapSets ⇒ mapSets.map {
  _ match {
    case MappingSet(Some(packageName), mings)
      if packageName.getName.startsWith("lwjgl-platform") ⇒
        MappingSet(Some(packageName), mings.map {
          case ((f: File, path: String)) ⇒ (f, s"$nativesDir/" + path)
        })
    case m: MappingSet ⇒ m
  }
}
}

seq(documentationSettings: _*)

val externalJavadocMap = Map()

/*
 * The rt.jar file is located in the path stored in the sun.boot.class.path
 * system property. See the Oracle documentation at
 * http://docs.oracle.com/javase/6/docs/technotes/tools/findingclasses.html.
 */
val rtJar: String = System.getProperty("sun.boot.class.path")
  .split(java.io.File.pathSeparator)
  .collectFirst {
    case str: String if str.endsWith(java.io.File.separator + "rt.jar") ⇒ str
  }.get // fail hard if not found

val javaApiUrl: String = "http://docs.oracle.com/javase/8/docs/api/index.html"

val allExternalJavadocLinks: Seq[String]
  = javaApiUrl +: externalJavadocMap.values.toSeq

def javadocLinkRegex(javadocURL: String): Regex
  = ("""\"(\Q""" + javadocURL + """\E)#([^"]*)\"""").r

def hasJavadocLink(f: File): Boolean = allExternalJavadocLinks exists {
  javadocURL: String ⇒
    (javadocLinkRegex(javadocURL) findFirstIn IO.read(f)).nonEmpty
}

val fixJavaLinks: Match ⇒ String = m ⇒
  m.group(1) + "?" + m.group(2).replace(".", "/") + ".html"

/* You can print the classpath with `show compile:fullClasspath` in the
 * SBT REPL.
 * From that list you can find the name of the jar for the managed dependency.
 */
lazy val documentationSettings = Seq(
  apiMappings ++= {
    // Lookup the path to jar from the classpath
    val classpath = (fullClasspath in Compile).value
    def findJar(nameBeginsWith: String): File = {
      classpath
        .find { attributed: Attributed[File] ⇒
          (attributed.data ** s"$nameBeginsWith*.jar").get.nonEmpty
        }.get // fail hard if not found
         .data
    }
    // Define external documentation paths
    (externalJavadocMap map {
      case (name, javadocURL) ⇒ findJar(name) → url(javadocURL)
    }) + (file(rtJar) → url(javaApiUrl))
  },
  // Override the task to fix the links to JavaDoc
  doc in Compile <<= (doc in Compile) map {
    target: File ⇒
      (target ** "*.html").get.filter(hasJavadocLink).foreach { f ⇒
        //println(s"Fixing $f.")
        val newContent: String = allExternalJavadocLinks.foldLeft(IO.read(f)) {
          case (oldContent: String, javadocURL: String) ⇒
            javadocLinkRegex(javadocURL).replaceAllIn(oldContent, fixJavaLinks)
        }
        IO.write(f, newContent)
      }
      target
  }
)
