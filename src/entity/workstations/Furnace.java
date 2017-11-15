package entity.workstations;

import graphics.Screen;
import graphics.Sprite;

public class Furnace extends Workstation {
	public Furnace() {
		super();
		resourceName = "stones";
	}



	public void render(Screen s) {
		if (!isRunning()) {
			s.renderSprite(x, y, Sprite.furnaceOff);
		} else {
			if (animationcounter>29) {
				s.renderSprite(x, y, Sprite.furnaceOn1);
				anmiationCounterTick();
			} else {
				s.renderSprite(x, y, Sprite.furnaceOn2);
				anmiationCounterTick();
			}
		} 
	}

}
