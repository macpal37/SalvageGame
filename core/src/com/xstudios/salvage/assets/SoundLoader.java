/*
 * SoundBufferLoader.java
 *
 * This is a simple loader for processing sound buffers (and making them assets managed
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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * This class is an {@link AssetLoader} to load {@link Sound} assets.
 *
 * Given the primitive state of LibGDX audio, we cannot do much more than
 * specify the filename when loading the asset.
 */
public class SoundLoader extends AsynchronousAssetLoader<Sound, SoundLoader.SoundParameters> {
    /** A reference to the file handle resolver (inaccessible in parent class) */
    protected FileHandleResolver resolver;
    /** The asynchronously read Sound */
    private Sound cachedSound;

    /**
     * The definable parameters for a {@link Sound} object.
     */
	static public class SoundParameters extends AssetLoaderParameters<Sound> {
        // Since everything is defined in the file, nothing to do here
    }

    /**
     * Creates a new SoundBufferLoader with an internal file resolver
     */
    public SoundLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new SoundBufferLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public SoundLoader(FileHandleResolver resolver) {
        super(resolver);
        this.resolver = resolver;
    }

    /** 
     * Returns the {@link Sound} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link Sound} instance currently loaded by this loader.
     */
    protected Sound getLoadedSound () {
        return cachedSound;
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
    public void loadAsync (AssetManager manager, String fileName, FileHandle file, SoundParameters params) {
        cachedSound = Gdx.audio.newSound(file);
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
    public Sound loadSync (AssetManager manager, String fileName, FileHandle file, SoundParameters params) {
        Sound sound = cachedSound;
        cachedSound = null;
        return sound;
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
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SoundParameters params) {
        return null;
    }

}  
