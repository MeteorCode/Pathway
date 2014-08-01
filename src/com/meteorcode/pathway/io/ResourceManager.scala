package com.meteorcode.pathway.io


object ResourceManager {
  def read (path: String): FileHandle = {
    //TODO: Stub
    return new DesktopFileHandle(path)
  }
}