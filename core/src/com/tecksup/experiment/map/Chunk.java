package com.tecksup.experiment.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import static com.tecksup.experiment.Experiment.drawPixel;

public class Chunk {

    //Each chunk is an array of size ChunkSize
    private final Color[][] colorMap;
    private final int[][] tileMap;

    private final int worldPositionX;
    private final int worldPositionY;

    private final int chunkIDx;
    private final int chunkIDy;

    public Chunk(int size, int xPos, int yPos, World world) {
        this.chunkIDx = xPos;
        this.chunkIDy = yPos;
        this.worldPositionX = xPos * size;
        this.worldPositionY = yPos * size;
        colorMap = new Color[size][size];
        tileMap = new int[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float landVal = (float) world.getLandValue(x + worldPositionX, y + worldPositionY);
                landVal = MathUtils.round(landVal*world.elevClamp) / world.elevClamp;
                float secondVal = (float) world.getSecondValue(x + worldPositionX, y + worldPositionY);

                tileMap[x][y] = 0;

                if (getLandType(landVal, world).equals(landType.WATER)) {
                    tileMap[x][y] = 416;
                    colorMap[x][y] = new Color((1-landVal) * (landVal * -1), (1-landVal) * (landVal * -1), (1-landVal), 1);
                } else if (getLandType(landVal, world).equals(landType.SHALLOW_WATER)) {
                    tileMap[x][y] = 416;
                    colorMap[x][y] = new Color((1-landVal) * (landVal * -1)+0.08f, (1-landVal) * (landVal * -1)+0.12f, (1-landVal)+0.12f, 1);
                } else if (getLandType(landVal, world).equals(landType.SAND)) {
                    tileMap[x][y] = 26;
                    if (!drawPixel)
                        colorMap[x][y] = Color.WHITE;
                    else
                        colorMap[x][y] = new Color(1f, 1f, landVal, 1);
                } else { //Land
                    if (getBiomeType(secondVal).equals(biomeType.LUSH_FOREST)) {
                        tileMap[x][y] = 15;
                        if (!drawPixel)
                            colorMap[x][y] = Color.WHITE;
                        else
                            colorMap[x][y] = new Color(landVal * secondVal, landVal / secondVal, landVal * secondVal, 1);
                    } else {
                        tileMap[x][y] = 14;
                        if (!drawPixel)
                            colorMap[x][y] = Color.WHITE;
                        else
                            colorMap[x][y] = new Color(landVal, landVal / secondVal, landVal, 1);
                    }
                }
            }
        }
    }


    protected landType getLandType(float val, World world) {
        if (val > world.waterElev)
            return landType.WATER;
        else if (val > world.waterElev - 0.03f)
            return landType.SHALLOW_WATER;
        else if (val > world.waterElev - 0.06f)
            return landType.SAND;
        else
            return landType.LAND;
    }

    protected biomeType getBiomeType(float val) {
        if (val > 0.5f)
            return biomeType.LUSH_FOREST;
        else
            return biomeType.FOREST;
    }

    public Color[][] getColorMap() {
        return colorMap;
    }

    public int[][] getTileMap() {
        return tileMap;
    }

    public int getWorldPositionX() {
        return worldPositionX;
    }

    public int getWorldPositionY() {
        return worldPositionY;
    }

    public int getChunkIDx() {
        return chunkIDx;
    }

    public int getChunkIDy() {
        return chunkIDy;
    }

    enum landType {
        SHALLOW_WATER, WATER, SAND, LAND
    }

    enum biomeType {
        FOREST, LUSH_FOREST
    }
}
