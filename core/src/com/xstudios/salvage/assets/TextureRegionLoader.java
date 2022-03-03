/*
 * TextureRegionLoader.java
 *
 * This is a simple loader for processing texture regions (and making them assets managed
 * by the asset manager. This is a simpler form of texture atlas for students.
 *
 * This code is based on the template for PolygonRegions by dermetfan.
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * This class is an {@link AssetLoader} to load {@link TextureRegion} assets.
 *
 * A texture region asset should be specified by filename:name where name is a unique
 * name for the region.
 */
public class TextureRegionLoader extends AsynchronousAssetLoader<TextureRegion, TextureRegionLoader.TextureRegionParameters> {
    /** A reference to the file handle resolver (inaccessible in parent class) */
    protected FileHandleResolver resolver;
    /** The asynchronously read TextureRegion */
    private TextureRegion cachedRegion;

    /**
     * The definable parameters for a {@link TextureRegion}.
     * 
     * A texture region is a texture plus a rectangle.  This parameter defines both those
     * values (through a symbolic link to the texture in this manager).  A texture region
     * is fully defined  by these parameters.  The file name is just a symbolic reference.
     */ 
    public static class TextureRegionParameters extends AssetLoaderParameters<TextureRegion> {
        /** The reference to the texture in the asset manager */
        public String source;
        /** The starting x-coordinate for the texture region (measured in pixels) */
        public int x = 0;
        /** The starting y-coordinate for the texture region (measured in pixels)  */
        public int y = 0;
        /** The pixel width of the texture region; use -1 for "remaining" width */
        public int width  = -1;
        /** The pixel height of the texture region; use -1 for "remaining" height */
        public int height = -1;

        /**
         * Creates texture region parameters for the give texture.
         *
         * @param fileName    The file for the parent texture
         */
        public TextureRegionParameters(String fileName) {
            this.source = fileName;
        }

        /**
         * Creates texture region parameters with the given values.
         *
         * All values are measured in pixels (of the source file).
         *
         * @param fileName  The file for the parent texture
         * @param width     The width of the texture region
         * @param height    The height of the texture region
         */
        public TextureRegionParameters(String fileName, int width, int height) {
            this.source = fileName;
        }

        /**
         * Creates texture region parameters with the given values.
         *
         * All values are measured in pixels (of the source file).
         *
         * @param fileName  The file for the parent texture
         * @param x         The starting x-coordinate of the texture region
         * @param y         The starting y-coordinate of the texture region
         * @param width     The width of the texture region
         * @param height    The height of the texture region
         */
        public TextureRegionParameters(String fileName, int x, int y, int width, int height) {
            this.source = fileName;
        }

    }

    /**
     * Creates a new TextureRegionLoader with an internal file resolver
     */
    public TextureRegionLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new TextureRegionLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public TextureRegionLoader (FileHandleResolver resolver) {
        super(resolver);
        this.resolver = resolver;
    }

    /** 
     * Returns the {@link TextureRegion} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link TextureRegion} instance currently loaded by this loader.
     */
    protected TextureRegion getLoadedRegion () {
        return cachedRegion;
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
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, TextureRegionParameters params) {
        Texture texture = manager.get(manager.getDependencies(fileName).first());
        cachedRegion = load(texture, params);
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
    public TextureRegion loadSync(AssetManager manager, String fileName, FileHandle file, TextureRegionParameters params) {
        TextureRegion region = cachedRegion;
        cachedRegion = null;
        return region;
    }

    /**
     * Resolves the file for this texture region.
     *
     * A texture region asset should be specified by filename:name where name is a unique
     * name for the region.
     *
     * @param fileName  The file name to resolve
     *
     * @return handle to the file, as resolved by the file resolver.
     */
    @Override
    public FileHandle resolve (String fileName) {
        int suffix = fileName.lastIndexOf(':');
        if (suffix == -1) {
            throw new GdxRuntimeException( "Texture region file name must end in ':name'." );
        }
        String prefix = fileName.substring( 0,suffix );
        return resolver.resolve(prefix);
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
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, TextureRegionParameters params) {
        if (params == null) {
            int suffix = fileName.lastIndexOf(':');
            String prefix = (suffix == -1) ? fileName : fileName.substring( 0,suffix );
            params = new TextureRegionParameters( prefix );
        }
        Array<AssetDescriptor> deps = new Array<AssetDescriptor>(1);
        deps.add(new AssetDescriptor<Texture>( params.source, Texture.class));
        return deps;
    }

    /** 
     * Loads a texture region from the given texture and parameters.
     *
     * If params is null, the texture region will be the entire texture.
     *
     * @param texture   The parent texture
     * @param params    The definition of the texture region.
     *
     * @return a texture region for the given texture and parameters.
     */
    public TextureRegion load (Texture texture, TextureRegionParameters params) {
        if (params == null) {
            return new TextureRegion(texture);
        }
        int width  = params.width  < 0 ? texture.getWidth()-params.x  : params.width;
        int height = params.height < 0 ? texture.getHeight()-params.y : params.height;
        return new TextureRegion(texture,params.x,params.y,width,height);
    }
}
