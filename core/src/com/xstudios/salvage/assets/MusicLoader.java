/*
 * MusicBufferLoader.java
 *
 * This is a simple loader for processing music buffers (and making them assets managed
 * by the asset manager.  This is required for using the new audio engine.
 *
 * This code is based on the template for SoundLoader by mzechner.
 *
 * @author Walker M. White
 * @data   04/20/2020
 */
package com.xstudios.salvage.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * This class is an {@link AssetLoader} to load {@link Music} assets.
 *
 * Given the primitive state of LibGDX audio, we cannot do much more than
 * specify the filename when loading the asset.
 */
public class MusicLoader extends AsynchronousAssetLoader<Music, MusicLoader.MusicParameters> {
    /** The asynchronously read MusicBuffer */
    private Music cachedMusic;
    
    /**
     * The definable parameters for a {@link Music} object.
     */
    public static  class MusicParameters extends AssetLoaderParameters<Music> {
        // Since everything is defined in the file, nothing to do here
    }

    /**
     * Creates a new MusicBufferLoader with an internal file resolver
     */
    public MusicLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new MusicBufferLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public MusicLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    /** 
     * Returns the {@link Music} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link Music} instance currently loaded by this loader.
     */
    protected Music getLoadedMusic () {
        return cachedMusic;
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
    public void loadAsync (AssetManager manager, String fileName, FileHandle file, MusicParameters params) {
        cachedMusic = Gdx.audio.newMusic( file );
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
    public Music loadSync (AssetManager manager, String fileName, FileHandle file, MusicParameters params) {
        Music music = cachedMusic;
        cachedMusic = null;
        return music;
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
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, MusicParameters params) {
        return null;
    }

}
