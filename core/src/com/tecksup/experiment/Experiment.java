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
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;

public class Experiment extends ApplicationAdapter {
	SpriteBatch batch;
	BitmapFont font;
	Texture img;

	SimplexNoise landNoise;
	SimplexNoise secondNoise;

	int scale = 16;

	int chunkSize = 64;
	int chunkCount = 0;
	HashMap<Integer, HashMap<Integer, Chunk>> chunks = new HashMap<>();

	OrthographicCamera camera;
	OrthographicCamera guiCamera;

	@Override
	public void create () {
		Gdx.graphics.setVSync(false);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		guiCamera = new OrthographicCamera();
		guiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("Fonts/PixPrompt.fnt"), new TextureRegion(new Texture(Gdx.files.internal("Fonts/PixPrompt.png"))));
		font.getData().markupEnabled = true;
		img = new Texture("white-pixel.png");

		landNoise = new SimplexNoise(128, .58f, 2352345);
		secondNoise = new SimplexNoise(64, .5f, 2345);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		if (false) {
			//We need to start x,y from the bottom left of the screen
			for (int x = 0; x < camera.viewportWidth/scale; x++) {
				for (int y = 0; y < camera.viewportHeight/scale; y++) {
					//0,0 is the pixel on bottom left of screen, we need to map it to world pos
					Vector3 screenPos = new Vector3(x,y,0);
					camera.unproject(screenPos);
					int worldX = MathUtils.floor(screenPos.x);
					int worldY = MathUtils.floor(screenPos.y);

					//With this, we just need to make sure there are no negatives
					if (worldX < 0 || worldY < 0)
						break;

					Chunk temp = getLandValueFromChunks(worldX, worldY);
					int chunkMapX = worldX % chunkSize;
					int chunkMapY = worldY % chunkSize;

					float landVal = temp.landMap[chunkMapX][chunkMapY];
					float secondVal = temp.secondMap[chunkMapX][chunkMapY];

					drawPixel(x, y, landVal, secondVal);
				}
			}
		} else {
			//We need to add chunk culling, delete chunks that are not visible to camera
			for (int x = 0; x < 4*chunkSize; x++) {
				for (int y = 0; y < 4*chunkSize; y++) {
					Chunk temp = getLandValueFromChunks(x, y);

					float landVal = temp.landMap[x % chunkSize][y % chunkSize];
					float secondVal = temp.secondMap[x % chunkSize][y % chunkSize];

					drawPixel(x, y, landVal, secondVal);
				}
			}
		}

		batch.setProjectionMatrix(guiCamera.combined);
		font.setColor(Color.BLACK);
		font.draw(batch, Gdx.graphics.getFramesPerSecond() + "", 0, Gdx.graphics.getHeight());
		font.draw(batch, "Pixels culled " + pixelsCulled, 0, Gdx.graphics.getHeight()-16);
		font.draw(batch, "Total Chunks " + chunkCount, 0, Gdx.graphics.getHeight()-32);

		Vector3 camCornerPos = new Vector3(camera.position.x - (camera.viewportWidth/2), camera.position.y - (camera.viewportHeight/2), 0);
		System.out.println("Camera Pos " + camCornerPos);
		camera.unproject(camCornerPos);
		System.out.println("Camera Pos unprojected " + camCornerPos);
		System.out.println("---");
		font.draw(batch, "(" + ((int) camCornerPos.x / scale) + "," + ((int) camCornerPos.y / scale) + ")", 0, Gdx.graphics.getHeight()-48);


		Vector3 cursorPosInWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(cursorPosInWorld);
		Vector3 cursorPosOnScreen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		guiCamera.unproject(cursorPosOnScreen);
		font.draw(batch, "(" + ((int) cursorPosInWorld.x / scale) + "," + ((int) cursorPosInWorld.y / scale) + ")", cursorPosOnScreen.x + 16, cursorPosOnScreen.y+16);

		font.setColor(Color.WHITE);
		batch.end();
		pixelsCulled = 0;

		handleInput();
	}

	public Chunk getLandValueFromChunks(int x, int y) {
		int chunkX = x / chunkSize;
		int chunkY = y / chunkSize;

		if (!chunks.containsKey(chunkX)) {
			chunks.put(chunkX, new HashMap<Integer, Chunk>());
		}
		if (!chunks.get(chunkX).containsKey(chunkY)) {
			chunks.get(chunkX).put(chunkY, new Chunk(chunkSize, chunkX, chunkY, landNoise, secondNoise));
			chunkCount++;
		}

		return chunks.get(chunkX).get(chunkY);
	}

	int pixelsCulled = 0;
	private void drawPixel(int x, int y, float landVal, float secondVal) {
		//Cull pixels not in view
		if (camera.frustum.boundsInFrustum(x*scale,y*scale,0,scale,scale,0)) {
			if (landVal > 0.5) { //Water
				batch.setColor(landVal * (landVal * -1), landVal * (landVal * -1), landVal, 1);
			} else { //Land
				if (landVal > 0.46) //Sand
					batch.setColor(1f, 1f, landVal, 1);
				else {
					if (secondVal > 0.5f) {
						batch.setColor(landVal * secondVal, landVal / secondVal, landVal * secondVal, 1);
					} else {
						batch.setColor(landVal, landVal / secondVal, landVal, 1);
					}
				}
			}

			//Draw Pixel
			batch.draw(img, x*scale, y*scale, scale, scale);
		} else {
			pixelsCulled++;
		}
	}

	public void handleInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.W))
			camera.position.y++;
		if (Gdx.input.isKeyPressed(Input.Keys.S))
			camera.position.y--;
		if (Gdx.input.isKeyPressed(Input.Keys.A))
			camera.position.x--;
		if (Gdx.input.isKeyPressed(Input.Keys.D))
			camera.position.x++;
	}

	Vector3 camPosCache = new Vector3();
	@Override
	public void resize(int width, int height) {
		System.out.println("Resized to " + width + " " + height);
		camPosCache.set(camera.position);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(camPosCache);
		guiCamera = new OrthographicCamera();
		guiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
