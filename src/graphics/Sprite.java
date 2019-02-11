package graphics;

import map.Tile;

//sprites in the game
public class Sprite {
    private int id;
    public static final int SIZE = Tile.SIZE; // 48
    public int[] pixels;

    protected Sprite(int x, int y, Spritesheet sheet) {
        pixels = load(x * SIZE + (x * sheet.getMargin()),
                y * SIZE + (y * sheet.getMargin()), SIZE, sheet);
        id = OpenglUtils.loadTexture(pixels, SIZE,SIZE);
    }

    public Sprite(int[] pixels) {
        this.pixels = pixels;
        id = OpenglUtils.loadTexture(this.pixels, SIZE, SIZE);
    }
    public Sprite() {
        id = 0;
    }

    // load a sprites pixels into the pixel array
    private int[] load(int xa, int ya, int size, Spritesheet sheet) {
        int[] pixels = new int[48 * 48];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixels[x + y * size] = sheet.getPixels()[(x + xa) + (y + ya) * sheet.getWidth()];
            }
        }
        return pixels;
    }



    public void draw(int x, int y) {
        OpenglUtils.drawTexturedQuadScaled(id,x,y,SIZE);
    }


}
