package com.tecksup.experiment;

import com.badlogic.gdx.graphics.Texture;

public class Chunk {

    //Each chunk is an array of size ChunkSize
    float[][] landMap;
    float[][] secondMap;

    int worldPositionX;
    int worldPositionY;

    public Chunk(int size, int xPos, int yPos, SimplexNoise landNoise, SimplexNoise secondNoise) {
        this.worldPositionX = xPos * size;
        this.worldPositionY = yPos * size;
        landMap = new float[size][size];
        secondMap = new float[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                landMap[x][y] = (float) ((landNoise.getNoise(x + worldPositionX, y + worldPositionY) + 1f) / 2f);
                secondMap[x][y] = (float) ((secondNoise.getNoise(x + worldPositionX, y + worldPositionY) + 1f) / 2f);
            }
        }
    }
}
