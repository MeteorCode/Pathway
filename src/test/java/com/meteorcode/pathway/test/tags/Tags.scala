package com.meteorcode.pathway.test
package tags

/**
 * Created by hawk on 6/1/15.
 */
import org.scalatest.Tag

/**
 * Tag for all tests that require access to the filesystem. These cannot be performed in
 * some environments, so I figured that it would be best to preemptively tag them so that
 * they can be excluded.
 */
object FilesystemTest extends Tag("com.meteorcode.pathway.test.FilesystemTest")