/*
 * SoundBufferParser.java
 *
 * This is an interface for parsing a JSON entry into a SoundBuffer asset. SoundBuffer
 * assets do not have any properties other than the file name (because all properties)
 * are stored in the file.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package com.xstudios.salvage.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
/**
 * This class parses a JSON entry into a {@link Sound} object.
 *
 * Sound assets do not have any properties other than the file name, due to
 * the limitations of LibGDX audio.
 */
public class SoundParser implements AssetParser<Sound> {
    /** The current font entry in the JSON directory */
    private JsonValue root;

    /**
     * Returns the asset type generated by this parser
     *
     * @return the asset type generated by this parser
     */
    public Class<Sound> getType() {
        return Sound.class;
    }

    /**
     * Resets the parser iterator for the given directory.
     *
     * The value directory is assumed to be the root of a larger JSON structure.
     * The individual assets are defined by subtrees in this structure.
     *
     * @param directory    The JSON representation of the asset directory
     */
    public void reset(JsonValue directory) {
        root = directory;
        root = root.getChild( "sounds" );
    }

    /**
     * Returns true if there are still assets left to generate
     *
     * @return true if there are still assets left to generate
     */
    public boolean hasNext() {
        return root != null;
    }

    /**
     * Processes the next available sound object, loading it into the asset manager
     *
     * {@link Sound} objects have no additional loader properties.  They are
     * specified key : filename.
     *
     * This method fails silently if there are no available assets to process.
     *
     * @param manager    The asset manager to load an asset
     * @param keymap    The mapping of JSON keys to asset file names
     */
    public void processNext(AssetManager manager, ObjectMap<String,String> keymap) {
        String file = root.asString();
        keymap.put(root.name(),file);
        manager.load( file, Sound.class, null );
        root = root.next();
    }

    /**
     * Returns true if o is another SoundBufferParser
     *
     * @return true if o is another SoundBufferParser
     */
    public boolean equals(Object o) {
        return o instanceof SoundParser;
    }

}