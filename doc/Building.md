[SBT](http://www.scala-sbt.org/), the Scala Build Tool, is used to build and test Pathway.

Project Structure
-----------------

Since separate JVM and JavaScript versions of Pathway are being developed in parallel, the Pathway SBT project is configured to use separate source roots for the JVM and JavaScript projects. A majority of Pathway's codebase can be shared between the JVM and Scala.js projects, and should go in the `shared/` source root. JVM-only code should go in the `jvm/` source root, while JavaScript-only code goes in the `js/` source root. Note that each of these source roots has a separate `target/` directory for build artifacts. JVM artifacts are placed in `jvm/target/` while compiled JavaScript code is placed in `js/target/`.


Using SBT
---------

By default, all SBT commands will be applied to the entire Pathway project. For example, `sbt compile` will compile both the JVM class files and the JavaScript code. To apply an SBT command to one half of the project, you may prefix the command with the name of the project component and a slash. For example, `sbt pathwayJVM/compile` will compile only the JVM class files.

SBT, unlike some build tools such as Gradle, is designed to be used interactively. While running `sbt` from the command line followed by a task name will run that task and then close, as in Gradle, `sbt` can also be launched to run in an interactive console mode where the user may enter commands at a prompt. This is useful for continuous compilation and testing, as well as other purposes. If the user launches an interactive SBT prompt and types the command `~test`, SBT will watch the source directories for changes, recompile any changed classes, and re-run tests automatically.
