package com.meteorcode.pathway.io

import com.meteorcode.pathway.io.FileHandle

class ResourceManager (assetsDir: String) {
  
  def read (path: String): FileHandle = {
    //TODO: Stub
    return new DesktopFileHandle(path)
  }
}