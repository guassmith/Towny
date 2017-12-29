package entity;

import entity.item.Item;
import map.Level;
import sound.Sound;

public abstract class BuildAbleObject extends Entity {
	private boolean open;
	public boolean initialised = false; // has the building been initialised
	protected byte condition = 0; // condition of the building (0=not built ,
									// 100=
	// done)
	protected String resourceName;

	public BuildAbleObject() {
		setVisible(false);
	}

	public void setOpened(boolean open) {
		this.open = open;
		if (open) {
			level.removeHardEntity(this);
			level.walkableEntities.add(this);
		} else {
			level.addHardEntity(this);
			level.walkableEntities.remove(this);
		}
	}

	public boolean isOpen() {
		return open;
	}

	public boolean initialise(int x, int y, Item material, Level level) {
		if (material == null) {
			return false;
		}
		this.level = level;
		setLocation(x * 16, y * 16);
		if (isOpen()) {
			level.walkableEntities.add(this);
		} else {
			level.addHardEntity(this);
		}

		initialised = true;
		return true;

	}

	// build method called by villagers when building
	public boolean build() {
		if (initialised) {
			if (condition < 100) {
				if (condition == 1) {
					Sound.speelGeluid(Sound.drill);
				}
				condition++;
				return false;
			} else {
				this.setVisible(true);
				level.getTile(x * 16, y * 16).setSolid(true);
				return true;
			}

		}
		return false;
	}

	public String getResourceName() {
		return resourceName;
	}

}
