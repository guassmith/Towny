package graphics.opengl;

import graphics.SpritesheetHashtable;
import main.Game;
import map.Tile;
import org.lwjgl.BufferUtils;
import util.TextureInfo;
import util.vectors.Vec2f;
import util.vectors.Vec4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.ARBImaging.GL_TABLE_TOO_LARGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL45.GL_CONTEXT_LOST;
import static org.lwjgl.stb.STBImage.*;

public abstract class OpenGLUtils {

	private static int VAO;
	public static Shader texShader;
	public static Shader colShader;
	public static Shader fontShader;
	public static Shader tileShader;
	private static final Vec4f outlineColour = new Vec4f(1,0,0,1);


	private static final int maxInstances = 627; //maximum amount of instances that can be drawn in one frame (per buffer)

	public static InstanceData tileData;
	public static InstanceData entityData;
	public static InstanceData itemData;
	public static InstanceData hardEntityData;
	public static InstanceData mobData;

	private static ArrayList<Outline> outlines = new ArrayList<>();

	public static void init() throws Exception {
		texShader = new Shader(Game.class.getResource("/shaders/tex_shader.vert"), Game.class.getResource("/shaders/tex_shader.frag"));
		colShader = new Shader(Game.class.getResource("/shaders/col_shader.vert"), Game.class.getResource("/shaders/col_shader.frag"));
		fontShader = new Shader(Game.class.getResource("/shaders/text_shader.vert"), Game.class.getResource("/shaders/tex_shader.frag"));
		tileShader = new Shader(Game.class.getResource("/shaders/tile_shader.vert"), Game.class.getResource("/shaders/tex_shader.frag"));

		float[] vertices = {
				// Left bottom triangle
				pToGL((float)Game.width/2, 'w'), pToGL((float)Game.height/2, 'h'),
				pToGL((float)Game.width/2, 'w'), pToGL((float)Game.height/2 + Tile.SIZE, 'h'),
				pToGL((float)Game.width/2 + Tile.SIZE, 'w'), pToGL((float)Game.height/2 + Tile.SIZE, 'h'),
				// Right top triangle
				pToGL((float)Game.width/2 + Tile.SIZE, 'w'), pToGL((float)Game.height/2 + Tile.SIZE, 'h'),
				pToGL((float)Game.width/2 + Tile.SIZE, 'w'), pToGL((float)Game.height/2, 'h'),
				pToGL((float)Game.width/2, 'w'), pToGL((float)Game.height/2, 'h')
				/*0f, 0f,
				0f, pToGL(1, 'h'),
				pToGL(1, 'w'), pToGL(1, 'h'),
				pToGL(1, 'w'), pToGL(1, 'h'),
				pToGL(1, 'w'), 0f,
				0f, 0f*/
		};

		float[] texCoords = {
				0f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 1f,
				1f, 0f,
				0f, 0f
		};


		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);

		glEnable(GL_BLEND);

		int VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glEnableVertexAttribArray(0);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

		int VBO2 = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO2);
		glEnableVertexAttribArray(1);
		glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		tileData = new InstanceData(maxInstances);
		tileData.setSpritesheet(SpritesheetHashtable.get(1));
		entityData = new InstanceData(maxInstances);
		entityData.setSpritesheet(SpritesheetHashtable.get(1));
		itemData = new InstanceData(maxInstances);
		itemData.setSpritesheet(SpritesheetHashtable.get(1));
		hardEntityData = new InstanceData(maxInstances*10);
		hardEntityData.setSpritesheet(SpritesheetHashtable.get(1));
		mobData = new InstanceData(maxInstances);
		mobData.setSpritesheet(SpritesheetHashtable.get(2));

		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glVertexAttribDivisor(2,1); //this sends the vertex attrib to the shader per instance instead of per vertex
		glVertexAttribDivisor(3,1); //this sends the vertex attrib to the shader per instance instead of per vertex

		glLineWidth(1.f); //mac's don't support more than 1
		glEnable(GL_LINE_SMOOTH);
	}

	public static void clearAllInstanceData() {
		tileData.clearInstances();
		entityData.clearInstances();
		itemData.clearInstances();
		hardEntityData.clearInstances();
		mobData.clearInstances();
	}

	public static float pToGL(float pixel, char o) { //converts between pixels and openGL coordinates
		float orientation = o == 'w' ? Game.width : Game.height;
		if(o != 'w') { pixel = Game.height - pixel; }
		return (2f * pixel + 1f) / orientation - 1f;
	}

	public static int loadTexture(int[] pixels, int width, int height) {
		ByteBuffer buffer = getByteBuffer(pixels,width,height);
		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
		//Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		//Setup texture scaling filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		//Send texel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		//Return the texture ID so we can bind it again later
		return textureID;
	}

	public static TextureInfo loadTexture(String filename) {
		int[] imageWidth = new int[1];
		int[] imageHeight = new int[1];
		int[] channels = new int[1];
		filename = System.getProperty("user.dir")+"/src/main/resources"+filename;
		ByteBuffer buffer = stbi_load(filename, imageWidth, imageHeight, channels, 4);

		//System.out.println(stbi_failure_reason());

		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, imageWidth[0], imageHeight[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return new TextureInfo(textureID, imageWidth[0], imageHeight[0], channels[0], buffer);
	}

	public static void deleteTexture(int textId) {
		glDeleteTextures(textId);
	}

	public static ByteBuffer getByteBuffer(int[] pixels, int width, int height) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component.
				buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
				buffer.put((byte) (pixel & 0xFF));             // Blue component
			}
		}
		buffer.flip();
		return buffer;
	}

	public static void drawTexturedQuad(Vec2f pos, Vec2f size, Vec2f offset, Vec2f texPos, Vec2f texSize, int texture) {
		glBindTexture(GL_TEXTURE_2D, texture);

		fontShader.setUniform("offset", pToGL(pos.x - offset.x, 'w'), pToGL(pos.y - offset.y, 'h'));
		fontShader.setUniform("scale", size.x / Tile.SIZE, size.y / Tile.SIZE);

		fontShader.setUniform("tex_offset", texPos);
		fontShader.setUniform("tex_scale", texSize);

		glBindVertexArray(VAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	public static void drawInstanced(InstanceData instanceData, Vec2f tileSize, Vec2f offset) {
		tileShader.use();
		instanceData.bindTexture();

		tileShader.setUniform("scale", tileSize.x / Tile.SIZE, tileSize.y / Tile.SIZE);
		//tileShader.setUniform("offset", pToGL(-offset.x % Tile.SIZE, 'w'), pToGL(-offset.y % Tile.SIZE, 'h'));
		fontShader.setUniform("offset", (2f*-offset.x)/ Game.width, (2f*offset.y)/ Game.height);
		Vec2f texScale = new Vec2f((float)Tile.SIZE/ instanceData.getSpriteSheet().getWidth(), (float)Tile.SIZE/ instanceData.getSpriteSheet().getHeight());
		tileShader.setUniform("tex_scale", texScale);

		instanceData.unmapBuffer();

		//I don't like this
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 5*4, 0);
		glVertexAttribPointer(3, 2, GL_FLOAT, false, 5*4, 3*4);

		glDrawArraysInstanced(GL_TRIANGLES, 0, 6, instanceData.getInstances());

		//GL_MAP_UNSYNCHRONIZED may break something at some point
		int accessBits = GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_UNSYNCHRONIZED_BIT;
		instanceData.mapBuffer(accessBits);
	}

	public static void drawTexturedQuadScaled(Vec2f pos, Vec2f size, Vec2f offset, int texture) {
		//TODO Move this out of here, shader.use() is very bad for fps
		texShader.use();

		glBindTexture(GL_TEXTURE_2D, texture);

		texShader.setUniform("offset", pToGL(pos.x - offset.x, 'w'), pToGL(pos.y - offset.y, 'h'));
		texShader.setUniform("scale", size.x / Tile.SIZE, size.y / Tile.SIZE);

		glBindVertexArray(VAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	public static void drawOutline(Vec2f pos, Vec2f size, Vec2f offset, Vec4f color) {
		colShader.use();
		colShader.setUniform("offset", pToGL(pos.x - offset.x, 'w'), pToGL(pos.y - offset.y, 'h'));
		colShader.setUniform("scale", size.x / Tile.SIZE, size.y / Tile.SIZE);
		colShader.setUniform("i_color", color);
		glBindVertexArray(VAO);
		glDrawArrays(GL_LINE_LOOP, 0, 6);
	}
	public static void drawOutline(Vec2f pos, Vec2f size, Vec2f offset) {
		drawOutline(pos, size, offset, outlineColour);
	}

	public static void iconDraw(int id, Vec2f pos, Vec2f size, boolean drawSelectionSquare) { //drawTexturedQuadScaled ui which does not need to be scaled up
		drawTexturedQuadScaled(pos, size, new Vec2f(0,0), id);
		if (drawSelectionSquare) {
			drawOutline(pos, size, new Vec2f(0,0), new Vec4f(1,0,0,1));
		}
	}

	public static void menuDraw(Vec2f pos, Vec2f size) {
		drawFilledSquare(pos,size, new Vec2f(0,0), new Vec4f(0.3568f,0.3686f,0.8235f,0.5f));
	}

	public static void drawFilledSquare(Vec2f pos, Vec2f size, Vec2f offset, Vec4f color) {
		colShader.use();
		colShader.setUniform("offset", pToGL(pos.x - offset.x, 'w'), pToGL(pos.y - offset.y, 'h'));
		colShader.setUniform("scale", size.x / Tile.SIZE, size.y / Tile.SIZE);
		colShader.setUniform("i_color", color);
		glBindVertexArray(VAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	public static void menuItemDraw(Vec2f pos, String text, boolean selected) {
		if (selected) {
			drawTextRed(text, pos.x, pos.y - 5);
		} else {
			drawText(text, pos.x,pos.y - 5);
		}

	}

	public static void buildOutlineDraw(Vec2f pos, float size, Vec2f offset, Vec4f color) {
		drawFilledSquare(pos, new Vec2f(size), offset, color);
	}

	public static void drawText(String text, float x, float y) {
		TrueTypeFont.black.drawString(x,y,text,1,1);
	}

	public static void drawTextRed(String text, float x, float y) {
		TrueTypeFont.red.drawString(x,y,text,1,1);
	}

	public static void clearOutlines() {
		outlines.clear();
	}
	public static void addOutline(Outline outline) {
		outlines.add(outline);
	}
	public static void addOutline(Vec2f pos, Vec2f size, Vec4f color) {
		addOutline(new Outline(pos, size, color));
	}
	public static void addOutline(Vec2f pos, Vec2f size) {
		addOutline(pos, size, outlineColour);
	}

	public static void drawOutlines(Vec2f offset) {
		for(Outline outline : outlines) {
			drawOutline(outline.pos, outline.size, offset, outline.color);
		}
	}

	private static String errorToString(int error) {
		switch (error) {
			case GL_INVALID_ENUM: return "GL_INVALID_ENUM";
			case GL_INVALID_VALUE: return "GL_INVALID_VALUE";
			case GL_INVALID_OPERATION: return "GL_INVALID_OPERATION";
			case GL_STACK_OVERFLOW: return "GL_STACK_OVERFLOW";
			case GL_STACK_UNDERFLOW: return "GL_STACK_UNDERFLOW";
			case GL_OUT_OF_MEMORY: return "GL_OUT_OF_MEMORY";
			case GL_INVALID_FRAMEBUFFER_OPERATION: return "GL_INVALID_FRAMEBUFFER_OPERATION";
			case GL_CONTEXT_LOST: return "GL_CONTEXT_LOST";
			case GL_TABLE_TOO_LARGE: return "GL_TABLE_TOO_LARGE";
			default: return "UNKNOWN_ERROR";
		}
	}

	public static void checkGLError() {
		int glError = glGetError();
		if(glError != 0) {
			String errorName = errorToString(glError);
			System.out.println("GL ERROR: " + Integer.toHexString(glError) + " " + errorName);
		}
	}
}
