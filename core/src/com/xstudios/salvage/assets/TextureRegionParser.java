package com.xstudios.salvage.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class TextureRegionParser implements AssetParser<TextureRegion> {
    private JsonValue root;
    private JsonValue atlas;

    public Class<TextureRegion> getType() {
        return TextureRegion.class;
    }

    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild( "textures" );
        atlas = null;
        advance();
    }

    public boolean hasNext() {
        return atlas != null;
    }

    public void processNext(AssetManager manager, ObjectMap<String,String> keymap) {
        if (atlas.size < 4) {
            throw new GdxRuntimeException( "Rectangle "+atlas+" is not valid");
        }
        String file = root.getString( "file", null );
        if (file == null) {
            advance();
            return;
        }
        TextureRegionLoader.TextureRegionParameters params = new TextureRegionLoader.TextureRegionParameters(file);

        params.x = atlas.getInt(0);
        params.y = atlas.getInt(1);
        params.width  = atlas.getInt(2);
        params.height = atlas.getInt(3);
        params.width = params.width == -1 ? -1 : params.width-params.x;
        params.height = params.height == -1 ? -1 : params.height-params.y;
        String region = file+":"+atlas.name();
        keymap.put(root.name()+"."+atlas.name(),region);
        manager.load( region,TextureRegion.class, params );
        advance();
    }

    public boolean equals(Object o) {
        return o instanceof TextureRegionParser;
    }

    private void advance() {
        if (atlas != null) {
            atlas = atlas.next();
            if (atlas == null) {
                root = root.next();
            }
        }
        while (atlas == null && root != null) {
            if (root.hasChild( "atlas" )) {
                atlas = root.getChild( "atlas" );
            } else {
                root = root.next();
            }
        }
    }

}