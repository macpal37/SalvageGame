/*
 * MusicBuffer.java
 *
 * This interface exposes more of the OpenAL interface to the user to improve audio 
 * options.  In particular, it is expands the notion of a music asset to a music queue
 * so that students can gaplessly string together music assets.  A true music buffer
 * would have crossfade support (like we do in CUGL). However, Java is not the right
 * place to implement low-level mixer options like this.
 *
 * @author Walker M. White
 * @date   4/15/20
 */
package com.xstudios.salvage.audio;

import com.badlogic.gdx.audio.Music;

/** 
 * This interface represents a continuous audio stream. 
 *
 * As a subinterface of {@link Music}, this supports pausing, resuming and so on. In
 * addition, it supports a simple music queue that allows you to programmatically
 * string audio assets together for gapless playback. 
 *
 * When the buffer has only one source, it behaves the same as a {@link Music} object.
 * However, when there is more than one source, both the methods {@link #setLooping} and 
 * {@link #setPosition(float)} behave differently. Setting the position sets the 
 * accumulated position across all sources. Looping may either be local (it loops the
 * current source, preventing queue advancement) or global (the queue loops when it
 * reaches the end of all sources) according the value {@link #setLoopBehavior(boolean)}.
 * 
 * Music buffer instances are created via {@link AudioEngine#newMusic}. When you 
 * are done with using the music buffer instance you have to dispose it via the 
 * {@link #dispose()} method.
 *
 * This interface is not even remotely thread-safe. LibGDX requires that all audio
 * interfaces be interacted with in the main application thread (for much the same
 * reason that OpenGL requires this).
 */
public interface MusicBuffer extends Music {

    /**
     * The interface for a callback invoked as the music queue is processed.
     *
     * This callback is more sophisticated than {@link Music.OnCompletionListener}
     * in that it is not limited to the end of the music stream.  It also notifies
     * us when we transition from one track to another.
     *
     * All of these methods are conservative.  When streaming audio there is a delay
     * between when the audio is queued to the audio card and when it is processed.
     * All of these callback methods are only called when the various events are 
     * processed, confirming that they did indeed happen.  For a less conservative
     * approach, you should get the current position in the queue.
     */
    public interface OnTransitionListener {
        /** 
         * Called when a single audio source loops back around.
         *
         * This callback is conservative. Is called when the loop around has been
         * processed (and has likely started playing already). For a less conservative
         * approach, you should get the current position in the queue.
         *
         * @param buffer    The buffer that reached the end of the stream
         * @param source    The source that looped around
         */
        public void onLoopback(MusicBuffer buffer, AudioSource source);
        /** 
         * Called when queue transitions from one source to another.
         *
         * This callback is conservative.  Is called when the second source has
         * been processed (and has likely started playing already). For a less 
         * conservative approach, you should get the current position in the queue.
         *
         * @param buffer    The buffer that reached the end of the stream
         * @param source1   The previous source in the transition
         * @param source2   The current source in the transition
         */
        public void onTransition(MusicBuffer buffer, AudioSource source1, AudioSource source2);
        /** 
         * Called when the end of a music stream is reached during playback.
         *
         * This callback is conservative.  Is called when the source has completely
         * finished playing, not when it has been queued up (this distinction matters
         * for gapless playback). For a less conservative approach, you should get the 
         * current position in the queue.
         *
         * @param buffer    The buffer that reached the end of the stream
         * @param source    The last source processed by this stream
         */
        public void onCompletion(MusicBuffer buffer, AudioSource source);
    }
    
    /** 
     * Registers a callback invoked as the music queue is processed.
     *
     * @param listener  The callback that will be run. 
     */
    public void setOnTransitionListener (OnTransitionListener listener);
    
    // #mark -
    // #mark Fixed Attributes
    /**
     * Returns true if this is a mono music stream.
     *
     * LibGDX only supports mono and stereo audio streams. It does not support
     * complex multi-channel streams (such as 7.1 surround).
     *
     * @return true if this is a mono music stream.
     */
    public boolean isMono();
    
    /**
     * Returns the number of audio samples (per channel) per second.
     *
     * All sources added to a music buffer must have the same sample rate (unless
     * you do not care about your pitch, that is.
     *
     * @return the number of audio samples (per channel) per second.
     */
    public int getSampleRate();
    
    /**
     * Returns the current duration of this music stream in seconds.
     *
     * The duration is the sum of the durations of all of the sources
     * in the music buffer.
     */
    public float getDuration();

    /** 
     * Sets the pitch of the music stream
     *
     * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
     * >1 is faster, and <1 is slower.
     *
     * @param pitch The pitch of the  music stream
     */
    public void setPitch(float pitch);
    
    /** 
     * Returns the pitch of the music stream
     *
     * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
     * >1 is faster, and <1 is slower.
     *
     * @return the pitch of the  music stream
     */
    public float getPitch();

    /** 
     * Sets the pan of the given music stream
     *
     * The pan is a value -1 to 1.  The value 0 is the default center position. -1
     * is full left and 1 is full right.
     *
     * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
     */
    public void setPan(float pan);

    /** 
     * Returns the pan value of the music stream
     *
     * The pan is a value -1 to 1.  The value 0 is the default center position. -1
     * is full left and 1 is full right.
     *
     * @return the pan value of the music stream
     */
    public float getPan();
    
    /** 
     * Sets the loop behavior of this music buffer.
     *
     * Loop behavior may be local or global. If the stream has only one source,
     * they are the same.  Local looping loops in place at the current source,
     * preventing advancement in the queue. Global looping loops the entire 
     * stream when it reaches the end.
     *
     * If loop behavior is local, any method that causes the stream to leave the
     * current source (such as {@link #advanceSource()}) will clear the looping
     * setting.
     *
     * @param local Whether the loop behavior is local
     */
     public void setLoopBehavior(boolean local);

    /** 
     * Returns the loop behavior of this music buffer.
     *
     * Loop behavior may be local or global. If the stream has only one source,
     * they are the same.  Local looping loops in place at the current source,
     * preventing advancement in the queue. Global looping loops the entire 
     * stream when it reaches the end.
     *
     * If loop behavior is local, any method that causes the stream to leave the
     * current source (such as {@link #advanceSource()}) will clear the looping
     * setting.
     *
     * @return the loop behavior of this music buffer.
     */
     public boolean getLoopBehavior();
     
     
    // #mark Queue Management
    /**
     * Returns the number of audio sources in this buffer
     *
     * @return the number of audio sources in this buffer
     */
    public int getNumberOfSources();
    
    /**
     * Returns the current audio sources being played.
     *
     * The buffer does not have to be playing to return a value.  If it does
     * return a source, this is the source that will first be played when 
     * playback resumes.
     *
     * @return the current audio sources being played.
     */
    public AudioSource getCurrent();

    /**
     * Returns the audio source at the given queue position.
     *
     * If the position is invalid, this method returns null.
     *
     * @return the audio source at the given queue position.
     */
    public AudioSource getSource(int pos);
    
    /**
     * Sets the source for the given position.
     *
     * It is safe to call this method while the buffer is playing.  If this
     * method replaces the source currently being played, it will start to
     * play the new source from the beginning.
     *
     * @param pos       The position in the source queue
     * @param source    The source to place
     */
    public void setSource(int pos, AudioSource source);

    /**
     * Adds the given source as the source queue.
     *
     * It is safe to call this method while the buffer is playing, though
     * gapless playback is only guaranteed if it is added with enough
     * lead time.
     *
     * @param source    The source to add
     */
    public void addSource(AudioSource source);

    /**
     * Insert the source at the given position.
     *
     * It is safe to call this method while the buffer is playing, though
     * it may be skipped over if the current source position is previous
     * the one being inserted.
     *
     * @param pos       The position in the source queue
     * @param source    The source to insert
     */
    public void insertSource(int pos, AudioSource source);

    /**
     * Removes the source at the given position.
     *
     * It is safe to call this method while the buffer is playing. If this
     * source is the current one playing, the buffer will skip to the next
     * source in the queue.  In addition, if there is a transition listener
     * it will update that transition as well.
     *
     * This method will return null if the position is invalid
     *
     * @param pos   The position in the source queue
     *
     * @return the source removed from the given position
     */
    public AudioSource removeSource(int pos);

    /**
     * Clears the music buffer, removing all sources.
     *
     * It is safe to call them method while the buffer is playing.
     * It will immediately halt all playback.
     */
    public void clearSources();

    /**
     * Advances the music buffer to the next audio source in the queue
     *
     * The position will be set to the start of the next audio source.
     * If there is no audio source, it will stop playback. If there is
     * an attached transition listener, it will be notified of the
     * transition.
     */
    public void advanceSource();
    
    /**
     * Advances the music buffer the given number of steps in the queue
     *
     * The position will be set to the start the given number of steps away 
     * in the queue. Calling this with steps==0 will be the same as reseting 
     * the current audio source. This method may not be used to go backwards 
     * in the queue.
     *
     * If there is no audio source at the given position, it will stop 
     * playback. If there is an attached transition listener, it will be 
     * notified of the transition.
     *
     * @param steps The number of steps to advance forward.
     *
     * @throws IllegalArgumentException if steps < 0
     */
    public void advanceSource(int steps);
    
    /**
     * Jumps to the given source in the music buffer queue
     *
     * If there is no audio source at the given position, it will stop 
     * playback. If there is an attached transition listener, it will be 
     * notified of the transition.
     *
     * @param pos   The position in the source queue
     */
    public void jumpToSource(int pos);
    
    /**
     * Resets the music buffer to very beginning of the stream.
     *
     * The stream will return to the very beginning of the first source
     * in the buffer queue.
     */
    public void reset();

}
