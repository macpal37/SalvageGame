/*
 * AudioRecorderAdapter.java
 *
 * This is just an attempt to remove some code from the way too long GDXAudio.
 *
 * @author Walker M. White
 * @date   04/15/20
 */
package com.xstudios.backend.audio;

import com.badlogic.gdx.audio.AudioRecorder;

/**
 * This class provides an empty implementation for {@link AudioRecorder}.
 */
public class AudioRecorderAdapter implements AudioRecorder {
    /** 
     * Reads in numSamples samples into the array samples starting at offset. 
     * 
     * Remember that audio samples are not the same as audio frames.  If the 
     * recorder is in stereo you have to multiply numSamples by 2.
     * 
     * @param samples the array to write the samples to
     * @param offset the offset into the array
     * @param numSamples the number of samples to be read 
     */
    @Override
    public void read (short[] samples, int offset, int numSamples) {
    }

    /**
     * Disposes this audio recorder, releasing all resources.
     */
    @Override
    public void dispose () {
    }
}
