/*
 * DirectoryLoader.java
 *
 * This is the core loader for AssetDirectory.  It allows us to asynchronously load
 * the initial asset directory AND to parse its entries.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package com.xstudios.salvage.assets;


import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * This class is an {@link AssetLoader} to load {@link AssetDirectory.Index} assets.
 *
 * This is the "top-level" loader for {@link AssetDirectory}. It is necessary to load
 * the initial directory JSON.  But it also has integrated {@link AssetParser} objects
 * to turn to contents of that JSON into more assets.
 */
public class DirectoryLoader extends AsynchronousAssetLoader<AssetDirectory.Index, DirectoryLoader.DirectoryLoaderParameters> {
    /** The asynchronously read AssetDirectory.Index */
    protected AssetDirectory.Index cachedData;
	/** The associated parsers for parsing the JSON contents */
    protected Array<AssetParser<?>> parsers;

    /**
     * The definable parameters for a {@link AssetDirectory.Index}.
     */
    public static class DirectoryLoaderParameters extends AssetLoaderParameters<AssetDirectory.Index> {
        // Since everything is defined in the file, nothing to do here
    }

    /**
     * Creates a new DirectoryLoader with an internal file resolver
     */
    public DirectoryLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new DirectoryLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public DirectoryLoader (FileHandleResolver resolver) {
        super(resolver);
        parsers = new Array<AssetParser<?>>(false, 16);
    }

	/**
	 * Returns the {@link AssetParser} objects associated with this directory loader
	 *
	 * If there are no asset parsers, then no assets will be generated beyond the
	 * initial JSON file.  The contents of the JSON file will be ignored.
	 *
	 * Each asset type may have multiple asset parsers
	 *
	 * @return the {@link AssetParser} objects associated with this directory loader
	 */
    public Array<AssetParser<?>> getParsers() {
        return parsers;
    }

	/**
	 * Adds a {@link AssetParser} for this directory loader
	 *
	 * If there are no asset parsers, then no assets will be generated beyond the
	 * initial JSON file.  The contents of the JSON file will be ignored.
	 *
	 * Each asset type may have multiple asset parsers
	 *
	 * @param parser	The {@link AssetParser} to add
	 */
    public void addParser(AssetParser<?> parser) {
        parsers.add(parser);
    }

	/**
	 * Removes a {@link AssetParser} from this directory loader
	 *
	 * If there are no asset parsers, then no assets will be generated beyond the
	 * initial JSON file.  The contents of the JSON file will be ignored.
	 *
	 * Each asset type may have multiple asset parsers
	 *
	 * @param parser	The {@link AssetParser} to remove
	 */
    public void removeParser(AssetParser<?> parser) {
        parsers.removeValue(parser,false);
    }
    
    /**
     * Returns the {@link JsonValue} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link JsonValue} instance currently loaded by this loader.
     */
    protected AssetDirectory.Index getLoadedDirectory() {
        return cachedData;
    }

    /**
     * Loads thread-safe part of the asset and injects any dependencies into the AssetManager.
     *
     * This is used to load non-OpenGL parts of the asset that do not require the context
     * of the main thread.
     *
     * @param manager   The asset manager
     * @param fileName  The name of the asset to load
     * @param file      The resolved file to load
     * @param params    The parameters to use for loading the asset
     */
    @Override
    public void loadAsync (AssetManager manager, String fileName, FileHandle file, DirectoryLoaderParameters params) {
        JsonReader reader = new JsonReader();
        cachedData = new AssetDirectory.Index();
        cachedData.directory = reader.parse(file);
        for(AssetParser<?> parser : parsers) {
            ObjectMap<String,String> keys = cachedData.keymap.get( parser.getType(), null );
            if (keys == null) {
                keys = new ObjectMap<String,String>();
                cachedData.keymap.put(parser.getType(),keys);
            }
            parser.reset( cachedData.directory );
            while (parser.hasNext()) {
                parser.processNext( manager, keys );
            }
        }
    }

    /**
     * Loads the main thread part of the asset.
     *
     * This is used to load OpenGL parts of the asset that require the context of the
     * main thread.
     *
     * @param manager   The asset manager
     * @param fileName  The name of the asset to load
     * @param file      The resolved file to load
     * @param params    The parameters to use for loading the asset
     */
    @Override
    public AssetDirectory.Index loadSync (AssetManager manager, String fileName, FileHandle file, DirectoryLoaderParameters params) {
        AssetDirectory.Index directory = cachedData;
        cachedData = null;
        return directory;
    }

    /**
     * Returns the other assets this asset requires to be loaded first.
     *
     * This method may be called on a thread other than the GL thread. It may return
     * null if there are no dependencies.
     *
     * @param fileName  The name of the asset to load
     * @param file      The resolved file to load
     * @param params parameters for loading the asset
     *
     * @return the other assets this asset requires to be loaded first.
     */
    @Override
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, DirectoryLoaderParameters params) {
        return null;
    }



}
