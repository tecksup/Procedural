package com.tecksup.experiment.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.google.gson.JsonObject;
import com.tecksup.experiment.data.SimplexNoise;
import com.tecksup.experiment.data.Common;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tecksup.experiment.Experiment.drawPixel;
import static com.tecksup.experiment.Experiment.renderUtils;

public class World {

    //Seeds, may remove this as it's not super needed atm
    private final int landSeed;

    //This is the elevation clamping that creates terraces in the elevation.
    protected final float elevClamp = 32f;
    protected final float waterElev = 0.5f;

    //Noise Maps for generating terrain
    private final SimplexNoise elevation;
    private final SimplexNoise secondNoise;

    //Chunk data
    private final int chunkSize = 64;
    private int chunkCount = 0;
    private final HashMap<Integer, HashMap<Integer, Chunk>> chunks = new HashMap<>();

    TkTileset tileset;

    //Pixel/Tile Size when drawn
    private final int scale = 8;

    //Debug Metrics
    private int totalChunksOnScreen = 0;
    private final ArrayList<Chunk> chunksOnScreen = new ArrayList<>();
    private int pixelsCulled = 0;

    public World() {
        this.landSeed = MathUtils.random(999999999);
        elevation = new SimplexNoise(512, .5f, landSeed);
        secondNoise = new SimplexNoise(64, .5f, landSeed/3);
        JsonObject temp = Common.jsonParser.parse(new String(Gdx.files.internal("tilesets.json").readBytes())).getAsJsonObject();
        tileset = new TkTileset(temp.get("Tilesets").getAsJsonArray().get(0).getAsJsonObject());
    }

    public void draw(SpriteBatch batch, OrthographicCamera camera, int focusPosX, int focusPosY, float time) {
        tileset.update(time);

        //We need to add chunk culling, delete chunks that are not visible to camera

        //We need to start x,y from the bottom left of the screen
        Chunk bottomLeft = getChunkAtPos(focusPosX - MathUtils.floor(camera.viewportWidth)/2, focusPosY - MathUtils.floor(camera.viewportHeight)/2);
        Chunk topRight = getChunkAtPos(focusPosX + MathUtils.floor(camera.viewportWidth)/2, focusPosY + MathUtils.floor(camera.viewportHeight)/2);

        //Reset debug stats
        totalChunksOnScreen = 0;
        chunksOnScreen.clear();
        pixelsCulled = 0;

        for (int chunkX = bottomLeft.getChunkIDx(); chunkX <= topRight.getChunkIDx(); chunkX++) {
            for (int chunkY = bottomLeft.getChunkIDy(); chunkY <= topRight.getChunkIDy(); chunkY++) {
                totalChunksOnScreen++;
                chunksOnScreen.add(drawChunk(chunkX, chunkY, batch, camera));
            }
        }
    }

    private Chunk drawChunk(int posX, int posY, SpriteBatch batch, OrthographicCamera camera) {
        Chunk chunk = getChunkFromID(posX, posY);
        for (int x = 0; x < getChunkSize(); x++) {
            for (int y = 0; y < getChunkSize(); y++) {
                int drawX = chunk.getChunkIDx()*getChunkSize() + x;
                int drawY = chunk.getChunkIDy()*getChunkSize() + y;

                //Cull pixels not in view
                if (camera.frustum.boundsInFrustum(drawX*scale,drawY*scale,0,scale,scale,0)) {
                    batch.setColor(chunk.getColorMap()[x % getChunkSize()][y % getChunkSize()]);
                    //Draw Pixel
                    if (drawPixel)
                        batch.draw(renderUtils.getTextures("pixel").get(0),
                                drawX*scale,
                                drawY*scale,
                                scale, scale);
                    else
                        batch.draw(tileset.getTile(chunk.getTileMap()[x % getChunkSize()][y % getChunkSize()]).getFrame(),
                            drawX*scale,
                            drawY*scale,
                            scale, scale);
                    batch.setColor(Color.WHITE);
                } else {
                    pixelsCulled++;
                }
            }
        }
        return chunk;
    }

    public void drawDebug(ShapeRenderer debugRenderer, OrthographicCamera camera) {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.setAutoShapeType(true);
        debugRenderer.begin();
        for (Chunk chunk : chunksOnScreen) {
            debugRenderer.rect(chunk.getWorldPositionX() * scale, chunk.getWorldPositionY() * scale, getChunkSize() * scale, getChunkSize() * scale);
        }
        debugRenderer.end();
    }

    public Chunk getChunkAtPos(int x, int y) {
        int chunkX = getChunkID(x, y)[0];
        int chunkY = getChunkID(x, y)[1];

        return getChunkFromID(chunkX, chunkY);
    }

    private int[] getChunkID(int x, int y) {
        return new int[] {((x / scale)/getChunkSize() - (x <= 0? 1: 0)), ((y / scale)/getChunkSize() - (y <= 0? 1: 0))};
    }

    public Chunk getChunkFromID(int x, int y) {
        if (!chunks.containsKey(x)) {
            chunks.put(x, new HashMap<Integer, Chunk>());
        }
        if (!chunks.get(x).containsKey(y)) {
            chunks.get(x).put(y, new Chunk(chunkSize, x, y, this));
            chunkCount++;
        }

        return chunks.get(x).get(y);
    }

    protected double getLandValue(int x, int y) {
        return (elevation.getNoise(x + x, y + y) + 1f) / 2f;
    }

    protected double getSecondValue(int x, int y) {
        return (secondNoise.getNoise(x + x, y + y) + 1f) / 2f;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public int getLandSeed() {
        return landSeed;
    }

    public int getTotalChunksOnScreen() {
        return totalChunksOnScreen;
    }

    public int getPixelsCulled() {
        return pixelsCulled;
    }

    public int getScale() {
        return scale;
    }
}
