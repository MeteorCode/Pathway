package com.meteorcode.pathway

import java.nio.file.Paths

import com.meteorcode.pathway.graphics.{ createGraphicsContext
                                       , GraphicsContext }
import com.meteorcode.pathway.io.{ FilesystemFileHandle
                                 , FileHandle
                                 , Unpacker
                                 , ResourceManager }

import scala.concurrent.Future

/**
 * Created by hawk on 9/21/15.
 */
object Bootstrap {

  val GAME_DIR_NAME: String // change if the name of the game data dir changes
    = "game-data"           // TODO: should this be read from a config file?

  lazy val gameDirPath
    = Paths.get("")
           .resolve(GAME_DIR_NAME)
           .toAbsolutePath

  lazy val initFileHandles: Future[Seq[FileHandle]]
    = Future {
        gameDirPath.toFile
                   .listFiles
                   .map { f ⇒
                     new FilesystemFileHandle( f.getName
                                             , f.getAbsolutePath
                                             , f
                                             , null // ugh
                                             )
                   }
      }

  def bootstrap(): Future[Unit]
    = for { graphics ← Unpacker.unpackNatives()
                               .flatMap { _ ⇒ createGraphicsContext }
            handles ← initFileHandles
            manager ← Future { new ResourceManager(handles) }
          } yield ()

}
