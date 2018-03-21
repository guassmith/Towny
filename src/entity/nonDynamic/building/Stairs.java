package entity.nonDynamic.building;

import entity.dynamic.mob.Villager;
import graphics.SpriteHashtable;
import map.Level;

public class Stairs extends BuildAbleObject {
    private boolean top;

    public Stairs(boolean top) {
        super();
        this.top = top;
        if (top) {
            sprites.add(SpriteHashtable.get(40));
        } else {
            sprites.add(SpriteHashtable.get(41));
        }
        setName("stairs");

    }

    public void initialise(int x, int y, Level[] levels, int depth) {
        initialise(x, y, levels, depth, true);
    }

    public void initialise(int x, int y, Level[] levels, int depth, boolean firstTime) {
        super.initialise(x, y, levels, depth);
        if (firstTime) {
            if (top && depth != levels.length - 1) {
                levels[depth + 1].removeEntity(x, y);
                Stairs otherPart = new Stairs(false);
                otherPart.initialise(x, y, levels, depth, false);
                levels[depth + 1].addEntity(otherPart, false);
            } else if (depth != 0) {
                levels[depth - 1].removeEntity(x, y);
                Stairs otherPart = new Stairs(true);
                otherPart.initialise(x, y, levels, depth, false);
                levels[depth - 1].addEntity(otherPart, false);
            }
        }
        setOpened(true);
    }

    public void goOnStairs(Villager villager) {
        if (top && z > 1) {
            villager.setLocation(x, y, z - 1);
        } else if (z < villager.levels.length - 1) {
            villager.setLocation(x, y, z + 1);
        }
    }

    @Override
    public BuildAbleObject clone() {
        return new Stairs(this.top);
    }

    public boolean isTop() {
        return top;
    }
}