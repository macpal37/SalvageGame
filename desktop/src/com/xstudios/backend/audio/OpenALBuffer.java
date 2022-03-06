/*
 * OpenALBuffer.java
 *
 * This interface allows us to process all OpenAL sources polymorphically.
 *
 * @author Walker M. White
 * @date   4/15/20
 */

package com.xstudios.backend.audio;

/**
 *  This interface represents a logical buffer attached to a source (or sources) in OpenAL.
 *
 *  This class is created to make OpenAL source assignment easier.
 */
public interface OpenALBuffer {
    /**
     * Stops all sources associated with this buffer.
     */
    public void stop();

    /**
     * Pauses all sources associated with this buffer.
     */
    public void pause();

    /**
     * Resumes all sources associated with this buffer.
     */
    public void resume();

    /**
     * Updates all OpenAL sources for this buffer
     */
    public void update();

    /**
     * Updates the given OpenAL source with the data from this buffer
     *
     * @param sourceId  The OpenAL source
     */
    public void update(int sourceId);

    /**
     * Returns whether this buffer can be evicted from is current OpenAL source
     *
     * This method is whenever we run out of sources.
     *
     * @return whether this buffer can be evicted from is current OpenAL source
     */
    public boolean evictable();

}
