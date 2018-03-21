package entity.dynamic.mob;

import entity.dynamic.item.Item;
import map.Level;

public abstract class Humanoid extends Mob {

    Humanoid(Level[] levels, int x, int y, int z) {
        super(levels);
        while (!levels[z].isWalkAbleTile(x / 16, y / 16)) {
            x += 16;
            y += 16;
        }
        setLocation(x, y, z);
    }

    private Item holding; // item the mob is holding in his hands

    public <T extends Item> boolean isHolding(T item) {
        return holding != null && holding.equals(item);
    }

    public <T extends Item> void setHolding(T item) {
        holding = item;
        if (holding != null) {
            holding.setLocation(this.x, this.y, this.z);
        }
    }

    public Item getHolding() {
        return holding;
    }

    @Override
    public void die() {
        if (holding != null) {
            levels[z].addItem(holding);
        }

    }

    // DO NOT TOUCH THIS. SET THE MOVEMENT TO THE PATH OBJ USE move()!! DO NOT USE!!!
    @Override
    protected final void moveTo(int x, int y) {
        super.moveTo(x, y);
        if (!(holding == null)) {
            holding.setLocation(x, y, z);
        }

    }

}