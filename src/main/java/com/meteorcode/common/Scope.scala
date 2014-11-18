package com.meteorcode.common

/**
 * Represents something that corresponds to a scope with a parent and children
 *
 * This is a generalized version fo the `ScopeNode`s in the Decaf compiler.
 *
 * @author Hawk Weisman
 */
trait Scope {
  var children = List[Scope]()
  var parent: Option[Scope] = ???

  /**
   * Fork the scopable thing into a child scope
   * @return a new Scope corresponding to the forked child scope
   */
  protected def fork: Scope = ???

  /**
   * @return a new child of this scope
   */
  def child(): Scope = {
    val c = this.fork
    children = children :+ c
    c
  }

  private def removeChild(other: Scope): Unit = {
    this.children = children.filter({other != _})
  }

  private def addChild(other: Scope): Unit = {
    this.children = this.children :+ other
  }

  /**
   * Change the parent corresponding to this scope.
   * @param nParent the new parent
   * @throws IllegalArgumentException if the specified parent was invalid
   */
  @throws[IllegalArgumentException]("if the specified parent was invalid")
  def reparent(nParent: Scope) = if (nParent == this) {
    throw new IllegalArgumentException ("Scope attempted to mount itself as parent!")
  } else {
    if (this.parent.isDefined) {
    val oldParent = this.parent.get
    oldParent.removeChild (this)
  }
    nParent.addChild (this)
  }
}