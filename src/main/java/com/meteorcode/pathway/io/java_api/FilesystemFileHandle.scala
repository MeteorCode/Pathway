package com.meteorcode.pathway.io.java_api

import com.meteorcode.pathway.io.scala_api
import com.meteorcode.pathway.io.scala_api.ResourceManager

/**
 * Wrapper to give Java callers a constructor for creating initial seed [[FileHandle]]s.
 * Created by hawk on 6/10/15.
 */
class FilesystemFileHandle private[this](underlying: scala_api.FileHandle) extends FileHandle(underlying) {

  def this(virtualPath: String, realPath: String, manager: ResourceManager) = this(
    new FilesystemFileHandle(virtualPath, realPath, manager)
  )
}

