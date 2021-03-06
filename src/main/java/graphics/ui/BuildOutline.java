package graphics.ui;

import entity.dynamic.mob.work.recipe.BuildingRecipe;
import graphics.opengl.OpenGLUtils;
import input.PointerInput;
import input.PointerMoveEvent;
import map.Level;
import map.Tile;
import util.vectors.Vec2f;
import util.vectors.Vec4f;

//the green or red outline used to select where to build things
public class BuildOutline {

	private final Vec4f buildable = new Vec4f(0.3058f, 0.9568f, 0.2588f, 0.8235f); // green
	private final Vec4f notbuildable = new Vec4f(0.9568f, 0.2588f, 0.2588f, 0.8235f); // red
	private float buildSquareXS; // x coord of the start in the game world
	private float buildSquareYS; // y coord of the start in the game world
	private int buildSquareXSTeken; // x coord of the start on the screen
	private int buildSquareYSTeken; // y coord of the start on the screen
	private float buildSquareXE; // x coord of the end in the game world
	private float buildSquareYE; // y coord of the end in the game world
	private final int WIDTH = Tile.SIZE; // width of a square
	private int squarewidth; // width of the outline in squares
	private int squareheight; // height of the outline in squares
	private boolean visible; // is the outline visible
	private Level[] levels; // the map is needed to decide if a square is empty
	private boolean lockedSize = false;
	private BuildingRecipe build;
	private int z = 0;
	private float xScroll;
	private float yScroll;

	//TODO all these location x and y need to be floats

	// rendering the outline
	public void render(Vec2f offset) {
		if (visible) {
			if (lockedSize || buildSquareXE == 0 && buildSquareYE == 0) {
				OpenGLUtils.buildOutlineDraw(new Vec2f(buildSquareXSTeken, buildSquareYSTeken), WIDTH, offset,
					notBuildable(buildSquareXS, buildSquareYS, z) ? notbuildable : buildable
				);
				return;
			}
			if (squarewidth > squareheight) {
				if (buildSquareXSTeken < buildSquareXE) { // START LEFT OF END == DRAG TO RIGHT
					for (int i = 0; i < squarewidth; i++) {
						OpenGLUtils.buildOutlineDraw(new Vec2f(buildSquareXSTeken + i, buildSquareYSTeken), WIDTH, offset,
							notBuildable(buildSquareXS + i, buildSquareYS, z) ? notbuildable : buildable
						);
					}
				} else { // START RIGHT OF END == DRAG TO LEFT
					for (int i = 0; i < squarewidth; i++) {
						OpenGLUtils.buildOutlineDraw(new Vec2f(buildSquareXSTeken - squarewidth - 1 + i, buildSquareYSTeken), WIDTH, offset,
							notBuildable(buildSquareXS - squarewidth - 1 + i, buildSquareYS, z) ? notbuildable : buildable
						);
					}
				}
			} else {
				if (buildSquareYSTeken < buildSquareYE) { // START ABOVE END == DRAG DOWN
					for (int i = 0; i < squareheight; i++) {
						OpenGLUtils.buildOutlineDraw(new Vec2f(buildSquareXSTeken,buildSquareYSTeken + i), squareheight - i, offset,
							notBuildable(buildSquareXS, buildSquareYS + i, z) ? notbuildable : buildable
						);
					}
				} else { // START BELOW END == DRAG UP
					for (int i = 0; i < squareheight; i++) {
						OpenGLUtils.buildOutlineDraw(new Vec2f(buildSquareXSTeken,buildSquareYSTeken - squareheight - 1 + i), WIDTH, offset,
							notBuildable(buildSquareXS, buildSquareYS - squareheight - 1 + i, z) ? notbuildable : buildable
						);
					}
				}

			}
		}
	}

	// is the tile empty
	private boolean notBuildable(float x, float y, int z) {
		return !levels[z].tileIsEmpty((int) x/Tile.SIZE, (int) y/Tile.SIZE);

	}

	// getters
	 float[][] getSquareCoords() {
		 float[][] coords;
		if (buildSquareXE == 0 && buildSquareYE == 0) {
			coords = new float[1][2];
			coords[0][0] = buildSquareXS;
			coords[0][1] = buildSquareYS;
		} else {
			if (squarewidth > squareheight) {
				if (buildSquareXSTeken < buildSquareXE) { // START LEFT OF END == DRAG RIGHT
					coords = new float[squarewidth][2];
					for (int i = 0; i < squarewidth; i++) {
						coords[i][0] = buildSquareXS + (i * Tile.SIZE);
						coords[i][1] = buildSquareYS;
					}
				} else { // START RIGHT OF END == DRAG LEFT
					coords = new float[squarewidth][2];
					for (int i = 0; i < squarewidth; i++) {
						coords[i][0] = (((buildSquareXS - (WIDTH * (squarewidth - 1))))) + (i * Tile.SIZE);
						coords[i][1] = buildSquareYS;
					}
				}
			} else {
				if (buildSquareYSTeken < buildSquareYE) { // START ABOVE END == DRAG DOWN
					coords = new float[squareheight][2];
					for (int i = 0; i < squareheight; i++) {
						coords[i][0] = buildSquareXS;
						coords[i][1] = buildSquareYS + (i * Tile.SIZE);
					}
				} else { // START BELOW END == DRAG UP
					coords = new float[squareheight][2];
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

	private void update(PointerMoveEvent e) {
		buildSquareXS = (float) ((int)(e.x + xScroll) / Tile.SIZE * Tile.SIZE);
		buildSquareXSTeken = (int) (buildSquareXS - xScroll);
		buildSquareYS = (float) ((int) (e.y + yScroll) / Tile.SIZE * Tile.SIZE);
		buildSquareYSTeken = (int) (buildSquareYS - yScroll);

		if (visible) {
			squarewidth = 1;
			squareheight = 1;
			buildSquareXE = 0;
			buildSquareYE = 0;
		}
	}

	// update the outline
	public void update(int z, float xScroll, float yScroll) {
		this.z = z;
		this.xScroll = xScroll;
		this.yScroll = yScroll;
	}

	BuildingRecipe getBuildRecipe() {
		return build;
	}

	// constructor
	BuildOutline(Level[] level, PointerInput pointer) {
		setLevels(level);
		pointer.on(PointerInput.EType.MOVE, this::update);
	}

	public void setLevels(Level[] levels) {
		this.levels = levels;
	}

	// show the outline
	void show(int z,  float xScroll, float yScroll, boolean lockedSize, BuildingRecipe build) {
		if (!visible) {
			visible = true;
			update(z, xScroll, yScroll);
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
