package com.meteorcode.pathway.model;

public class Grid {

    public Grid(int size, String name) {
        this.grid = new Tile[size][size];
        this.name = name;
        this.context = new Context(name)
    }

    public Grid (int size) {
        new Grid(size, name = this.getClass().getSimpleName());
    }

	//
	private Tile[][] grid;



    private String name;
    private Context context;

	/**
	 * Returns the tile at the specified position.
	 * @author Hawk Weisman
	 * @param 	x	the x-position of the target tile
	 * @param 	y	the y-position of the target tile
	 * @return the tile at the specified position
	 */
	public Tile getTileAt(int x, int y) {
		return grid[x][y];
	}

	/**
	 * Returns the tile at the specified position.
	 * @author Hawk Weisman
	 * @param position a GridCoordinates object containing the coordinates of the targeted position.
	 * @return the tile at the specified position
	 */
	public Tile getTileAt(GridCoordinates position) {
		return grid[position.getX()][position.getY()];
	}

	/**
	 * Insert a tile at the specified position.
	 * @author Hawk Weisman
	 * @param x the x-position of the target tile
	 * @param y	the y-position of the target tile
	 * @param tile the Tile to insert at the specified position
	 */
	public void setTileAt(int x, int y, Tile tile) {
		this.grid[x][y] = tile;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Context getContext() {
        return this.context;
    }
}
