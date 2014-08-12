package com.meteorcode.pathway.model;

/**
 * Represents a 2D grid of {@link com.meteorcode.pathway.model.Tile Tiles}, which may be occupied by
 * {@link com.meteorcode.pathway.model.Entity Entities}, with an associated
 * {@link com.meteorcode.pathway.model.Context Context} for evaluating scripts.
 * @author Hawk Weisman
 */
public class Grid {

    public Grid(int size, String name) {
        this.grid = new Tile[size][size];
        this.name = name;
        this.context = new Context(name);
    }

    public Grid (int size) {
        new Grid(size, name = this.getClass().getSimpleName());
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
