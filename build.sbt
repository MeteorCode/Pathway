import scala.util.matching.Regex
import scala.util.matching.Regex.Match

name            := "pathway"
organization    := "com.meteorcode"
version         := s"$projectVersion-${gitHeadCommitSha.value}"
scalaVersion    := "2.11.7"
autoAPIMappings := true // link Scala standard lib in docs

val lwjglVersion = "3.0.0a"
val projectVersion = "2.0.0" // current release version
val gitHeadCommitSha = settingKey[String]("current git commit short SHA")
gitHeadCommitSha in ThisBuild := Process("git rev-parse --short HEAD")
  .lines
  .headOption
  .getOrElse("")

resolvers += "Hawk's Bintray Repo" at "https://dl.bintray.com/hawkw/maven"

libraryDependencies ++= Seq(
  "org.beanshell"   %  "bsh"            % "2+",
  "me.hawkweisman"  %% "util"           % "0.0.3",
  // --- test dependencies ------------------------------
  "org.scalacheck"  %% "scalacheck"     % "1.12.2+"            % "test",
  "org.scalatest"   %% "scalatest"      % "2.2.4+"             % "test",
  "org.mockito"     %  "mockito-all"    % "1.10.19+"           % "test"
)

wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
  Wart.Any, Wart.Nothing, Wart.Serializable, Wart.NonUnitStatements,
  Wart.Throw, Wart.DefaultArguments, Wart.NoNeedForMonad, Wart.Var
)

seq(documentationSettings: _*)

seq(lwjglSettings: _*)

val externalJavadocMap = Map()

/*
 * The rt.jar file is located in the path stored in the sun.boot.class.path system property.
 * See the Oracle documentation at http://docs.oracle.com/javase/6/docs/technotes/tools/findingclasses.html.
 */
val rtJar: String = System.getProperty("sun.boot.class.path").split(java.io.File.pathSeparator).collectFirst {
  case str: String if str.endsWith(java.io.File.separator + "rt.jar") => str
}.get // fail hard if not found

val javaApiUrl: String = "http://docs.oracle.com/javase/8/docs/api/index.html"

val allExternalJavadocLinks: Seq[String] = javaApiUrl +: externalJavadocMap.values.toSeq

def javadocLinkRegex(javadocURL: String): Regex = ("""\"(\Q""" + javadocURL + """\E)#([^"]*)\"""").r

def hasJavadocLink(f: File): Boolean = allExternalJavadocLinks exists {
  javadocURL: String =>
    (javadocLinkRegex(javadocURL) findFirstIn IO.read(f)).nonEmpty
}

val fixJavaLinks: Match => String = m =>
  m.group(1) + "?" + m.group(2).replace(".", "/") + ".html"

/* You can print the classpath with `show compile:fullClasspath` in the SBT REPL.
 * From that list you can find the name of the jar for the managed dependency.
 */
lazy val documentationSettings = Seq(
  apiMappings ++= {
    // Lookup the path to jar from the classpath
    val classpath = (fullClasspath in Compile).value
    def findJar(nameBeginsWith: String): File = {
      classpath.find { attributed: Attributed[File] => (attributed.data ** s"$nameBeginsWith*.jar").get.nonEmpty }.get.data // fail hard if not found
    }
    // Define external documentation paths
    (externalJavadocMap map {
      case (name, javadocURL) => findJar(name) -> url(javadocURL)
    }) + (file(rtJar) -> url(javaApiUrl))
  },
  // Override the task to fix the links to JavaDoc
  doc in Compile <<= (doc in Compile) map {
    target: File =>
      (target ** "*.html").get.filter(hasJavadocLink).foreach { f =>
        //println(s"Fixing $f.")
        val newContent: String = allExternalJavadocLinks.foldLeft(IO.read(f)) {
          case (oldContent: String, javadocURL: String) =>
            javadocLinkRegex(javadocURL).replaceAllIn(oldContent, fixJavaLinks)
        }
        IO.write(f, newContent)
      }
      target
  }
)
