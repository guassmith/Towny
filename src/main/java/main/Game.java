package main;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import entity.Entity;
import entity.dynamic.mob.work.*;
import entity.dynamic.mob.work.recipe.BuildingRecipe;
import entity.dynamic.mob.work.recipe.ItemRecipe;
import entity.nonDynamic.building.container.Chest;
import entity.nonDynamic.building.container.Container;
import entity.nonDynamic.resources.Ore;
import entity.nonDynamic.resources.Tree;
import entity.dynamic.item.Clothing;
import entity.dynamic.item.Item;
import entity.dynamic.item.ItemHashtable;
import entity.dynamic.item.weapon.Weapon;
import entity.dynamic.item.weapon.WeaponMaterial;
import entity.dynamic.mob.Mob;
import entity.dynamic.mob.Villager;
import entity.dynamic.mob.Zombie;
import entity.nonDynamic.building.container.workstations.Anvil;
import entity.nonDynamic.building.container.workstations.Furnace;
import entity.pathfinding.PathFinder;
import graphics.*;
import graphics.opengl.OpenGLUtils;
import graphics.opengl.TrueTypeFont;
import graphics.ui.Ui;
import graphics.ui.menu.MenuItem;
import input.Keyboard;
import input.PointerInput;
import map.Level;
import map.Tile;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import util.StringUtils;
import util.TextureInfo;
import util.vectors.Vec2f;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;

public class Game {

	public static final int width = 1500;
	public static final int height = (int)(width / 16f * 9f); //843.75
	public Level[] map;
	private ArrayList<Villager> vills;
	private ArrayList<Villager> sols;
	private ArrayList<Mob> mobs;
	private Ui ui;
	private boolean paused = false;
	private byte speed = 60;
	private double ns = 1000000000.0 / speed;
	private Villager selectedvill;
	public float xScroll = 0;
	public float yScroll = 0;
	public int currentLayerNumber = 0;
	private long window;
	private PointerInput pointer;

	public static void main(String[] args) {
		try {
			new Game();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Game() throws Exception {
		init();
		loop();
	}

	private void init() throws Exception {
		if (!glfwInit()) {
			System.err.println("GLFW failed to initialize");
			return;
		}

		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);

		window = glfwCreateWindow(width, height, "Towny by Bramelvix", 0, 0);

		if (window == 0) {
			System.err.println("Window failed to be created");
			return;
		}
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		if (vidmode == null) {
			System.err.println("Vidmode is null");
			return;
		}
		glfwSetWindowPos(window, (vidmode.width() - (width)) / 2, (vidmode.height() - (height )) / 2);
		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		GL.createCapabilities();
		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		glfwSwapInterval(0); //0 = VSYNC OFF, 1= VSYNC ON
		setIcon();
		glfwSetKeyCallback(window, new Keyboard());
		PointerInput.configure (this);
		this.pointer = PointerInput.getInstance ();
		glfwSetMouseButtonCallback(window, pointer.buttonsCallback ());
		glfwSetCursorPosCallback(window, pointer.positionCallback ());
		glfwSetScrollCallback(window, this::scroll);



		SpritesheetHashtable.registerSpritesheets();
		SpriteHashtable.registerSprites();
		ItemHashtable.registerItems();

		OpenGLUtils.init();

		//Sound.initSound();
		TrueTypeFont.init();
		generateLevel();
		mobs = new ArrayList<>();
		vills = new ArrayList<>();
		sols = new ArrayList<>();

		initUi();

		PathFinder.init(100, 100);
		spawnvills();
		spawnZombies();
	}

	private void setIcon() {
		TextureInfo textureInfo = OpenGLUtils.loadTexture("/icons/soldier.png");
		ByteBuffer buffer = textureInfo.buffer;
		GLFWImage image = GLFWImage.malloc();
		image.set(textureInfo.width, textureInfo.height, buffer);
		GLFWImage.Buffer images = GLFWImage.malloc(1);
		images.put(0, image);
		glfwSetWindowIcon(window, images);

		images.free();
		image.free();
	}

	private void generateLevel() {
		map = new Level[20];
		for (int i = 0; i < map.length; i++) {
			map[i] = new Level(100, 100, i);
		}
	}

	private void spawnvills() {
		Villager vill = new Villager(432, 432, 0, map);
		vill.addClothing((Clothing) ItemHashtable.get(61));
		vill.addClothing((Clothing) ItemHashtable.get(75));
		addVillager(vill);
		Villager vil2 = new Villager(432, 480, 0, map);
		vil2.addClothing((Clothing) ItemHashtable.get(65));
		vil2.addClothing((Clothing) ItemHashtable.get(74));
		addVillager(vil2);
		Villager vil3 = new Villager(480, 480, 0, map);
		vil3.addClothing((Clothing) ItemHashtable.get(70));
		vil3.addClothing((Clothing) ItemHashtable.get(73));
		addVillager(vil3);
	}

	private void spawnZombies() {
		int teller = Entity.RANDOM.nextInt(5) + 1;
		for (int i = 0; i < teller; i++) {
			mobs.add(new Zombie(map, Entity.RANDOM.nextInt(768/ Tile.SIZE)* Tile.SIZE + Tile.SIZE, Entity.RANDOM.nextInt(768/ Tile.SIZE)*Tile.SIZE + Tile.SIZE, 0));
		}
	}

	private void addVillager(Villager vil) {
		if (!vills.contains(vil)) {
			sols.remove(vil);
			vills.add(vil);
			ui.updateCounts(sols.size(), vills.size());
		}
	}

	private void addSoldier(Villager vil) {
		if (!sols.contains(vil)) {
			vills.remove(vil);
			sols.add(vil);
			ui.updateCounts(sols.size(), vills.size());
		}
	}

	private void loop() {
		long lastTime = System.nanoTime();
		double delta = 0;
		long now;
		GL.createCapabilities();
		while (!glfwWindowShouldClose(window)) {
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;

			while (delta >= 1) {
				glfwPollEvents();
				updateUI();
				if (!paused) {
					updateMobs();
				}
				delta--;
			}

			draw();
		}
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	private void draw() {
		OpenGLUtils.clearOutlines();
		OpenGLUtils.clearAllInstanceData();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(0.2f, 0.2f, 0.2f, 0.2f);
		Vec2f scroll = new Vec2f(xScroll, yScroll);
		map[currentLayerNumber].render(scroll);
		renderMobs();

		OpenGLUtils.drawInstanced(OpenGLUtils.tileData, new Vec2f(Sprite.SIZE), scroll);

		if(OpenGLUtils.entityData.getInstances() > 0) {
			OpenGLUtils.drawInstanced(OpenGLUtils.entityData, new Vec2f(Sprite.SIZE), scroll);
		}

		if(OpenGLUtils.itemData.getInstances() > 0) {
			OpenGLUtils.drawInstanced(OpenGLUtils.itemData, new Vec2f(Sprite.SIZE), scroll);
		}

		if(OpenGLUtils.mobData.getInstances() > 0) {
			OpenGLUtils.drawInstanced(OpenGLUtils.mobData, new Vec2f(Tile.SIZE), new Vec2f(xScroll, yScroll));
		}

		if(OpenGLUtils.hardEntityData.getInstances() > 0) {
			OpenGLUtils.drawInstanced(OpenGLUtils.hardEntityData, new Vec2f(Sprite.SIZE), scroll);
		}

		OpenGLUtils.drawOutlines(scroll);
		ui.render(currentLayerNumber, speed);
		glfwSwapBuffers(window);
		OpenGLUtils.checkGLError();
	}

	private void updateUI() {
		ui.update(pointer, currentLayerNumber, xScroll, yScroll);
		updateMouse();
		moveCamera();
		pointer.resetLeftAndRight();
	}

	private void initUi() {
		ui = new Ui(map, pointer);
		ui.initLayerLevelChangerActions(pointer, this::onClickLayerUp, this::onClickLayerDown);
		ui.initTopBarActions(pointer, this::onClickPause, this::onClickupSpeed, this::onClickdownSpeed);
		ui.getIcons().setShovelOnClick(pointer, this::onClickShovel);
		ui.getIcons().setPlowOnclick(pointer, this::onClickPlow);
		ui.getIcons().setSawOnClick(pointer, this::onClickSaw);
	}

	private void onClickLayerUp() {
		if (this.currentLayerNumber != 0) {
			this.currentLayerNumber--;
			ui.updateMinimap(map, currentLayerNumber);
		}
	}

	private void onClickLayerDown() {
		if (this.currentLayerNumber != map.length - 1) {
			this.currentLayerNumber++;
			ui.updateMinimap(map, currentLayerNumber);
		}
	}

	private void onClickPause() {
		if (paused) {
			paused = false;
			speed = 60;
		} else {
			paused = true;
			speed = 20;
		}

	}

	private void onClickupSpeed() {
		if (speed < 90) {
			speed+=10;
			ns = 1000000000.0 / speed;
		}
	}

	private void onClickdownSpeed() {
		if (speed > 30) {
			speed-=10;
			ns = 1000000000.0 / speed;
		}
	}

	private void onClickShovel() {
		if (!ui.menuVisible()) {
			deselectAllVills();
			ui.showBuildSquare( true, BuildingRecipe.STAIRSDOWN, currentLayerNumber, xScroll, yScroll);
			ui.deSelectIcons();
		}
	}

	private void onClickPlow() {
		if (!ui.menuVisible()){
			ui.showBuildSquare( false, BuildingRecipe.TILLED_SOIL, currentLayerNumber, xScroll, yScroll);
			ui.deSelectIcons();
		}
	}

	private void onClickBuild(MenuItem item) {
		ui.showBuildSquare(false, item.getRecipe(), currentLayerNumber, xScroll, yScroll);
		ui.deSelectIcons();
		ui.getMenu().hide();
	}

	private void onClickSaw() {
		if (!ui.menuVisible()) {
			deselectAllVills();
			MenuItem[] items = new MenuItem[BuildingRecipe.RECIPES.length + 1];
			for (int i = 0; i < BuildingRecipe.RECIPES.length; i++) {
				items[i] = new MenuItem(BuildingRecipe.RECIPES[i], this::onClickBuild, pointer);
			}
			items[items.length - 1] = new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer);
			ui.showMenu(pointer, items);
		}
	}

	private void onClickMove() {
		selectedvill.resetAll();
		selectedvill.addJob(new MoveJob(pointer.getTileX(), pointer.getTileY(), currentLayerNumber, selectedvill));
		deselect(selectedvill);
		ui.deSelectIcons();
		ui.getMenu().hide();
	}

	private void onClickPickup(MenuItem item) {
		selectedvill.setPath(null);
		Item e = (Item) item.getEntity();
		selectedvill.addJob(new MoveItemJob(e, selectedvill));
		ui.deSelectIcons();
		deselect(selectedvill);
		ui.getMenu().hide();

	}

	private void onClickChop(MenuItem item) {
		selectedvill.setPath(null);
		selectedvill.addJob((Tree) item.getEntity());
		deselect(selectedvill);
		ui.deSelectIcons();
		ui.getMenu().hide();
	}

	private void onClickSmelt() {
		MenuItem[] craftingOptions = new MenuItem[ItemRecipe.FURNACE_RECIPES.length + 1];
		for (int i = 0; i < ItemRecipe.FURNACE_RECIPES.length; i++) {
			craftingOptions[i] = new MenuItem(ItemRecipe.FURNACE_RECIPES[i], this::onClickCraft, pointer);
		}
		craftingOptions[craftingOptions.length - 1] = new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer);
		ui.showMenu(pointer, craftingOptions);

	}

	private void onClickCraft(MenuItem item) {
		Villager idle = getIdlestVil();
		ItemRecipe recipe = item.getRecipe();
		if (recipe != null) {
			idle.setPath(null);
			Item[] res = new Item[recipe.getResources().length];
			for (int i = 0; i < res.length; i++) {
				res[i] = idle.getNearestItemOfType(recipe.getResources()[i]).orElse(null);
			}
			map[currentLayerNumber].getNearestWorkstation(
				recipe.getWorkstationClass(), idle.getTileX(), idle.getTileY()
			).ifPresent(station -> idle.addJob(new CraftJob(idle, res, recipe.getProduct(), station)));
			ui.deSelectIcons();
			ui.getMenu().hide();
		}
	}

	private void onClickSmith() {
		MenuItem[] craftingOptions = new MenuItem[WeaponMaterial.values().length + 1];
		for (int i = 0; i < WeaponMaterial.values().length; i++) {
			craftingOptions[i] = new MenuItem(StringUtils.capitalize(WeaponMaterial.values()[i].toString().toLowerCase()), this::showMaterials, pointer);
		}
		craftingOptions[craftingOptions.length - 1] = new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer);
		ui.showMenu(pointer, craftingOptions);
	}

	private void showMaterials(MenuItem item) {
			ItemRecipe[] recipes = ItemRecipe.smithingRecipesFromWeaponMaterial(WeaponMaterial.valueOf(item.getText().toUpperCase()));
			MenuItem[] craftingOptions = new MenuItem[recipes.length + 1];
			for (int i = 0; i < WeaponMaterial.values().length; i++) {
				craftingOptions[i] = new MenuItem(recipes[i], this::onClickCraft, pointer);
			}
			craftingOptions[craftingOptions.length - 1] = new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer);
			ui.showMenu(pointer, craftingOptions);

	}

	private void onClickMine(MenuItem item) {
		selectedvill.setPath(null);
		selectedvill.addJob((Ore) item.getEntity());
		deselect(selectedvill);
		ui.deSelectIcons();
		ui.getMenu().hide();
	}

	private void onClickDrop() {
		selectedvill.setPath(null);
		selectedvill.addJob(new MoveItemJob((int) ui.getMenuIngameX()/Tile.SIZE, (int) ui.getMenuIngameY() / Tile.SIZE, currentLayerNumber, selectedvill));
		ui.deSelectIcons();
		deselect(selectedvill);
		ui.getMenu().hide();
	}

	private void onClickCancel() {
		ui.getMenu().hide();
		ui.deSelectIcons();
		if (selectedvill != null) {
			deselect(selectedvill);
		}
	}

	private void onClickFight(MenuItem item) {
		selectedvill.setPath(null);
		selectedvill.addJob(new FightJob(selectedvill, (Mob) item.getEntity()));
		deselect(selectedvill);
		ui.deSelectIcons();
		ui.getMenu().hide();
	}

	private void scroll(long window, double v, double v1) {
		if ((currentLayerNumber <= map.length - 1 && v1 > 0) || (currentLayerNumber >= 0 && v1 < 0)) {
			currentLayerNumber -= v1;
			if (currentLayerNumber < 0) {
				currentLayerNumber = 0;
			} else if (currentLayerNumber > map.length - 1) {
				currentLayerNumber = map.length - 1;
			}
			ui.updateMinimap(map, currentLayerNumber);
		}
	}

	private Villager getIdlestVil() {
		Villager lowest = vills.get(0);
		for (Villager i : vills) {
			if (i.getJobSize() < lowest.getJobSize()) {
				lowest = i;
			}
		}
		return lowest;
	}

	private Optional<Villager> anyVillHoverOn() {
		return vills.stream().filter(villager -> villager.hoverOn(pointer, currentLayerNumber)).findAny();
	}

	private Optional<Mob> anyMobHoverOn() {
		return mobs.stream().filter(mob -> mob.hoverOn(pointer, currentLayerNumber)).findAny();
	}

	private void deselectAllVills() {
		vills.forEach(this::deselect);
	}

	private void deselect(Villager vill) {
		vill.setSelected(false);
		selectedvill = null;
	}

	private void updateMouse() {
		if ((ui.getIcons().isAxeSelected()) && ui.getIcons().hoverOnNoIcons()) {
			if (pointer.wasPressed(GLFW_MOUSE_BUTTON_LEFT)) {
				ui.showSelectionSquare(pointer);
				return;
			}
			if (pointer.wasReleased(GLFW_MOUSE_BUTTON_LEFT)) {
				int x = (int) (ui.getSelectionX() / Tile.SIZE);
				int y = (int) (ui.getSelectionY() / Tile.SIZE);
				int width = (int) (ui.getSelectionWidth() / Tile.SIZE);
				int height = (int) (ui.getSelectionHeight() / Tile.SIZE);
				for (int xs = x; xs < (x + width); xs ++) {
					for (int ys = y; ys < (y + height); ys++) {
						map[currentLayerNumber].selectTree(xs, ys, false).ifPresent(tree -> getIdlestVil().addJob(tree));
					}
				}
				ui.resetSelection();
				ui.deSelectIcons();
			}
			return;
		}
		if (pointer.wasPressed(GLFW_MOUSE_BUTTON_LEFT)) {
			if (ui.getIcons().isMiningSelected() && ui.getIcons().hoverOnNoIcons()) {
				map[currentLayerNumber].selectOre(pointer.getTileX(), pointer.getTileY()).ifPresent(ore -> {
					Villager idle = getIdlestVil();
					deselectAllVills();
					idle.addJob(ore);
					ui.deSelectIcons();
				});
			}
			if (((ui.getIcons().isSwordsSelected()) && ui.getIcons().hoverOnNoIcons())) {
				Villager idle = getIdlestVil();
				deselectAllVills();
				anyMobHoverOn().ifPresent(mob -> idle.addJob(new FightJob(idle, mob)));
				ui.deSelectIcons();
				return;

			}
			if (!ui.outlineIsVisible()) {
				anyVillHoverOn().ifPresent(villager -> {
					deselectAllVills();
					selectedvill = villager;
					selectedvill.setSelected(true);
					ui.deSelectIcons();
				});
			}
			if (ui.outlineIsVisible() && !ui.menuVisible() && ui.getIcons().hoverOnNoIcons()) {
				float[][] coords = ui.getOutlineCoords();
				for (float[] blok : coords) {
					if (map[currentLayerNumber].tileIsEmpty((int) blok[0] / Tile.SIZE, (int) blok[1] / Tile.SIZE)) {
						Villager idle = getIdlestVil();
						idle.addBuildJob((int) blok[0] / Tile.SIZE, (int) blok[1] / Tile.SIZE, currentLayerNumber, ui.getBuildRecipeOutline().getProduct(),
							ui.getBuildRecipeOutline().getResources()[0]);
					}
				}
				ui.removeBuildSquare();
				deselectAllVills();
				ui.deSelectIcons();
			}
		} else if (pointer.wasPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
			if (selectedvill != null) {
				List<MenuItem> options = new ArrayList<>();
				if (selectedvill.getHolding() != null&&(map[currentLayerNumber].tileIsEmpty(pointer.getTileX(), pointer.getTileY()) || map[currentLayerNumber].getEntityOn(pointer.getTileX(), pointer.getTileY()) instanceof Chest)) {
					options.add(new MenuItem((MenuItem.DROP + " " + selectedvill.getHolding().getName()), in -> onClickDrop(), pointer));
				}

				map[currentLayerNumber].selectTree(pointer.getX(), pointer.getY()).ifPresent(
					tree -> options.add(new MenuItem((MenuItem.CHOP), tree, this::onClickChop, pointer))
				);

				map[currentLayerNumber].selectOre(pointer.getTileX(), pointer.getTileY()).ifPresent(
					ore ->  options.add(new MenuItem((MenuItem.MINE), ore, this::onClickMine, pointer))
				);

				anyMobHoverOn().ifPresent(mob -> options.add(new MenuItem(MenuItem.FIGHT, mob, this::onClickFight, pointer)));

				map[currentLayerNumber].getItemOn(pointer.getX(), pointer.getY()).ifPresent(item -> {
					if (item instanceof Weapon) {
						options.add(new MenuItem((MenuItem.EQUIP), item, this::onClickPickup, pointer));
					} else if (item instanceof Clothing) {
						options.add(new MenuItem((MenuItem.WEAR), item, this::onClickPickup, pointer));
					} else {
						options.add(new MenuItem((MenuItem.PICKUP), item, this::onClickPickup, pointer));
					}
				});

				if (map[currentLayerNumber].getEntityOn(pointer.getTileX(), pointer.getTileY()) instanceof Container) {
					Container container = map[currentLayerNumber].getEntityOn(pointer.getTileX(), pointer.getTileY());
					for (Item i : container.getItems()) {
						options.add(new MenuItem((MenuItem.PICKUP), i, this::onClickPickup, pointer));
					}
				} else {
					options.add(new MenuItem(MenuItem.MOVE, in -> onClickMove(), pointer));
				}
				options.add(new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer));
				ui.showMenu(pointer, options.toArray(new MenuItem[0]));
			} else {
				if (map[currentLayerNumber].getEntityOn(pointer.getTileX(), pointer.getTileY()) instanceof Furnace) {
					ui.showMenu(
						pointer,
						new MenuItem(MenuItem.SMELT, in -> onClickSmelt(), pointer),
						new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer)
					);
				} else if (map[currentLayerNumber].getEntityOn(pointer.getTileX(), pointer.getTileY()) instanceof Anvil) {
					ui.showMenu(
						pointer,
						new MenuItem(MenuItem.SMITH, in -> onClickSmith(), pointer),
						new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer)
					);
				} else {
					ui.showMenu(pointer, new MenuItem(MenuItem.CANCEL, in -> onClickCancel(), pointer));
				}
			}
		}
	}

	private void moveCamera(int xScroll, int yScroll) {
		this.xScroll += xScroll;
		this.yScroll += yScroll;
		ui.setOffset(this.xScroll, this.yScroll);
	}

	private void moveCamera() {
		int _yScroll = 0;
		int _xScroll = 0;
		if (Keyboard.isKeyDown(GLFW_KEY_UP) && yScroll > 1) {
			_yScroll -= 6;
		}
		if (Keyboard.isKeyDown(GLFW_KEY_DOWN) && yScroll < ((map[currentLayerNumber].height * Tile.SIZE) - 1 - height)) {
			_yScroll += 6;
		}
		if (Keyboard.isKeyDown(GLFW_KEY_LEFT) && xScroll > 1) {
			_xScroll -= 6;
		}
		if (Keyboard.isKeyDown(GLFW_KEY_RIGHT) && xScroll < ((map[currentLayerNumber].width * Tile.SIZE) - width - 1)) {
			_xScroll += 6;
		}
		moveCamera(_xScroll, _yScroll);
	}

	private <T extends Mob> void update(T mob, Iterator<T> iterator) {
		mob.update();
		if (mob.getHealth() == 0) {
			mob.die();
			iterator.remove();
		}
	}

	private void updateMobs() {
		Iterator<Mob> iMob = mobs.iterator();
		while (iMob.hasNext()) {
			update(iMob.next(), iMob);
		}
		Iterator<Villager> iVill = vills.iterator();
		while (iVill.hasNext()) {
			update(iVill.next(), iVill);
		}
		Iterator<Villager> iSoll = sols.iterator();
		while (iSoll.hasNext()) {
			update(iSoll.next(), iSoll);
		}
	}

	private void renderMobs() {
		float x1 = (xScroll + width + Sprite.SIZE);
		float y1 = (yScroll + height + Sprite.SIZE);

		mobs.forEach(mob -> mob.renderIf(
			inBounds(mob.getX(), mob.getY(), mob.getZ(), currentLayerNumber, xScroll, x1, yScroll , y1),
			OpenGLUtils.mobData
		));

		vills.forEach(vil -> vil.renderIf(
			inBounds(vil.getX(), vil.getY(), vil.getZ(), currentLayerNumber, xScroll, x1, yScroll , y1),
				OpenGLUtils.mobData
		));

		sols.forEach(sol -> sol.renderIf(
			inBounds(sol.getX(), sol.getY(), sol.getZ(), currentLayerNumber, xScroll, x1, yScroll , y1),
				OpenGLUtils.mobData
		));

	}

	private boolean inBounds(float x, float y, int z, int layer, float xScroll, float x1, float yScroll, float y1) {
		return z == layer && x + Tile.SIZE >= xScroll && x - Tile.SIZE <= x1 && y + Tile.SIZE >= yScroll && y - Tile.SIZE <= y1;
	}

}
