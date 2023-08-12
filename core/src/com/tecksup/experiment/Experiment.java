package com.tecksup.experiment;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class Experiment extends ApplicationAdapter {
	SpriteBatch batch;
	BitmapFont font;
	Texture img;

	World world;

	//Pixel/Tile scale
	int scale = 16;

	OrthographicCamera camera;
	OrthographicCamera guiCamera;
	
	int focusPosX = 1;
	int focusPosY = 1;

	//Debug Metrics
	int totalChunksOnScreen = 0;
	int pixelsCulled = 0;

	@Override
	public void create() {
		Gdx.graphics.setVSync(false);

		//Resize gets ran on startup, this sets up cameras
		//This is fine as long as we don't reference them during the create method

		batch = new SpriteBatch();

		font = new BitmapFont(Gdx.files.internal("Fonts/PixPrompt.fnt"), new TextureRegion(new Texture(Gdx.files.internal("Fonts/PixPrompt.png"))));
		font.getData().markupEnabled = true;
		img = new Texture("white-pixel.png");

		//Generate the noise layers
		world = new World();
	}

	@Override
	public void render() {
		ScreenUtils.clear(0.1f, 0.2f, 0.18f, 1);
		updateCamera();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		//We need to add chunk culling, delete chunks that are not visible to camera
		if (true) {
			//We need to start x,y from the bottom left of the screen
			Chunk bottomLeft = getChunkAtPos(focusPosX - MathUtils.floor(camera.viewportWidth)/2, focusPosY - MathUtils.floor(camera.viewportHeight)/2);
			Chunk topRight = getChunkAtPos(focusPosX + MathUtils.floor(camera.viewportWidth)/2, focusPosY + MathUtils.floor(camera.viewportHeight)/2);

			totalChunksOnScreen = 0;
			for (int chunkX = bottomLeft.getChunkIDx(); chunkX <= topRight.getChunkIDx(); chunkX++) {
				for (int chunkY = bottomLeft.getChunkIDy(); chunkY <= topRight.getChunkIDy(); chunkY++) {
					totalChunksOnScreen++;

					drawChunk(world.getChunkFromID(chunkX, chunkY));
				}
			}
		} else { //Renders only the chunk the focus point is in
			totalChunksOnScreen = 1;

			drawChunk(getChunkAtPos(focusPosX, focusPosY));
		}

		batch.setColor(Color.BLACK);
		batch.draw(img, focusPosX, focusPosY, 8, 8);

		drawDebug();

		batch.end();
		pixelsCulled = 0;

		handleInput();
	}

	private void drawDebug() {
		batch.setProjectionMatrix(guiCamera.combined);
		font.setColor(Color.BLACK);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());
		font.draw(batch, "SEED: " + world.getSeed(), 0, Gdx.graphics.getHeight()-16);
		font.draw(batch, "Pixels culled: " + pixelsCulled, 0, Gdx.graphics.getHeight()-32);
		font.draw(batch, "Chunks visible: " + totalChunksOnScreen, 0, Gdx.graphics.getHeight()-48);
		font.draw(batch, "Total Chunks: " + world.getChunkCount(), 0, Gdx.graphics.getHeight()-64);

		Chunk temp = getChunkAtPos(focusPosX, focusPosY);
		font.draw(batch, "Chunk (" + temp.getChunkIDx() + "," + temp.getChunkIDy() + ")", 0, Gdx.graphics.getHeight()-80);

		//Code block to display pixel at cursor position
		Vector3 cursorPosInWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(cursorPosInWorld);
		Vector3 cursorPosOnScreen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		guiCamera.unproject(cursorPosOnScreen);
		font.draw(batch, "(" + ((int) cursorPosInWorld.x / scale) + "," + ((int) cursorPosInWorld.y / scale) + ")", cursorPosOnScreen.x + 16, cursorPosOnScreen.y+16);

		font.setColor(Color.WHITE);
	}

	private void drawChunk(Chunk chunk) {
		for (int x = 0; x < world.getChunkSize(); x++) {
			for (int y = 0; y < world.getChunkSize(); y++) {
				drawPixel(chunk.getChunkIDx()*world.getChunkSize() + x, chunk.getChunkIDy()*world.getChunkSize() + y, chunk.getColorMap()[x % world.getChunkSize()][y % world.getChunkSize()]);
			}
		}
	}

	private void drawPixel(int x, int y, Color color) {
		//Cull pixels not in view
		if (camera.frustum.boundsInFrustum(x*scale,y*scale,0,scale,scale,0)) {
			batch.setColor(color);

			//Draw Pixel
			batch.draw(img, x*scale, y*scale, scale, scale);
		} else {
			pixelsCulled++;
		}
	}

	public void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
			focusPosY++;
		if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
			focusPosY--;
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
			focusPosX--;
		if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			focusPosX++;
	}

	public Chunk getChunkAtPos(int x, int y) {
		int chunkX = getChunkID(x, y)[0];
		int chunkY = getChunkID(x, y)[1];

		return world.getChunkFromID(chunkX, chunkY);
	}

	public int[] getChunkID(int x, int y) {
		return new int[] {((x / scale)/world.getChunkSize() - (x <= 0? 1: 0)), ((y / scale)/world.getChunkSize() - (y <= 0? 1: 0))};
	}

	public void updateCamera() {
		camera.position.set(focusPosX, focusPosY, 0);
		camera.update();
	}

	@Override
	public void resize(int width, int height) {
		System.out.println("Resized to " + width + " " + height);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		guiCamera = new OrthographicCamera();
		guiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		updateCamera();
	}

	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
		font.dispose();
	}
}
