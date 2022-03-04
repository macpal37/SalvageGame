/*
 * AudioSource.java
 *
 * This interface is decouples the audio asset from the playback interface (e.g. sound
 * or music).  I understand why LibGDX did not do this, but we have decoupled this on
 * desktop platforms to improve audio options for the students.
 *
 * @author Walker M. White
 * @date   4/15/20
 */
package com.xstudios.salvage.audio;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 * This class is an audio asset that is not explicitly attached to an audio engine.
 *
 * Sometimes we just want the data for a sound or music asset without having to 
 * explicitly play it.  This is especially true when we are appending tracks to
 * a {@link MusicBuffer}.  This class allows us to decouple this relationship.
 *
 * This class also provides the user with a lot of "header" information that is 
 * not available in the basic LibGDX asset classes.  This includes information
 * like the number of channels or the sample rate.  This gives users more programmatic
 * control over their game audio.
 * 
 * All sample instances are created via {@link AudioEngine#newSource}. When you 
 * are done with using the sample instance you have to dispose it via the 
 * {@link #dispose()} method.
 */
public interface AudioSource extends Disposable {
    /**
     * Returns the file that generated this source.
     *
     * The currently supported formats are WAV, MP3 and OGG.
     *
     * @return the file that generated this source.
     */
    public FileHandle getFile();

    /**
     * Returns the number of audio channels in this source.
     *
     * A mono sound will have 1 channel, while a stereo sound will have two 
     * channels.  While OGG and WAV support more than 2 channels, MP3 does
     * not.  Furthermore, LibGDX cannot playback any source with more than
     * 2 channels in it.
     *
     * @return the number of audio channels in this source.
     */
    public int getChannels();

    /**
     * Returns the sample rate of this audio asset
     *
     * The sample rate is the number of samples per second of audio.  CD
     * quality sound has a sample rate of 44100.  Many games use a sample
     * rate of 48000.
     *
     * @return the sample rate of this audio asset
     */
    public int getSampleRate();

    /**
     * Returns the duration of this audio asset in seconds
     *
     * This value may be a slight approximation, given the variability within
     * the different formats (WAV, MP3, OGG).
     *
     * @return  the duration of this audio asset in seconds
     */
    public float getDuration();
    
    /**
     * Returns a new {@link AudioStream} to stream this asset.
     *
     * This is the interface used by {@link MusicBuffer} to stream music from
     * the file.  A sample may have multiple independent streams.  The will
     * all be distinct from one another.
     *
     * @return a new {@link AudioStream} to stream this asset.
     */
    public AudioStream getStream();

    /**
     * Returns a byte buffer encapsulating the audio asset
     *
     * The byte buffer will be the complete audio asset, fully loaded into
     * memory. While there is no upper limit on the audio file size for this
     * method, you should avoid using this method for any audio asset greater 
     * than 1 MB.
     *
     * The byte representation of an audio source is platform dependent. You
     * should avoid using this buffer directly unless you know what you are 
     * doing.  If you need to read audio samples, get an {@link AudioStream}
     * instead.
     *
     * @return a byte buffer encapsulating the audio asset
     */
    public ByteBuffer getData();

}
