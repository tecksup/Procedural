package com.tecksup.experiment.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class RenderUtils {

    private final AssetManager manager = new AssetManager();
    public TextureAtlas textureAtlas;
    private final String textureAtlasFilename = "textureAtlas/atlas.atlas";

    private final BitmapFont font;

    private final HashMap<String, Array<TextureAtlas.AtlasRegion>> cachedTextureGroups = new HashMap<>();

    private boolean fullyLoaded = false;
    private Runnable onLoad;

    public RenderUtils(Runnable finishedLoadingRunnable) {

        font = new BitmapFont(Gdx.files.internal("Fonts/PixPrompt.fnt"), new TextureRegion(new Texture(Gdx.files.internal("Fonts/PixPrompt.png"))));
        font.getData().markupEnabled = true;

        // load the texture atlas
        manager.load(textureAtlasFilename, TextureAtlas.class);
        onLoad = finishedLoadingRunnable;
    }

    public boolean doneLoading() {
        if (fullyLoaded)
            return true;

        manager.update();

        if (manager.isFinished()) {
            if (textureAtlas == null) // Retrieve the loaded texture atlas
                textureAtlas = manager.get(textureAtlasFilename, TextureAtlas.class);
            if (onLoad != null) {
                onLoad.run();
                onLoad = null;
            }
            fullyLoaded = true;
        }

        return manager.isFinished();
    }

    public Array<TextureAtlas.AtlasRegion> getTextures(String name) {
        Array<TextureAtlas.AtlasRegion> textures = cachedTextureGroups.get(name);
        if (textures == null) {
            textures = textureAtlas.findRegions(name);
            if (textures.size == 0)
                throw new RuntimeException("Group of regions ( .findRegions() ) not found on atlas, name: " + name);

            cachedTextureGroups.put(name, textures);
        }

        return textures;
    }

    public boolean hasTextures(String name) {
        Array<TextureAtlas.AtlasRegion> textures;
        textures = textureAtlas.findRegions(name);
        return textures.size != 0;
    }

    public BitmapFont getFont() {
        return font;
    }

    public void dispose() {
        font.dispose();
        textureAtlas.dispose();
        manager.dispose();
    }

}
