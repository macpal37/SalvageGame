/*
 * AssetDirectory.java
 *
 * This is an extension of AssetManager that uses a JSON to define the assets
 * to be used.  The JSON file is called the asset directory.  This class allows
 * you to load and unload the directory all at once.  It also allows you to 
 * refer to assets by their directory keys instead of the file names.  This
 * provides a more extensible way of adding assets.
 *
 * Right now, this asset manager only supports textures, fonts, audio, and other
 * JSON files. If you want an asset directory that provides support for other assets, 
 * you will need to add additional asset parsers.
 *
 * @author Walker M. White
 * @date   4/18/2020
 */
package com.xstudios.salvage.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.xstudios.salvage.util.*;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
//import com.xstudios.salvage.util.utils.ResourceManager;

/**
 * An asset manager that uses a JSON file to define its assets.
 *
 * The manager allows assets to be loaded normally, but it is best to use the global
 * {@link #loadAssets()} method to load from the JSON directory.  Once that method is 
 * called, you may use {@link #getEntry} instead of {@link AssetManager#get} to 
 * access an asset.
 *
 * As with any {@link AssetManager}, this class loads assets via {@link AssetLoader}
 * objects.  However, it also requires {@link AssetParser} objects.  An asset parser
 * takes an JSON entry in the directory and instructs the appropriate loader how to
 * load that file.  This class has built in parsers for the classes {@link Texture}, 
 * {@link TextureRegion},  {@link BitmapFont}, {@link Sound}, {@link Music}, and
 * {@link JsonValue}.
 *
 * If you wish to add custom assets, you need to add BOTH a custom {@link AssetLoader}
 * and a custom {@link AssetParser} to this manager.  While a type can only have one
 * loader per file suffix, there is no limit on the number of asset parsers per type.
 * Use the method {@link #addParser} to add additional asset parsers.
 * 
 * Right now, this class supports built-in reading for Textures (and Texture regions),
 * audio assets, fonts, and other JSON files.  To add more assets, you will need to
 * extend this class.
 *
 * We have decided not implement this class as a singleton. It is possible that you 
 * may want more than one asset manager (for managing separate scenes).  If you
 * need to globalize access to this asset manager, use {@link ResourceManager}.
 */
public class AssetDirectory extends AssetManager {
    /** The resolver (converting strings to file handles) */
    protected FileHandleResolver resolver;
    /** The asset directory of this asset manager */
    protected String filename;
    /** The directory contents (including the map from JSON keys to file names) */
    protected Index contents;
    /** The dedicated loader for the {@link Index} class */
    protected DirectoryLoader topLoader;

    /**
     * This class represents the top level index of an asset directory.
     *
     * It contains the contents of the JSON file as a {@link JsonValue}.  It also
     * stores the mapping of JSON keys to asset file names (which are how the 
     * individual loaders refer to the files 
     */
    public static class Index {
        /** The mapping from JSON keys to file names */
        ObjectMap<Class<?>, ObjectMap<String, String>> keymap;
        /** The contents of the JSON file */
        JsonValue directory;
        
        /**
         * Creates a new, empty directory index
         */
        public Index() {
            keymap = new ObjectMap<Class<?>, ObjectMap<String, String>>();
        }
    }

    /** 
     * A callback function for the directory loader
     *
     * We need this callback to extract the contents object
     */
    private DirectoryLoader.DirectoryLoaderParameters.LoadedCallback callback = new DirectoryLoader.DirectoryLoaderParameters.LoadedCallback() {
        /**
         * Assign contents to the result of the directory loader.
         *
         * @param manager   This asset manager
         * @param fileName  The directory file name
         * @param type      The directory file type (Index)
         */
        @Override
        public void finishedLoading(AssetManager manager, String fileName, Class type) {
            contents = manager.get( fileName, Index.class );
        }
    };

    /**
     * Creates a new AssetDirectory from the given directory.
     *
     * This class uses an {@link InternalFileHandleResolver} to convert asset file
     * names into assets.
     *
     * @param directory    The asset directory file name
     */
    public AssetDirectory(String directory) {
        this( directory, new InternalFileHandleResolver() );
    }

    /**
     * Creates a new AssetDirectory from the given directory.
     *
     * This class uses the given {@link FileHandleResolver} to convert asset file
     * names into assets.
     *
     * @param directory    The asset directory file name
     * @param resolver    The file handle resolver
     */
    public AssetDirectory(String directory, FileHandleResolver resolver) {
        super( resolver, false );
        filename = directory;
        this.resolver = resolver;

        // Add the default loaders
        topLoader = new DirectoryLoader( resolver );
        setLoader( Index.class, topLoader );
        setLoader( FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader( resolver ) );
        setLoader( BitmapFont.class, ".ttf", new FreetypeFontLoader( resolver ) );
        //setLoader( BitmapFont.class, new BitmapFontLoader( resolver ) ); // fallback

        setLoader( Sound.class, new SoundLoader( resolver ) );
        setLoader( Music.class, new com.badlogic.gdx.assets.loaders.MusicLoader( resolver ) );
        setLoader( Sound.class, new SoundLoader( resolver ) );
        setLoader( Music.class, new MusicLoader( resolver ) );

        setLoader( Pixmap.class, new PixmapLoader( resolver ) );
        setLoader( Texture.class, new TextureLoader( resolver ) );
        setLoader( TextureAtlas.class, new TextureAtlasLoader( resolver ) );
        setLoader( TextureRegion.class, new TextureRegionLoader( resolver ) );
        setLoader( FilmStrip.class, new FilmStripLoader( resolver ) );

        setLoader( JsonValue.class, new JsonValueLoader( resolver ) );

        setLoader( SoundBuffer.class, ".wav", new SoundBufferLoader( resolver ) );

        // Why not?
        setLoader( Skin.class, new SkinLoader( resolver ) );
        setLoader( ParticleEffect.class, new ParticleEffectLoader( resolver ) );
        setLoader( PolygonRegion.class, new PolygonRegionLoader( resolver ) );

        // And now the default parsers
        topLoader.addParser( new JsonValueParser() );
        topLoader.addParser( new TextureParser() );
        topLoader.addParser( new TextureRegionParser() );
        topLoader.addParser( new FilmStripParser() );
        topLoader.addParser( new FreetypeFontParser() );
        topLoader.addParser( new SoundParser() );
        topLoader.addParser( new MusicParser() );
    }

    /**
     * Returns the progress in percent of completion.
     *
     * @return the progress in percent of completion.
     */
    public synchronized float getProgress () {
        // This is a workaround for the problem with getProgress in AssetManager
        if (getLoadedAssets() == 0) { return 0.0f; }
        return (float)getLoadedAssets()/(getLoadedAssets()+getQueuedAssets());
    }

    /**
     * Returns the file name for the asset directory.
     *
     * @return the file name for the asset directory.
     */
    public String getDirectory() {
        return filename;
    }

    /**
     * Loads all assets defined by the asset directory
     * 
     * Each asset must have an associated {@link AssetParser} for this to work.
     * There are default parsers for the classes {@link Texture}, {@link TextureRegion},
     * {@link BitmapFont}, {@link Sound}, {@link Music}, {@link JsonValue}.
     *
     * Any additional asset parsers should be added with the {@link #addParser} method. 
     */
    public void loadAssets() {
        DirectoryLoader.DirectoryLoaderParameters params = new DirectoryLoader.DirectoryLoaderParameters();
        params.loadedCallback = callback;
        load( filename, Index.class, params );
    }

    
    /**
     * Unloads all assets previously loaded by {@link #loadAssets}.
     *
     * Assets loaded manually (e.g. not via the asset directory JSON) will not be
     * affected and will remain in this asset manager.
     */
    public void unloadAssets() {
        if (contents == null) {
            return;
        }
        for (ObjectMap<String, String> category : contents.keymap.values()) {
            for (String filename : category.values()) {
                unload( filename );
            }
        }
        contents = null;
    }
    
    /**
     * Returns the {@link AssetParser} objects associated with this directory loader
     *
     * If there are no asset parsers, then {@link #loadAssets} will not generate any
     * assets beyond the initial JSON file.  The contents of the JSON file will be ignored.
     *
     * Each asset type may have multiple asset parsers
     *
     * @return the {@link AssetParser} objects associated with this directory loader
     */
    public Array<AssetParser<?>> getParsers() {
        return topLoader.getParsers();
    }

    /**
     * Adds a {@link AssetParser} for this directory loader
     *
     * If there are no asset parsers, then {@link #loadAssets} will not generate any
     * assets beyond the initial JSON file.  The contents of the JSON file will be ignored.
     *
     * Each asset type may have multiple asset parsers
     *
     * @param parser    The {@link AssetParser} to add
     */
    public void addParser(AssetParser<?> parser) {
        topLoader.addParser(parser);
    }

    /**
     * Removes a {@link AssetParser} from this directory loader
     *
     * If there are no asset parsers, then {@link #loadAssets} will not generate any
     * assets beyond the initial JSON file.  The contents of the JSON file will be ignored.
     *
     * Each asset type may have multiple asset parsers
     *
     * @param parser    The {@link AssetParser} to remove
     */
    public void removeParser(AssetParser<?> parser) {
        topLoader.removeParser(parser);
    }

    /**
     * Returns the asset keys with the given directory
     *
     * @return the asset  keys with the given directory
     */
    public Array<String> getEntryKeys() {
        Array<String> result = new Array<String>();

        for(ObjectMap<String, String> value: contents.keymap.values()) {
            for(String key : value.keys()) {
                result.add( key );
            }
        }
        return result;
    }

    /**
     * Returns the asset associated with the given directory key
     * 
     * The method {@link #loadAssets} must have been called for this method to
     * return a value.
     *
     * @param key  the asset directory key
     * @param type the asset type
     *
     * @return the asset associated with the given directory key
     */
    public <T> T getEntry(String key, Class<T> type) {
        ObjectMap<String, String> keys = contents.keymap.get( type, null );
        if (keys == null) {
            return null;
        }
        String filename = keys.get( key, null );
        if (filename == null) {
            return null;
        }
        return get( filename, type );
    }

    /**
     * Returns true if there is an asset associated with the given directory key
     * 
     * The method {@link #loadAssets} must have been called for this method to
     * return anything other than false.
     *
     * @param key  the asset directory key
     * @param type the asset type
     *
     * @return true if there is an asset associated with the given directory key
     */
    public <T> boolean hasEntry(String key, Class<T> type) {
        ObjectMap<String, String> keys = contents.keymap.get( type, null );
        if (keys == null) {
            return false;
        }
        String filename = keys.get( key, null );
        if (filename == null) {
            return false;
        }
        return contains( filename, type );
    }
}
