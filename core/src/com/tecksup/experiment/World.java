package com.tecksup.experiment;

import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;

public class World {

    private final int seed;

    private final SimplexNoise landNoise;
    private final SimplexNoise secondNoise;

    private final int chunkSize = 64;
    private int chunkCount = 0;
    private final HashMap<Integer, HashMap<Integer, Chunk>> chunks = new HashMap<>();

    public World() {
        this.seed = MathUtils.random(999999999);
        landNoise = new SimplexNoise(128, .58f, seed);
        secondNoise = new SimplexNoise(64, .5f, seed);
    }

    public double getLandValue(int x, int y) {
        return (landNoise.getNoise(x + x, y + y) + 1f) / 2f;
    }

    public double getSecondValue(int x, int y) {
        return (secondNoise.getNoise(x + x, y + y) + 1f) / 2f;
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

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public HashMap<Integer, HashMap<Integer, Chunk>> getChunks() {
        return chunks;
    }

    public int getSeed() {
        return seed;
    }
}
