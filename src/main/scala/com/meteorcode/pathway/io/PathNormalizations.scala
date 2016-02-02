package com.meteorcode.pathway.io

import org.scalactic.{AbstractStringUniformity, Uniformity}

import scala.language.postfixOps

/**
  * Scalactic normalizations for strings that represent paths
  *
  * Created by hawk on 2/2/16.
  */
trait PathNormalizations {

  /**
    * Produces a <code>Uniformity[String]</code> whose <code>normalized</code>
    * method removes the trailing slash
    *
    * @return a <code>Uniformity[String]</code> that normalizes by
    *         pruning the trailing slash
    */
  val trailingSlashNormed: Uniformity[String]
    = new AbstractStringUniformity {

        def normalized(s: String): String
          = s withoutTrailingSlash

        override def toString: String = "trailingSlashNormed"
      }

  val extensionTrimmed: Uniformity[String]
    = new AbstractStringUniformity {

      def normalized(s: String): String
        = s split '.' dropRight 1 mkString "."

      override def toString: String = "extensionTrimmed"
    }

}
