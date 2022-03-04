/*
 * GameCanvas.java
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * You should change exactly ONE method in this class.  Look for the master
 * draw method.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 * This version of GameCanvas only supports (rectangular) Sprite drawing.
 * support for polygonal textures and drawing primitives will be present
 * in future labs.
 *
 * The functionality in this lab is not that much more than SpriteBatch.
 * However, SpriteBatch implements its order of operations (rotation, scaling,
 * translation) incorrectly, so we need this class to compensate.  By "incorrectly"
 * we mean contrary to anything you would ever be taught in 4620.
 */
public class GameCanvas {
	/** Drawing context to handle textures as sprites */
	private SpriteBatch spriteBatch;
	
	/** Value to cache window width (if we are currently full screen) */
	private int width;
	/** Value to cache window height (if we are currently full screen) */
	private int height;
	
	/** Track whether or not we are active (for error checking) */
	private boolean active;
	
	/** The current color blending mode */
	private BlendState blend;
	
	// CACHE OBJECTS
	/** Affine cache for current sprite to draw */
	private Affine2 local;
	/** Affine cache for all sprites this drawing pass */
	private Affine2 global;
	/** Cache object to unify everything under a master draw method */
	private TextureRegion holder;

	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.
	 */
	public GameCanvas() {
		this.width  = Gdx.graphics.getWidth();
		this.height = Gdx.graphics.getHeight();

		active = false;
		spriteBatch = new SpriteBatch();
		
		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		
		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Affine2();
	}
	
	/**
	 * Creates a new GameCanvas of the given size.
	 *
	 * The canvas will be displayed in a window, not fullscreen.
	 *
	 * @param width 	The width of the canvas window
	 * @param height 	The height of the canvas window
	 */
	public GameCanvas(int width, int height) {
		this(width,height,false);
	}

	/**
	 * Creates a new GameCanvas with the giving parameters.
	 *
	 * This constructor will completely override the settings in the
	 * LWGJApplicationConfig object used to start the application.
	 *
	 * @param width 		The width of the canvas window
	 * @param height 		The height of the canvas window
	 * @param fullscreen 	Whether or not the window should be full screen.
	 */	 
	protected GameCanvas(int width, int height, boolean fullscreen) {
		// Create a new graphics manager.
		this.width  = width;
		this.height = height;
		if (fullscreen) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
			
		}
		
		// Continue as normal
		active = false;
		spriteBatch = new SpriteBatch();

		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		
		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Affine2();
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		spriteBatch.dispose();
    	spriteBatch = null;
    	global = null;
    	local  = null;
    	holder = null;
    }

	/**
	 * Returns the width of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getWidth()
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 * 
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}
	
	/**
	 * Returns the height of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getHeight()
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(width,height);
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();
	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * Changing to fullscreen will use the resolution of the application at startup.
	 * It will NOT use the dimension settings of this canvas (which are for window
	 * display only).
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param value		Whether this canvas should change to fullscreen.
	 */
	public void setFullscreen(boolean value) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (value) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}
	
	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	 public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Returns the current color blending state for this canvas.
	 *
	 * Textures draw to this canvas will be composited according
	 * to the rules of this blend state.
	 *
	 * @return the current color blending state for this canvas
	 */
	public BlendState getBlendState() {
		return blend;
	}
	
	/**
	 * Sets the color blending state for this canvas.
	 *
	 * Any texture draw subsequent to this call will use the rules of this blend 
	 * state to composite with other textures.  Unlike the other setters, if it is 
	 * perfectly safe to use this setter while  drawing is active (e.g. in-between 
	 * a begin-end pair).  
	 *
	 * @param state the color blending rule
	 */
	public void setBlendState(BlendState state) {
		if (state == blend) {
			return;
		}
		switch (state) {
		case NO_PREMULT:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA_BLEND:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
			break;
		case OPAQUE:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
			break;
		}
		blend = state;
	}

	/**
	 * Start and active drawing sequence with the identity transform.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void begin() {
    	spriteBatch.begin();
    	active = true;
    }

	/**
	 * Start and active drawing sequence with the given transform.
	 *
	 * All textures drawn will have the given transform applied before any
	 * any other subsequent transforms.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param transform The drawing transform.
	 */
    public void begin(Affine2 transform) {
    	global.set(transform);
    	spriteBatch.begin();
    }

	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
    public void end() {
    	spriteBatch.end();
    	active = false;
    }

	/**
	 * Draws the texture at the given position.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(image,Color.WHITE,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(image,tint,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the screen location
	 * @param y 	The y-coordinate of the screen location
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, 
					float x, float y, float angle, float sx, float sy) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
	}

	/**
	 * Draws the tinted texture with the given transformation
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformation is applied BEFORE after the global transform (@see begin(Affine2)).
	 * As a result, the specified texture origin will be applied to all transforms
	 * (both the local and global).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param trans The coordinate space transform
	 */
	public void draw(Texture image, Color tint, Affine2 trans) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(holder,tint,trans);
	}

	/**
	 * Draws the texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param region	The texture to draw
	 * @param x 		The x-coordinate of the bottom left corner
	 * @param y 		The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method		
		draw(region,Color.WHITE,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param region	The texture to draw
	 * @param tint  	The color tint
	 * @param x 		The x-coordinate of the bottom left corner
	 * @param y 		The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		draw(region,tint,0,0,x,y,0,1.0f,1.0f);
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) with the given transformations
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param region	The texture to draw
	 * @param tint  	The color tint
	 * @param ox 		The x-coordinate of texture origin (in pixels)
	 * @param oy 		The y-coordinate of texture origin (in pixels)
	 * @param x 		The x-coordinate of the texture origin
	 * @param y 		The y-coordinate of the texture origin
	 * @param angle 	The rotation angle (in degrees) about the origin.
	 * @param sx 		The x-axis scaling factor
	 * @param sy 		The y-axis scaling factor
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		//  THIS IS THE MASTER DRAW METHOD
		// This is the method that you should alter to implement clipping.
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		computeTransform(ox,oy,x,y,angle,sx,sy);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);

		// 4 bounds - left, right, top, bottom
		// if bound exceeded, computeTransform at location on other side
		// x - ox < 0 (move to this.width + x), x + ox > this.width (move to - (width - x))
		// y - oy < 0 (move to this.height + y), y + oy > this.height (move to - (height - y))
//		float[] bounds = {x, x, y, y};
//		float[] borders = {0, this.width, 0, this.height};
//		float[] offsets = {-ox, +ox, -oy, +oy};
//		float[] xpos = {this.width + x, -this.width + x, x, x};
//		float[] ypos = {y, y, this.height + y, -this.height + y};
//		for (int i = 0; i < 4; i++) {
//			if (bounds[i] + offsets[i] > borders[i]) {
//				computeTransform(ox, oy, xpos[i], ypos[i], angle, sx, sy);
//				spriteBatch.setColor(tint);
//				spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
//			}
//		}

		// could use just one operator (<), and change the value to a negative
		int bound_conds_met = 0;
		float xpos = 0;
		float ypos = 0;
		if (x - ox < 0){
			bound_conds_met += 1;
			xpos = this.width + x;
			computeTransform(ox, oy, xpos, y, angle, sx, sy);
			spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
		}
		if (x + ox > this.width){
			bound_conds_met += 1;
			xpos = -this.width + x;
			computeTransform(ox, oy, xpos, y, angle, sx, sy);
			spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
		}
		if (y - oy < 0){
			bound_conds_met += 1;
			ypos = this.height + y;
			computeTransform(ox, oy, x, ypos, angle, sx, sy);
			spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
		}
		if (y + oy > this.height){
			bound_conds_met += 1;
			ypos = -this.height + y;
			computeTransform(ox, oy, x, ypos, angle, sx, sy);
			spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
		}
		// Double edge case
		if (bound_conds_met == 2) {
			computeTransform(ox, oy, xpos, ypos, angle, sx, sy);
			spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
		}
	}

	/**
	 * Draws the tinted texture region (filmstrip) with the given transformation
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformation is applied BEFORE after the global transform (@see begin(Affine2)).
	 * As a result, the specified texture origin will be applied to all transforms
	 * (both the local and global).
	 *
	 * @param region	The texture to draw
	 * @param tint  	The color tint
	 * @param trans 	The coordinate space transform
	 */
	public void draw(TextureRegion region, Color tint, Affine2 trans) {
		// THIS METHOD SHOULD ONLY BE USED BY THE LOADING SCREEN
		// It is unattached to the master draw method
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		local.set(global);
		local.mul(trans);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
	}
	
	/**
	 * Compute the affine transform (and store it in local) for this image.
	 * 
	 * This helper is meant to simplify all of the math in the above draw method
	 * so that you do not need to worry about it when working on Exercise 4.
	 *
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */
	private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
		local.set(global);
		// Post multiplication means we read this in reverse order
		local.translate(x,y);
		local.rotate(angle);
		local.scale(sx,sy);
		local.translate(-ox,-oy);
	}

	/**
     * Draw an unscaled overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * @param image Texture to draw as an overlay
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
    public void drawOverlay(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,x,y);
    }
    
	/**
     * Draw an unscaled overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
     * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void drawOverlay(Texture image, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, x, y);
    }

	/**
     * Draw an stretched overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
     * @param image Texture to draw as an overlay
	 * @param fill	Whether to stretch the image to fill the screen
	 */
    public void drawOverlay(Texture image, boolean fill) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,fill);
    }
    
	/**
     * Draw an stretched overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
     *
     * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param fill	Whether to stretch the image to fill the screen
	 */
	public void drawOverlay(Texture image, Color tint, boolean fill) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		float w, h;
		if (fill) {
			w = getWidth();
			h = getHeight();
		} else {
			w = image.getWidth();
			h = image.getHeight();
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, 0, 0, w, h);
    }

	/**
	 * Draw an stretched overlay image tinted by the given color.
	 *
	 * An overlay image is one that is not scaled by the global transform
	 * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
	 * track the camera.
	 *
	 * The image will be drawn starting at the bottom right corner, and will
	 * be stretched to fill the whole screen if appropriate.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param fill	Whether to stretch the image to fill the screen
	 */

	public void drawOverlay(Texture image, Color tint, boolean fill, float ox, float oy,
							float x, float y, float angle) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		float w, h;
		if (fill) {
			w = getWidth();
			h = getHeight();
		} else {
			w = image.getWidth();
			h = image.getHeight();
		}
		spriteBatch.setColor(tint);
		// scale it so the corners don't show (so each dim is at least as large as Math.sqrt(width **2 + height **2)
		// rotate by angle amount
		float a_2_b_2 = (float)Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2));
		float sx = a_2_b_2 / w;
		float sy = a_2_b_2 / h;
		spriteBatch.draw(image, 0, 0, w / 2, h /2, w, h, sx, sy, angle, 0, 0, (int) w, (int) h, false, false);
	}

	/**
	 * Enumeration of supported BlendStates.
	 *
	 * For reasons of convenience, we do not allow user-defined blend functions.
	 * 99% of the time, we find that the following blend modes are sufficient
	 * (particularly with 2D games).
	 */
	public enum BlendState {
		/** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
		ALPHA_BLEND,
		/** Alpha blending on, assuming the colors have no pre-multipled alpha */
		NO_PREMULT,
		/** Color values are added together, causing a white-out effect */
		ADDITIVE,
		/** Color values are draw on top of one another with no transparency support */
		OPAQUE
	}	
}