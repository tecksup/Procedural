package com.tecksup.experiment;

import com.badlogic.gdx.graphics.Color;

public class Chunk {

    //Each chunk is an array of size ChunkSize
    private final Color[][] colorMap;

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

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float landVal = (float) world.getLandValue(x + worldPositionX, y + worldPositionY);
                float secondVal = (float) world.getSecondValue(x + worldPositionX, y + worldPositionY);

                if (landVal > 0.5) { //Water
                    colorMap[x][y] = new Color(landVal * (landVal * -1), landVal * (landVal * -1), landVal, 1);
                } else { //Land
                    if (landVal > 0.46) //Sand
                        colorMap[x][y] = new Color(1f, 1f, landVal, 1);
                    else {
                        if (secondVal > 0.5f) {
                            colorMap[x][y] = new Color(landVal * secondVal, landVal / secondVal, landVal * secondVal, 1);
                        } else {
                            colorMap[x][y] = new Color(landVal, landVal / secondVal, landVal, 1);
                        }
                    }
                }
            }
        }
    }

    public Color[][] getColorMap() {
        return colorMap;
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
}
