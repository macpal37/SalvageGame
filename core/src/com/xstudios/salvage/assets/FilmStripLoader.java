/*
 * FilmStrimLoader.java
 *
 * This is a simple loader for processing filmstrips (and making them assets managed
 * by the asset manager). However, film strips are not freely reusable like textures
 * and texture regions. You should be careful to make sure that you only have one
 * version of the asset at a time.
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.xstudios.salvage.util.FilmStrip;

/**
 * This class is an {@link AssetLoader} to load {@link com.xstudios.salvage.util.FilmStrip} assets.
 *
 * A film strip asset should be specified by filename:name where name is a unique
 * name for the region.  Note that, unlike textures, film strips are not really
 * reusable. They all share a frame attribute. So each film strip asset must
 * be explicitly copied if you want to make another.
 */
public class FilmStripLoader extends AsynchronousAssetLoader<FilmStrip, FilmStripLoader.FilmStripParameters> {
    /** A reference to the file handle resolver (inaccessible in parent class) */
    protected FileHandleResolver resolver;
    /** The asynchronously read FilmStrip */
    private FilmStrip cachedSprite;

    /**
     * The definable parameters for a {@link FilmStrip}.
     *
     * A filmstrip is a texture broken up into equal sized components, read top-to-bottom
     * and left-to-right.  It is defined by the number of rows, the number of columns,
     * and the size. The file name is just a symbolic reference.
     */
    public static class FilmStripParameters extends AssetLoaderParameters<FilmStrip> {
        /** The reference to the texture in the asset manager */
        public String source;
        /** The number of rows in the film strip */
        public int rows = 1;
        /** The number of columns in the film strip  */
        public int cols = 1;
        /** The number of frames in the film strip */
        public int size  = 1;
        /** The initial frame to start with */
        public int frame = 0;
        /** The starting x-coordinate for the film strip (measured in pixels) */
        public int x = 0;
        /** The starting y-coordinate for the film strip (measured in pixels)  */
        public int y = 0;
        /** The pixel width of the full sprite sheet; use -1 for "remaining" width */
        public int width  = -1;
        /** The pixel height of the full sprite sheet; use -1 for "remaining" height */
        public int height = -1;

        /**
         * Creates film strip parameters for the give texture.
         *
         * @param fileName    The file for the parent texture
         */
        public FilmStripParameters(String fileName) {
            this.source = fileName;
        }

        /**
         * Creates film strip parameters with the given values.
         *
         * The size is assumed to be rows*cols
         *
         * @param fileName  The file for the parent texture
         * @param rows      The number of rows of the film strip
         * @param cols      The number of rows of the film strip
         */
        public FilmStripParameters(String fileName, int rows, int cols) {
            this.source = fileName;
            this.rows = rows;
            this.cols = cols;
            size = rows*cols;
        }

        /**
         * Creates film strip parameters with the given values.
         *
         * @param fileName  The file for the parent texture
         * @param rows      The number of rows of the film strip
         * @param cols      The number of rows of the film strip
         * @param size      The number of animation frames
         */
        public FilmStripParameters(String fileName, int rows, int cols, int size) {
            this.source = fileName;
            this.rows = rows;
            this.cols = cols;
            this.size = size;
        }

    }

    /**
     * Creates a new TextureRegionLoader with an internal file resolver
     */
    public FilmStripLoader() {
        this(new InternalFileHandleResolver());
    }

    /**
     * Creates a new TextureRegionLoader with the given file resolver
     *
     * @param resolver    The file resolver
     */
    public FilmStripLoader (FileHandleResolver resolver) {
        super(resolver);
        this.resolver = resolver;
    }

    /**
     * Returns the {@link FilmStrip} instance currently loaded by this loader.
     *
     * If nothing has been loaded, this returns {@code null}.
     *
     * @return the {@link FilmStrip} instance currently loaded by this loader.
     */
    protected FilmStrip getLoadedSprite () {
        return cachedSprite;
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
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, FilmStripParameters params) {
        Texture texture = manager.get(manager.getDependencies(fileName).first());
        cachedSprite = load(texture, params);
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
    public FilmStrip loadSync(AssetManager manager, String fileName, FileHandle file, FilmStripParameters params) {
        FilmStrip region = cachedSprite;
        cachedSprite = null;
        return region;
    }

    /**
     * Resolves the file for this film string
     *
     * A film strip asset should be specified by filename:name where name is a unique
     * name for the sprite properties.
     *
     * @param fileName  The file name to resolve
     *
     * @return handle to the file, as resolved by the file resolver.
     */
    @Override
    public FileHandle resolve (String fileName) {
        int suffix = fileName.lastIndexOf(FilmStripParser.ALIAS_SEP);
        if (suffix == -1) {
            throw new GdxRuntimeException( "Texture region file name must end in "+FilmStripParser.ALIAS_SEP+"name'." );
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
    public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, FilmStripParameters params) {
        if (params == null) {
            int suffix = fileName.lastIndexOf(FilmStripParser.ALIAS_SEP);
            String prefix = (suffix == -1) ? fileName : fileName.substring( 0,suffix );
            params = new FilmStripParameters( prefix );
        }
        Array<AssetDescriptor> deps = new Array<AssetDescriptor>(1);
        deps.add(new AssetDescriptor<Texture>( params.source, Texture.class));
        return deps;
    }

    /**
     * Loads a film strip from the given texture and parameters.
     *
     * If params is null, the texture region will be the entire texture.
     *
     * @param texture   The parent texture
     * @param params    The definition of the texture region.
     *
     * @return a texture region for the given texture and parameters.
     */
    public FilmStrip load (Texture texture, FilmStripParameters params) {
        if (params == null) {
            return new FilmStrip(texture,1,1);
        }

        int w = params.width == -1 ? texture.getWidth()-params.x : params.width;
        int h = params.height == -1 ? texture.getHeight()-params.y : params.height;
        FilmStrip result = new FilmStrip(texture,params.rows,params.cols,params.size,
                                          params.x,params.y,w,h);
        result.setFrame( params.frame );
        return result;
    }
}