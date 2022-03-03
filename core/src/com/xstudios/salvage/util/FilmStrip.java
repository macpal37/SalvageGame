/* 
 * FilmStrip.java
 *
 * A filmstrip is a single image with multiple copies of the game object
 * as different animation frames.  The frames are arranged in rows and 
 * columns, starting at the top-left.  If there are any blank frames, 
 * they are at the end of the filmstrip (e.g. the bottom right). 
 * 
 * The frames must all be equally sized. The size of each frame is the image
 * width divided by the number of columns, and the image height divided
 * by the number of rows.  If the frames are not equally sized, this class
 * will not animate properly.
 * 
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Texture class providing flipbook animation.
 * 
 * The class breaks up the image into regions, according the number of
 * rows and columns in the image.  It then indexes each region by frame,
 * starting from the top-left and processing one row at a time.
 * 
 * This is a subclass of TextureRegion, and so it keeps a rectangle region
 * that tracks the active part of the image to use for drawing.  See the
 * API for that class to understand how a TextureRegion.  The primary 
 * advantage of this class is that it can quickly compute the new region
 * from the frame number.
 */
public class FilmStrip extends TextureRegion {
	/** The number of rows in this filmstrip */
	private int rows;
	/** The number of columns in this filmstrip */
	private int cols;
	/** The width of a single frame; computed from column count */
	private int fwidth;	
	/** The height of a single frame; computed from row count */
	private int fheight;
	
	/** The x-origin of the film strip (in case it is not the whole texture) */
	private int x;
	/** The y-origin of the film strip (in case it is not the whole texture) */
	private int y;
	/** The width of the film strip (in case it is not the whole texture) */
	private int width;
	/** The width of the film strip (in case it is not the whole texture) */
	private int height;
	
	/** The number of frames in this filmstrip */
	private int size;	
	/** The active animation frame */
	private int frame;
	
	/**
	 * Creates a new filmstrip from the given texture.
	 *
	 * The filmstrip will use the entire texture.
	 * 
	 * @param texture The texture image to use
	 * @param rows The number of rows in the filmstrip
	 * @param cols The number of columns in the filmstrip
	 */
	public FilmStrip(Texture texture, int rows, int cols) {
		this(texture,rows,cols,rows*cols);
	}
	
	/**
	 * Creates a new filmstrip from the given texture.
	 * 
	 * The parameter size is to indicate that there are unused frames in
	 * the filmstrip.  The value size must be less than or equal to
	 * rows*cols, or this constructor will raise an error.
	 *
	 * The filmstrip will use the entire texture.
	 * 
	 * @param texture The texture image to use
	 * @param rows The number of rows in the filmstrip
	 * @param cols The number of columns in the filmstrip
	 * @param size The number of frames in the filmstrip
	 */
	public FilmStrip(Texture texture, int rows, int cols, int size) {
		this(texture,rows,cols,size,0,0, texture.getWidth(), texture.getHeight() );
	}

	/**
	 * Creates a new filmstrip from the given texture.
	 * 
	 * The parameter size is to indicate that there are unused frames in
	 * the filmstrip.  The value size must be less than or equal to
	 * rows*cols, or this constructor will raise an error.
	 *
	 * This version of the constructor allows you to specify the film
	 * strip as a subset of the entire texture.
	 * 
	 * @param texture 	The texture image to use
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 * @param x   		The x-position of the film strip origin
	 * @param y   		The y-position of the film strip origin
	 * @param width   	The width of the entire film strip
	 * @param height	The height of the entire film strip
	 */
	public FilmStrip(Texture texture, int rows, int cols, int size, 
					 int x, int y, int width, int height) {
		super(texture);
		if (size > rows*cols) {
			Gdx.app.error("FilmStrip", "Invalid strip size", new IllegalArgumentException());
			return;
		}
		this.rows = rows;
		this.cols = cols;
		this.size = size;
		fwidth  = width/cols;
		fheight = height/rows;
		setFrame(0);
	}

	
	/**
	 * Returns the number of frames in this filmstrip.
	 * 
	 * @return the number of frames in this filmstrip.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the current active frame.
	 * 
	 * @return the current active frame.
	 */
	public int getFrame() {
		return frame;
	}
	
	/**
	 * Sets the active frame as the given index.
	 * 
	 * If the frame index is invalid, an error is raised.
	 * 
	 * @param frame the index to make the active frame
	 */
	public void setFrame(int frame) {
		if (frame < 0 || frame >= size) {
			Gdx.app.error("FilmStrip", "Invalid animation frame", new IllegalArgumentException());
			return;
		}
		this.frame = frame;
		int x = this.x+(frame % cols)*fwidth;
		int y = this.y+(frame / cols)*fheight;
		setRegion(x,y,fwidth,fheight);
	}

	/**
	 * Returns a copy of this filmstrip.
	 *
	 * Sometimes we want a filmstrip with the same texture, but set
	 * to an different animation frame. That is the point of this
	 * method.
	 *
	 * @return a copy of this filmstrip
	 */
	public FilmStrip copy() {
		return new FilmStrip( getTexture(), rows, cols, size );
	}

}