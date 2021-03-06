package graphics.ui;

import graphics.opengl.OpenGLUtils;
import graphics.ui.icon.Icon;
import input.PointerInput;
import util.TextureInfo;
import util.vectors.Vec2f;
import util.vectors.Vec4f;

public class TopBar {

	private final Vec2f position;
	private final Vec2f size;
	private int vilcount, solcount; // amount of villagers and soldiers
	private final Icon pause;
	private final Icon fast;
	private final Icon slow;
	private final Icon sol;
	private final Icon vil;
	private final TextureInfo pauseTexture;
	private final TextureInfo playTexture;
	private static final Vec4f colour = new Vec4f(0.3568f, 0.3686f, 0.3882f, 0.43137f); //colour for background

	// constructor
	TopBar(float x, float y, float width, float height, PointerInput pointer) {
		position = new Vec2f(x, y);
		size = new Vec2f(width, height);
		pauseTexture = OpenGLUtils.loadTexture("/icons/pause-button.png");
		playTexture = OpenGLUtils.loadTexture("/icons/play-button.png");
		pause = new Icon(x + 120, y + 25, pauseTexture, 0.060f, pointer);
		fast = new Icon(x + 160, y + 30, "/icons/fast.png", 1.0f, pointer);
		slow = new Icon(x + 75, y + 30, "/icons/slow.png", 1.0f, pointer);
		sol = new Icon(x + 210, y + 17, "/icons/soldier.png", 1.0f, pointer);
		vil = new Icon(x + 10, y + 17, "/icons/villager.png", 1.0f, pointer);
	}

	// update the villager and soldier counts
	void updateCounts(int solcount, int vilcount) {
		this.solcount = solcount;
		this.vilcount = vilcount;
	}

	void init(PointerInput pointer, Runnable togglePause, Runnable upSpeed, Runnable downSpeed) {
		this.pause.setOnClick(pointer, () -> {
			pause.setTexture(pause.getTextureId() == pauseTexture.id ? playTexture : pauseTexture, 0.060f);
			togglePause.run();
		});
		this.fast.setOnClick(pointer, upSpeed);
		this.slow.setOnClick(pointer, downSpeed);
	}

	// render the topbar on the screen
	public void render(int speed) {
		OpenGLUtils.drawFilledSquare(position, size, new Vec2f(0, 0), colour);
		vil.render();
		sol.render();
		slow.render();
		fast.render();
		pause.render();
		OpenGLUtils.drawText("Villagers", position.x + 5, position.y - 5);
		OpenGLUtils.drawText(vilcount + "", position.x + 25, position.y + 65);
		OpenGLUtils.drawText("Speed: " + (speed/10-2), position.x + 95, position.y + 60);
		OpenGLUtils.drawText("Soldiers", position.x + 200, position.y - 5);
		OpenGLUtils.drawText(solcount + "", position.x + 225, position.y + 65);
	}

}
