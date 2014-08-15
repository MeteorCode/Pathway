package com.meteorcode.pathway.model
import scala.collection.JavaConversions._
/**
 * An [[com.meteorcode.pathway.model.Event Event]] that represents an [[com.meteorcode.pathway.model.Entity Entity]]'s attempt to
 * move from one [[com.meteorcode.pathway.model.Tile Tile]] to another.
 *
 * ==How to Use==
 *
 * A Move provides the following [[com.meteorcode.pathway.model.Payload Payload]] when inspected by a
 * [[com.meteorcode.pathway.model.Property Property]]:
 *  - `"who": Entity` - the [[com.meteorcode.pathway.model.Entity Entity]] that is attempting the move.
 *  - `"to": Tile ` - the [[com.meteorcode.pathway.model.Tile Tile]] that `"who"` wants to move to.
 *  - `"from": Tile ` - the [[com.meteorcode.pathway.model.Tile Tile]] that `"who"` currently occupies
 *  - `"location": Tile ` - the [[com.meteorcode.pathway.model.Tile Tile]] that originated this Event (should be the same as
 *  `"from"`)
 *
 * Note that when a Move evaluates itself, it will check if the target tile is
 * [[com.meteorcode.pathway.model.Tile.occupied() occupied()]]. If the tile is occupied, the Move will
 * [[com.meteorcode.pathway.model.Event.invalidate() invalidate()]] itself. This shouldn't make a difference, as the
 * [[com.meteorcode.pathway.model.Context.pump() pump]]ing [[com.meteorcode.pathway.model.Context Context]] has already
 * [[scala.collection.mutable.Stack.pop() popped]]it off the `EventStack`, and will just quietly not take place. However,
 * if anything cares about validity at that point, do note that the Move will be invalid.
 *
 * This is provided primarily for convenience, and represents essentially the simplest possible interpretation of the
 * idea of a character or monster moving from one game grid square to another. If your game's rule-set has more complex
 * rules for moving, you have several options:
 *
 *  1. Add [[com.meteorcode.pathway.model.Property Properties]] to the game that watch for [[com.meteorcode.pathway.model.Move Move]]
 *  events that match some criterion, and [[com.meteorcode.pathway.model.Event.invalidate() invalidate()]] them if they are illegal, or [[com.meteorcode.pathway.model.Event.patchPayload() patch]] their payload with some
 *  additional data. This is probably the Right Thing (I might even go as far as to refer to it as "idiomatic Pathway".
 *  Note that [[com.meteorcode.pathway.model.Move Move]] stores its data (original tile, target tile, entity that's moving)
 *  in a Payload rather than as fields in the Move class specifically so that you can interact with Moves like any other
 *
 *  2. Extend [[com.meteorcode.pathway.model.Move Move ]] with different subclasses representing the different types of Move
 *  in your game's rule set. This is also the Right Thing, if the circumstances call for it.
 *
 *  3. Completely ignore our Move class and write your own. You should really only have to do this if you aren't using
 *  our [[com.meteorcode.pathway.model.Grid Grid]]/[[com.meteorcode.pathway.model.Tile Time]] system at all. Otherwise, I'd
 *  recommend avoiding this, if possible.
 *
 * @author Hawk Weisman
 */
class Move(
            to: Tile,
            from: Tile,
            who: Entity,
            where: Grid
            )
  extends Event(
    "Move " + who + " from " + from + " to "  + to,
    Map("from" -> from, "to" -> to, "Entity" -> who),
    where.getContext,
    from
  ) {

  def this(from: GridCoordinates,
           to: GridCoordinates,
           who: Entity,
           where: Grid) = this(where.getTileAt(to), where.getTileAt(from), who, where)

  def evalEvent(): Unit = {
    // assign all values from the payload to variables of the correct type
    val to: Tile = this.getPayload.get("to").asInstanceOf[Tile]
    val from: Tile = this.getPayload.get("from").asInstanceOf[Tile]
    val who: Entity = this.getPayload.get("who").asInstanceOf[Entity]
    if (to.occupied) {
      this.invalidate() // if the location we are moving to is occupied, this move is invalid
    } else { // if the target is free, then we can move
      from.setEntity(null) // set the Entity occupying the previous tile to null
      to.setEntity(who) // set the Entity occupying the targeted tile to this
    }
  }
}
