package com.meteorcode.pathway.model;

/**
 * <p>Represents a 2D grid of {@link com.meteorcode.pathway.model.Tile Tiles}, which may be occupied by
 * {@link com.meteorcode.pathway.model.Entity Entities}, with an associated
 * {@link com.meteorcode.pathway.model.Context Context} for evaluating scripts.</p>
 *
 * <p>Grids modeled by this class are backed by a two-dimensional array of Tiles with a specified size. This is to make
 * performing basic grid operations (such as
 * {@link com.meteorcode.pathway.model.Grid#getTileAt(int, int) getTileAt(x,y)}) fast. Note, however, that this means
 * that the Grid will always take up the same amount of space in RAM, even if most of the coordinate pairs are unused.
 * If your game's needs would be better suited by a sparse grid, consider extending this class with a new Grid
 * implementation backed by a {@link java.util.LinkedList linked list} or (if you're really ambitious) a sparse array
 * implementation. This also means means that a Grid cannot grow in size once it has been created. If you need that
 * functionality for your game, and are willing to sacrifice a significantly slower access time, a new Grid
 * implementation backed by a {@link java.util.LinkedList linked list} might be your best bet.
 * Finally, note that all Grids are square. If you really, really need rectangular grids,  I suggest you write your own
 * rectangular grid implementation.
 * </p>
 * <p>
 * If you actually take my advice and write a new Grid implementation, please be sure to make a pull request on the
 * <a href="https://github.com/MeteorCode/pathway">GitHub repository</a> for Pathway. And, if you need help or want to
 * discuss Pathway's grid system, you can contact me at <a href="mailto://hawk@meteorcode.com">hawk@meteorcode.com</a>.
 * </p>
 * @author Hawk Weisman
 */
public class Grid {

    /**
     * Constructor for a Grid with it's own {@link com.meteorcode.pathway.model.Context Context}.
     *
     * <p>A new {@link com.meteorcode.pathway.model.Context Context} will be created for this grid automatically.</p>
     * @param size an integer specifying the size of the grid in the x/y dimension
     * @param name this Grid's name.
     */
    public Grid(int size, String name) {
        new Grid(size, name, new Context(name));
    }

    /**
     * Constructor for a Grid with its' own {@link com.meteorcode.pathway.model.Context Context} and the default name.
     *
     * <p>A new {@link com.meteorcode.pathway.model.Context Context} will be created for this Grid automatically, and
     * the name will be set to the class name (Grid, if this Grid is a grid, or the name of the extending class if this
     * is an instance of some class that extends Grid).</p>
     * @param size an integer specifying the size of the grid in the x/y dimension
     */
    public Grid (int size) {
        new Grid(size, name = this.getClass().getSimpleName());
    }

    /**
     * Constructor to attach a Grid to an existing {@link com.meteorcode.pathway.model.Context Context}.
     * @param size an integer specifying the size of the grid in the x/y dimension
     * @param name this Grid's name
     * @param context the {@link com.meteorcode.pathway.model.Context scripting context} which contains this Grid
     */
    public Grid(int size, String name, Context context) {
        this.grid = new Tile[size][size];
        this.name = name;
        this.context = context;
    }


	private Tile[][] grid;
    private String name;
    private Context context;

	/**
	 * Returns the tile at the specified position.
	 * @param 	x	the x-position of the target tile
	 * @param 	y	the y-position of the target tile
	 * @return the tile at the specified position
	 */
	public Tile getTileAt(int x, int y) {
		return grid[x][y];
	}

	/**
	 * Returns the tile at the specified position.
	 * @param position a GridCoordinates object containing the coordinates of the targeted position.
	 * @return the tile at the specified position
	 */
	public Tile getTileAt(GridCoordinates position) {
		return grid[position.getX()][position.getY()];
	}

	/**
	 * Insert a tile at the specified position.
	 * @param x the x-position of the target tile
	 * @param y	the y-position of the target tile
	 * @param tile the Tile to insert at the specified position
	 */
	public void setTileAt(int x, int y, Tile tile) {
		this.grid[x][y] = tile;
	}

    /**
     * @return this Grid's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets this Grid's name
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a reference to this Grid's associated {@link com.meteorcode.pathway.model.Context scripting context}.
     * @return this Grid's associated {@link com.meteorcode.pathway.model.Context scripting context}.
     */
    public Context getContext() {
        return this.context;
    }
}
