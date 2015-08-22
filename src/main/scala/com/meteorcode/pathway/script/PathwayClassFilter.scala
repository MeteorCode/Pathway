package com.meteorcode.pathway.script

import jdk.nashorn.api.scripting._

/**
 * Class filter for Pathway embedded JavaScript.
 *
 * This class filter controls what Java classes may be accessed by JavaScript
 * running within Nashorn, primarily to ensure that script execution is secure.
 *
 * Currently, access to the `java.io` and `java.nio` namespaces is completely
 * denied.
 *
 * Please note that this requires a JDK version >= 8u40 to compile.
 *
 * Created by hawk on 8/22/15.
 */
object PathwayClassFilter
extends ClassFilter {

  private[this] val denied: Array[String]
    = Array("java.io", "java.nio", "scala.io")
  // TODO: what other class namespaces should be denied?
  // potential candidates:
  //  - java.net
  //    * will we need this for multiplayer?
  //    * should we provide our own wrappers like we do for the fs?
  //  - java.lang.System
  //    * scripts already have access to print streams on the script engine
  //    * we probably don't want to give them access to things like System.exit
  // TODO: we might want to operate with a whitelist rather than a blacklist

  def exposeToScripts(className: String): Boolean
    = !denied.exists( className startsWith _ )

}
