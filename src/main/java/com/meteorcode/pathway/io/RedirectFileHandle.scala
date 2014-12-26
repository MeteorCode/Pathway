package com.meteorcode.pathway.io

import java.io.{IOException, File, OutputStream, InputStream}
import java.util

/**
 * Wraps another FileHandle with a different virtual path. This is for internal use only.
 *
 * Created by hawk on 8/27/14.
 */
protected[io] class RedirectFileHandle (protected val wrapped: FileHandle,
                                        virtualPath: String//,
                                        //token: IOAccessToken
                                        ) extends FileHandle(virtualPath, wrapped.manager//, token
                                        ) {
  /** Returns true if the file exists. */
  override def exists: Boolean = wrapped.exists

  /**
   * Returns the [[File]] backing this file handle.
   * @return a [[File]] that represents this file handle, or null if this file is inside a Jar or Zip archive.
   */
  override protected[io] def file: File = wrapped.file

  /**
   * Returns the length of this file in bytes, or 0 if this FileHandle is a directory or does not exist
   * @return the length of the file in bytes, or 0 if this FileHandle is a directory or does not exist
   */
  override def length: Long = wrapped.length

  /** Returns an [[OutputStream]] for writing to this file.
    * @return an [[OutputStream]] for writing to this file, or null if this file is not writable.
    * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
    * @throws IOException if something went wrong while opening the file.
    */
  override def write(append: Boolean): OutputStream = wrapped.write(append)

  /**
   * Delete this file if it exists and is writable.
   * @return true if this file was successfully deleted, false if it could not be deleted
   */
  override def delete: Boolean = wrapped.delete

  /** Returns true if this file is a directory.
    *
    * Note that this may return false if a directory exists but is empty.
    * This is Not My Fault, it's [[util.File]] behaviour.
    *
    * @return true if this file is a directory, false otherwise
    **/
  override def isDirectory: Boolean = wrapped.isDirectory

  /**
   * @return a list containing FileHandles to the contents of FileHandle, or an empty list if this file is not a
   *         directory or does not have contents.
   */
  override def list: util.List[FileHandle] = wrapped.list

  /** @return a [[InputStream]] for reading this file, or null if the file does not exist or is a directory.
    */
  override def read: InputStream = wrapped.read

  /** Returns true if this FileHandle represents something that can be written to */
  override def writable: Boolean = wrapped.writable

  /**
   * Returns the physical path to the actual filesystem object represented by this FileHandle.
   */
  override protected[io] def physicalPath: String = wrapped.physicalPath
}
