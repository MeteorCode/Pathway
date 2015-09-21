package com.meteorcode.pathway.test

import com.meteorcode.pathway.io

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

/**
 * Created by hawk on 8/14/15.
 */
object TempoRedscreenTest {

  def die(msg: String): Unit = {
    System.err.println(msg)

    System.exit(1)
  }

  def main(args: Array[String])
    = io.Unpacker.unpackNatives() onComplete {
      case Failure(why) ⇒
        why.printStackTrace()
        die(why.toString)
      case _ ⇒
        import org.lwjgl.Sys
        println(s"Using LWJGL version ${Sys.getVersion}")

        import org.lwjgl.glfw.Callbacks._
        import org.lwjgl.glfw.GLFW._
        import org.lwjgl.glfw.GLFWKeyCallback
        import org.lwjgl.opengl.GL11._
        import org.lwjgl.opengl.{GL11, GLContext}
        import org.lwjgl.system.MemoryUtil._

        val errorCallback = errorCallbackPrint(System.err)
        try {
          glfwSetErrorCallback(errorCallback)

          if (glfwInit() != GL11.GL_TRUE) {
            die("Unable to init GLFW")
          }

          glfwDefaultWindowHints()
          glfwWindowHint(GLFW_VISIBLE, GL_FALSE)
          glfwWindowHint(GLFW_RESIZABLE, GL_FALSE)

          val WIDTH = 300
          val HEIGHT = 300

          val window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL)
          if (window == NULL) {
            die("Failed to create GLFW window")
          }

          val keyCallback = new GLFWKeyCallback {
            override def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
              if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, GL_TRUE)
              }
            }
          }

          glfwSetKeyCallback(window, keyCallback)

          glfwMakeContextCurrent(window)
          glfwSwapInterval(1)
          glfwShowWindow(window)

          GLContext.createFromCurrent()

          glClearColor(1, 0, 0, 0)
          while (glfwWindowShouldClose(window) == GL_FALSE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
            glfwSwapBuffers(window)
            glfwPollEvents()
          }
        } finally {
          glfwTerminate()
          if (errorCallback != null) errorCallback.release()
        }
    }
}
