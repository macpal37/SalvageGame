/*
 * JsonValueLoader.java
 *
 * This is a simple loader for processing json files (and making them assets managed
 * by the asset manager.
 *
 * This code is based on the template for SoundLoader by mzechner.
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

/**
 * This class is an {@link AssetLoader} to load {@link JsonValue} assets.
 */
public class JsonValueLoader extends AsynchronousAssetLoader<JsonValue, JsonValueLoader.JsonValueParameters> {
    /** The asynchronously read JsonValue */
    protected JsonValue cachedData;

    /**
     * The definable parameters for a {@link JsonValue}.
     */
    public static class JsonValueParameters extends AssetLoaderParameters<JsonValue> {
        // Since everything is defined in the file, nothing to do here
    }
    
    /**
     * Creates a new JsonValueLoader with an internal file resolver
     */
    public JsonValueLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new JsonValueLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public JsonValueLoader (FileHandleResolver resolver) {
        super(resolver);
    }

    /** 
     * Returns the {@link JsonValue} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link JsonValue} instance currently loaded by this loader.
     */
    protected JsonValue getLoadedJSON() {
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
    public void loadAsync (AssetManager manager, String fileName, FileHandle file, JsonValueParameters params) {
        JsonReader reader = new JsonReader();
        cachedData = reader.parse(file);
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
    public JsonValue loadSync (AssetManager manager, String fileName, FileHandle file, JsonValueParameters params) {
        JsonValue json = cachedData;
        cachedData = null;
        return json;
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
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, JsonValueParameters params) {
        return null;
    }

}