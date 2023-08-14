package com.tecksup.experiment;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.tecksup.experiment.data.RenderUtils;
import com.tecksup.experiment.map.Chunk;
import com.tecksup.experiment.map.World;

public class Experiment extends ApplicationAdapter {

	private World world;

	private SpriteBatch batch;
	private ShapeRenderer debugRenderer;

	private OrthographicCamera camera;
	private OrthographicCamera guiCamera;
	private int focusPosX = 0;
	private int focusPosY = 0;

	public static RenderUtils renderUtils;

	public static boolean drawPixel = true;

	@Override
	public void create() {
		Gdx.graphics.setVSync(false);

		//Resize gets ran on startup, this sets up cameras
		//This is fine as long as we don't reference them during this method

		batch = new SpriteBatch();
		debugRenderer = new ShapeRenderer();

		//Load Render Utils
		renderUtils = new RenderUtils(() -> {
			//Generate the world after loading is complete
			world = new World();
		});
	}

	@Override
	public void render() {
		if (!renderUtils.doneLoading()) {
			return; //We don't render anything unless our assetManager has finished loading textures
		}

		ScreenUtils.clear(0.1f, 0.2f, 0.18f, 1);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		//The world is responsible for culling its own rendering, so we pass in the camera and focus point
		world.draw(batch, camera, focusPosX, focusPosY, Gdx.graphics.getDeltaTime());

		//Draws focus point
		batch.draw(renderUtils.getTextures("pixel").get(0), focusPosX-4, focusPosY-4, 8, 8);

		//Just draws the debug data
		drawDebugText();

		batch.end();

		//Draws the chunk boundaries, this is not done with spritebatch, so we draw it separately
		world.drawDebug(debugRenderer, camera);

		handleInput();
		updateCamera();
	}

	public void drawDebugText() {
		batch.setProjectionMatrix(guiCamera.combined);
		renderUtils.getFont().setColor(Color.BLACK);
		renderUtils.getFont().draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());
		renderUtils.getFont().draw(batch, "SEED: " + world.getLandSeed(), 0, Gdx.graphics.getHeight()-16);
		renderUtils.getFont().draw(batch, "Pixels culled: " + world.getPixelsCulled(), 0, Gdx.graphics.getHeight()-32);
		renderUtils.getFont().draw(batch, "Chunks visible: " + world.getTotalChunksOnScreen(), 0, Gdx.graphics.getHeight()-48);
		renderUtils.getFont().draw(batch, "Total Chunks: " + world.getChunkCount(), 0, Gdx.graphics.getHeight()-64);

		Chunk temp = world.getChunkAtPos(focusPosX, focusPosY);
		renderUtils.getFont().draw(batch, "Chunk (" + temp.getChunkIDx() + "," + temp.getChunkIDy() + ")", 0, Gdx.graphics.getHeight()-80);

		//Code block to display pixel at cursor position
		Vector3 cursorPosInWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(cursorPosInWorld);
		Vector3 cursorPosOnScreen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		guiCamera.unproject(cursorPosOnScreen);
		renderUtils.getFont().draw(batch, "(" + ((int) cursorPosInWorld.x / world.getScale()) + "," + ((int) cursorPosInWorld.y /  world.getScale()) + ")", cursorPosOnScreen.x + 16, cursorPosOnScreen.y+16);

		renderUtils.getFont().setColor(Color.WHITE);
	}

	private void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
			focusPosY++;
		if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
			focusPosY--;
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
			focusPosX--;
		if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			focusPosX++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.R))
			world = new World();
	}

	private void updateCamera() {
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
		debugRenderer.dispose();
		renderUtils.dispose();
	}
}
