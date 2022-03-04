/*
 * AudioStream.java
 *
 * This interface exposes the underlying audio decoder streams (WAV, MP3, OGG) to the
 * user. This is not possible on all platforms, but we have exposed this on desktop
 * platforms to improve audio options for the students.
 *
 * @author Walker M. White
 * @date   4/15/20
 */
package com.xstudios.salvage.audio;

import com.badlogic.gdx.audio.AudioDevice;

/**
 * This class represents a streaming decoder for an audio asset.
 *
 * This class is used to read data from a sound asset, without necessarily playing. One
 * use case is to provide input to a {@link AudioDevice} object.  Another is to append
 * additional music streams to a {@link MusicBuffer} object.
 *
 * Decoder streams are read-forward. Each time you read data from them, they advance the
 * {@link #getByteOffset} and {@link #getSampleOffset}. To start reading data from the
 * beginning, you must call the {@link #reset()} method.  While there is some limited
 * seek functionality, this should be done in conjunction with a read.
 */
public interface AudioStream {
    
    /** 
     * Returns the {@link AudioSource} that generated this stream.
     *
     * @return the {@link AudioSource} that generated this stream.
     */
     public AudioSource getSource();
    
    /**
     * Returns the total number of bytes in this stream.
     *
     * This value is different from {@link #getSampleSize} in that the number of
     * bytes per audio frame is platform dependent. You should avoid using this
     * value unless you know what you are doing.
     *
     * @return the total number of bytes in this stream.
     */
    public long getByteSize();
    
    /**
     * Returns the current byte position in this stream.
     *
     * This value is moved forward anytime a {@link #read} method is called. It
     * is different from {@link #getSampleOffset} in that the number of bytes per
     * audio frame is platform dependent. You should avoid using this value 
     * unless you know what you are doing.
     *
     * @return the total number of bytes in this stream.
     */
    public long getByteOffset();
    
    /**
     * Returns the total number of audio samples in this stream.
     *
     * This value is different from {@link #getByteSize} in that it is a 
     * platform-independent method of measuring the length of a stream.
     * Keep in mind that non-mono streams will interleave audio streams
     * across each of the channels.
     *
     * @return the total number of audio samples in this stream.
     */
    public long getSampleSize();
    
    /**
     * Returns the current audio sample in this stream.
     *
     * This value is moved forward anytime a {@link #read} method is called. 
     * It is different from the value {@link #getByteSize} in that it is a
     * platform-independent method of measuring the current position in the
     * audio stream. Keep in mind that non-mono streams will interleave audio 
     * streams across each of the channels.
     *
     * @return the current audio samples in this stream.
     */
    public long getSampleOffset();
    
    /**
     * Reads the next page of bytes into the given buffer.
     *
     * This method will attempt to read up to size of buffer bytes from the
     * stream.  However, because of limitations with audio decoders, the page
     * size may not align with the size of this buffer.  Therefore, you should
     * always be prepared for less data to be read.  
     *
     * This method differs from the other {@link #read(short[])} methods in
     * that it reads raw bytes.  These a platform specific implementation of
     * the audio stream. You should avoid using this method unless you know 
     * what you are doing.
     *
     * @param buffer    The buffer store the audio data
     *
     * @return the number of bytes read
     */
    public int read(byte[] buffer);
    
    /**
     * Reads the next page of audio samples into the given buffer.
     *
     * This method will attempt to read up to size of buffer short audio samples
     * from the stream. This method differs from {@link #read(byte[])} in that
     * it access the audio data in a platform-independent way.  The audio sample
     * values will be between Short.MIN_VALUE and Short.MAX_VALUE.
     *
     * For a non-mono audio source, this buffer will interleave audio samples
     * channel by channel. The length of the provided buffer should have the
     * same parity as the number of channels.
     *
     * Because of limitations with audio decoders, the page size may not align 
     * with the size of this buffer. Therefore, you should always be prepared 
     * for less data to be read.
     *
     * @param buffer    The buffer store the audio data
     *
     * @return the number of audio samples read
     */
    public int read(short[] buffer);
    
    /**
     * Reads the next page of audio samples into the given buffer.
     *
     * This method will attempt to read up to size of buffer short audio samples
     * from the stream. This method differs from {@link #read(byte[])} in that
     * it access the audio data in a platform-independent way. The audio sample
     * values will be between -1 and 1.
     *
     * For a non-mono audio source, this buffer will interleave audio samples
     * channel by channel. The length of the provided buffer should have the
     * same parity as the number of channels.
     *
     * Because of limitations with audio decoders, the page size may not align 
     * with the size of this buffer.  Therefore, you should always be prepared 
     * for less data to be read.
     *
     * @param buffer    The buffer store the audio data
     *
     * @return the number of audio samples read
     */
    public int read(float[] buffer);
    
    /**
     * Seeks to the given byte position, reading the results into the provided buffer
     *
     * Provided the position sought is found, the data at that position will be
     * written to the very beginning of the buffer.  The value returned is the number
     * of bytes written to buffer (including this position). So a value of 0 means
     * that the seek failed.
     * 
     * Because of how stream decoding works, it is impossible to move to a spot in
     * the audio and stop. Reading has to occur at well-defined pages, and the position
     * may be in the middle of a page. When we seek to a position, we have to be
     * prepared to read the remainder of a page.
     *
     * This method differs from the other {@link #seek(long,short[])} methods in
     * that it reads raw bytes.  These a platform specific implementation of
     * the audio stream.   You should avoid using this method unless you know 
     * what you are doing.
     *
     * @param pos       The position to seek to
     * @param buffer    The buffer store the audio data
     *
     * @return the number of bytes read
     */
    public int seek(long pos, byte[] buffer);
    
    /**
     * Seeks to the given audio sample, reading the results into the provided buffer
     *
     * Provided the position sought is found, the data at that position will be
     * written to the very beginning of the buffer.  The value returned is the number
     * of audio samples written to buffer (including this position). So a value of 0 
     * means that the seek failed. The audio sample values will be between 
     * Short.MIN_VALUE and Short.MAX_VALUE.
     * 
     * Because of how stream decoding works, it is impossible to move to a spot in
     * the audio and stop. Reading has to occur at well-defined pages, and the position
     * may be in the middle of a page. When we seek to a position, we have to be
     * prepared to read the remainder of a page.
     *
     * This method differs from the other {@link #seek(long,byte[])} methods in that
     * it is platform independent.  However, for non-mono audio the sample position
     * should always correspond to the first audio sample in an audio frame. Picking
     * a position for an alternate channel will affect interleaving when you wish to
     * playback the data.
     *
     * @param pos       The position to seek to
     * @param buffer    The buffer store the audio data
     *
     * @return the number of audio samples read
     */
    public int seek(long pos, short[] buffer);

    /**
     * Seeks to the given audio sample, reading the results into the provided buffer
     *
     * Provided the position sought is found, the data at that position will be
     * written to the very beginning of the buffer.  The value returned is the number
     * of audio samples written to buffer (including this position). So a value of 0 
     * means that the seek failed. The audio sample values will be between -1 and 1.
     * 
     * Because of how stream decoding works, it is impossible to move to a spot in
     * the audio and stop. Reading has to occur at well-defined pages, and the position
     * may be in the middle of a page.  So when we seek to a position, we have to be
     * prepared to read the remainder of a page.
     *
     * This method differs from the other {@link #seek(long,byte[])} methods in that
     * it is platform independent.  However, for non-mono audio the sample position
     * should always correspond to the first audio sample in an audio frame. Picking
     * a position for an alternate channel will affect interleaving when you wish to
     * playback the data.
     *
     * @param pos       The position to seek to
     * @param buffer    The buffer store the audio data
     *
     * @return the number of audio samples read
     */
    public int seek(long pos, float[] buffer);

    /**
     * Resets this audio stream rewinding it to the beginning.
     *
     * This differs from {@link #loop} in that it assumes the asset will not need
     * to be streamed immediately, so it is safe to delete internal memory.
     */
    public void reset();

    /**
     * Resets this audio stream rewinding it to the beginning.
     *
     * This differs from {@link #reset} in that it assumes the asset will need
     * to be streamed immediately, so it preserves any internal memory.
     */
    public void loop();

}