package graphics.ui;

import java.awt.Color;

import entity.dynamic.mob.work.BuildingRecipe;
import graphics.opengl.OpenGLUtils;
import input.PointerInput;
import map.Level;
import map.Tile;

//the green or red outline used to select where to build things
public class BuildOutline {

	private Color buildable = new Color(78, 244, 66, 210); // green
	private Color notbuildable = new Color(244, 66, 66, 210); // red
	private int buildSquareXS; // x coord of the start in the game world
	private int buildSquareYS; // y coord of the start in the game world
	private int buildSquareXSTeken; // x coord of the start on the screen
	private int buildSquareYSTeken; // y coord of the start on the screen
	private int buildSquareXE; // x coord of the end in the game world
	private int buildSquareYE; // y coord of the end in the game world
	private final int WIDTH = Tile.SIZE; // width of a square
	private int squarewidth; // width of the outline in squares
	private int squareheight; // height of the outline in squares
	private boolean visible; // is the outline visible
	private Level[] levels; // the map is needed to decide if a square is empty
	private boolean lockedSize = false;
	private BuildingRecipe build;
	private int z = 0;

	// rendering the outline
	public void render(float xOffset, float yOffset) {
		if (visible) {
			if (lockedSize || buildSquareXE == 0 && buildSquareYE == 0) {
				OpenGLUtils.buildOutlineDraw(buildSquareXSTeken, buildSquareYSTeken, WIDTH, xOffset, yOffset,
					notBuildable((buildSquareXS / Tile.SIZE), buildSquareYS / Tile.SIZE, z) ? notbuildable : buildable
				);
				return;
			}
			if (squarewidth > squareheight) {
				if (buildSquareXSTeken < buildSquareXE) { // START LEFT OF END == DRAG TO RIGHT
					for (int i = 0; i < squarewidth; i++) {
						OpenGLUtils.buildOutlineDraw(buildSquareXSTeken + (i * WIDTH), buildSquareYSTeken,WIDTH, xOffset, yOffset,
							notBuildable((buildSquareXS /Tile.SIZE) + i, (buildSquareYS /Tile.SIZE), z) ? notbuildable : buildable
						);
					}
				} else { // START RIGHT OF END == DRAG TO LEFT
					for (int i = 0; i < squarewidth; i++) {
						OpenGLUtils.buildOutlineDraw(buildSquareXSTeken - (WIDTH * (squarewidth - 1)) + (i * WIDTH), buildSquareYSTeken, WIDTH, xOffset, yOffset,
							notBuildable(((buildSquareXS) - (WIDTH * (squarewidth - 1)) /Tile.SIZE) + i, (buildSquareYS / Tile.SIZE), z) ? notbuildable : buildable
						);
					}
				}
			} else {
				if (buildSquareYSTeken < buildSquareYE) { // START ABOVE END == DRAG DOWN
					for (int i = 0; i < squareheight; i++) {
						OpenGLUtils.buildOutlineDraw(buildSquareXSTeken,buildSquareYSTeken + (WIDTH * i), WIDTH * (squareheight - i), xOffset, yOffset,
							notBuildable((buildSquareXS /Tile.SIZE), ((buildSquareYS) /Tile.SIZE) + i, z) ? notbuildable : buildable
						);
					}
				} else { // START BELOW END == DRAG UP
					for (int i = 0; i < squareheight; i++) {
						OpenGLUtils.buildOutlineDraw(buildSquareXSTeken,buildSquareYSTeken - (WIDTH * (squareheight - 1)) + (i * WIDTH), WIDTH, xOffset, yOffset,
							notBuildable((buildSquareXS /Tile.SIZE), (buildSquareYS - (WIDTH * (squareheight - 1)) /Tile.SIZE) + i, z) ? notbuildable : buildable
						);
					}
				}

			}
		}
	}

	// is the tile empty
	private boolean notBuildable(int x, int y, int z) {
		return (levels[z].getTile(x, y).isSolid());

	}

	// getters
	 int[][] getSquareCoords() {
		int[][] coords;
		if (buildSquareXE == 0 && buildSquareYE == 0) {
			coords = new int[1][2];
			coords[0][0] = buildSquareXS;
			coords[0][1] = buildSquareYS;
		} else {
			if (squarewidth > squareheight) {
				if (buildSquareXSTeken < buildSquareXE) { // START LEFT OF END == DRAG RIGHT
					coords = new int[squarewidth][2];
					for (int i = 0; i < squarewidth; i++) {
						coords[i][0] = buildSquareXS + (i * Tile.SIZE);
						coords[i][1] = buildSquareYS;
					}
				} else { // START RIGHT OF END == DRAG LEFT
					coords = new int[squarewidth][2];
					for (int i = 0; i < squarewidth; i++) {
						coords[i][0] = (((buildSquareXS - (WIDTH * (squarewidth - 1))))) + (i * Tile.SIZE);
						coords[i][1] = buildSquareYS;
					}
				}
			} else {
				if (buildSquareYSTeken < buildSquareYE) { // START ABOVE END == DRAG DOWN
					coords = new int[squareheight][2];
					for (int i = 0; i < squareheight; i++) {
						coords[i][0] = buildSquareXS;
						coords[i][1] = buildSquareYS + (i * Tile.SIZE);
					}
				} else { // START BELOW END == DRAG UP
					coords = new int[squareheight][2];
					for (int i = 0; i < squareheight; i++) {
						coords[i][0] = buildSquareXS;
						coords[i][1] = buildSquareYS - (WIDTH * (squareheight - 1)) + (i * Tile.SIZE);
					}
				}
			}
		}
		return coords;

	}

	boolean isVisible() {
		return visible;
	}

	// update the outline
	public void update(PointerInput pointer, int xOff, int yOff, boolean force, int z) {
		this.z = z;
		if (visible || force) { // TODO fix this aids
			buildSquareXS = (pointer.getTileX() * Tile.SIZE);
			buildSquareXSTeken = (pointer.getTileX() * Tile.SIZE)-xOff;
			buildSquareYS = (pointer.getTileY() * Tile.SIZE);
			buildSquareYSTeken = (pointer.getTileY() * Tile.SIZE)-yOff;
			squarewidth = 1;
			squareheight = 1;
			buildSquareXE = 0;
			buildSquareYE = 0;
		}
	}

	public void update(PointerInput pointer, int xOff, int yOff,int z) {
		update(pointer, xOff, yOff, false,z);
	}

	BuildingRecipe getBuildRecipe() {
		return build;
	}

	// constructor
	BuildOutline(Level[] level) {
		setLevels(level);
	}

	public void setLevels(Level[] levels) {
		this.levels = levels;
	}

	// show the outline
	void show(PointerInput pointer, int xoff, int yoff, int z ,boolean lockedSize, BuildingRecipe build) {
		if (!visible) {
			update(pointer, xoff, yoff, true,z);
			visible = true;
			buildSquareXS = pointer.getTileX() *Tile.SIZE;
			buildSquareYS = pointer.getTileY() *Tile.SIZE;
			squarewidth = 1;
			squareheight = 1;
			this.build = build;
			this.lockedSize = lockedSize;
		}
	}

	// hide the outline
	void remove() {
		visible = false;
	}

}
