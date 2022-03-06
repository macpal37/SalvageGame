/*
 * AudioDeviceAdapter.java
 *
 * This is just an attempt to remove some code from the way too long GDXAudio.
 *
 * @author Walker M. White
 * @date   04/15/20
 */
package com.xstudios.backend.audio;

import com.badlogic.gdx.audio.AudioDevice;

/**
 * This class provides an empty implementation for {@link AudioDevice}.
 */
public class AudioDeviceAdapter implements AudioDevice {
    /** We at least track the mono state */
    protected boolean isMono;
    
    /**
     * Creates an empty audio device
     *
     * @param mono    Whether the device is mono or stereo
     */
    public AudioDeviceAdapter(boolean mono){
        isMono = mono;
    }
    
    /** 
     * Writes the array of 16-bit signed PCM samples to the audio device.
     *
     * Stereo data should have its samples interleaved. This method blocks until 
     * all samples have been processed.
     * 
     * @param samples       The samples.
     * @param offset        The offset into the samples array
     * @param numSamples    The number of samples to write to the device 
     */
    @Override
    public void writeSamples (float[] samples, int offset, int numSamples) {
    }
    
    /** 
     * Writes the array of float values PCM samples to the audio device.
     *
     * Stereo data should have its samples interleaved. This method blocks until 
     * all samples have been processed.
     * 
     * @param samples       The samples.
     * @param offset        The offset into the samples array
     * @param numSamples    The number of samples to write to the device 
     */
    @Override
    public void writeSamples (short[] samples, int offset, int numSamples) {
    }
    
    /** 
     * Sets the volume in the range [0,1]
     *
     * @param volume    The device volume 
     */
    @Override
    public void setVolume (float volume) {
    }

    /** 
     * Returns whether this device is in mono or stereo mode.
     *
     * @return whether this device is in mono or stereo mode. 
     */
    @Override
    public boolean isMono () {
        return isMono;
    }

    /**
     * Returns the device latency in number of samples
     *
     * This is the number of samples that must be processed before the currently
     * written batch can be processed.  It exists because an audio devices does
     * a lot of internal buffering.
     *
     * @return the device latency in number of samples
     */
    @Override
    public int getLatency () {
        return 0;
    }

    /**
     * Disposes this audio device, releasing all resources.
     */
    @Override
    public void dispose () {
    }

}
