package com.tecksup.experiment.map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.gson.JsonObject;
import com.tecksup.experiment.data.TkTextureAnimation;

import static com.tecksup.experiment.Experiment.renderUtils;
import static com.tecksup.experiment.data.Common.FormatterObject;
import static com.tecksup.experiment.data.Common.stringBuilderObject;

public class TkTileset {

    String Name;
    int TileSize;
    float TileSpeed;
    int TotalTiles;

    private TkTextureAnimation<TextureAtlas.AtlasRegion>[] Tiles;
    private int[] TerrainType;
    private int[] GraphType;

    public TkTileset(JsonObject TilesetJson) {
        Name = TilesetJson.get("Name").getAsString();
        TileSize = TilesetJson.get("TileSize").getAsInt();
        TileSpeed = TilesetJson.get("TileSpeed").getAsFloat();

        TotalTiles = 0;
        while (hasTile(TotalTiles)) {
            TotalTiles++;
        }

        if (TilesetJson.has("TerrainMappings")) {
            //Here we generate the Terrain and Graph assignments to each tile
            String PreparedBitString = TilesetJson.get("TerrainMappings").getAsString();
            String[] Bits = PreparedBitString.split(",");

            TerrainType = new int[TotalTiles];
            for (int i = 0; i < TotalTiles; i++) {
                if (i < Bits.length)
                    TerrainType[i] = Integer.parseInt(Bits[i]);
                else
                    TerrainType[i] = 0;
            }
        } else {
            TerrainType = new int[TotalTiles];
            for (int i = 0; i >= TotalTiles; i++) {
                TerrainType[i] = 0;
            }
            //If there is an exception here, check that the tileset name matches the tileset images name
        }

        if (TilesetJson.has("GraphMappings")) {
            //Here we generate the Terrain and Graph assignments to each tile
            String PreparedBitString = TilesetJson.get("GraphMappings").getAsString();
            String[] Bits = PreparedBitString.split(",");

            GraphType = new int[TotalTiles];
            for (int i = 0; i < TotalTiles; i++) {
                if (i < Bits.length)
                    GraphType[i] = Integer.parseInt(Bits[i]);
                else
                    GraphType[i] = 0;
            }
        } else {
            GraphType = new int[TotalTiles];
            for (int i = 0; i >= TotalTiles; i++) {
                GraphType[i] = 0;
            }
        }

        Tiles = new TkTextureAnimation[TotalTiles];

        for (int i = 0; i < TotalTiles; i++) {
            String temp = FormatterObject.format("%03d", i).toString();
            stringBuilderObject.setLength(0);
            Tiles[i] = new TkTextureAnimation(renderUtils.getTextures(Name + "-" + temp), TileSpeed);
        }

    }

    public TkTextureAnimation<TextureAtlas.AtlasRegion> getTile(int ID) {
        if (Tiles == null || ID == -1 || ID > Tiles.length-1)
            return Tiles[0];
        if (Tiles[ID] != null)
            if (ID == -1)
                return Tiles[0];
            else
                return Tiles[ID];
        else
            return Tiles[0];
    }

    private boolean hasTile(int ID) {
        String temp = FormatterObject.format("%03d", ID).toString();
        stringBuilderObject.setLength(0);

        return renderUtils.hasTextures(Name + "-" + temp);
    }

    public int getTilesSize() {
        return TotalTiles;
    }

    public void update(float Time) {
        for (TkTextureAnimation<TextureAtlas.AtlasRegion> tile : Tiles) {
            tile.update(Time);
        }
    }

    public void refreshAtlas() {
        Tiles = new TkTextureAnimation[TotalTiles];

        for (int i = 0; i < TotalTiles; i++) {
            String temp = FormatterObject.format("%03d", i).toString();
            stringBuilderObject.setLength(0);
            Tiles[i] = new TkTextureAnimation(renderUtils.getTextures(temp), TileSpeed, true);
        }
    }

    public String getName() {
        return Name;
    }

    public int[] getGraphType() {
        return GraphType;
    }

    public int[] getTerrainType() {
        return TerrainType;
    }

}
