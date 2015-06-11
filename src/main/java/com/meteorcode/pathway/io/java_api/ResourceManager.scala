package com.meteorcode.pathway.io.java_api

import java.io.IOException

import com.meteorcode.pathway.io.{java_api, scala_api}

/**
 * Created by hawk on 6/10/15.
 */
class ResourceManager protected[io](protected val underlying: scala_api.ResourceManager) {
  //TODO: add constructors so you can actually make this
  /**
   * Request that the ResourceManager handle the file at a given path
   * as a Java [[java_api.FileHandle FileHandle]].
   * @param path the path in the virtual filesystem to handle
   * @throws java.io.IOException if the FileHandle cannot be created
   * @return A [[java_api.FileHandle FileHandle]] for the object that exists at the requested path
   *         in the virutal filesystem.
   */
  @throws(classOf[IOException])
  def handleJava(path: String): java_api.FileHandle = underlying.handle(path).get

}

object ResourceManager {
  implicit def asScala(mangler: ResourceManager): scala_api.ResourceManager = mangler.underlying
  implicit def asJava(mangler: scala_api.ResourceManager): ResourceManager = new ResourceManager(mangler)
}