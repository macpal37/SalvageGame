/*
 * WavSource.java
 *
 * This is an adaptation of the LibGDX file Wav.java by Nathan Sweet, adapted to our
 * AudioSource interface.  This interface improves seek behavior and caches important
 * query information.
 *
 * @author Walker M. White
 * @date   4/15/20
 */
package com.xstudios.backend.audio;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.xstudios.salvage.audio.AudioSource;
import com.xstudios.salvage.audio.AudioStream;
import com.xstudios.salvage.audio.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is an implementation of {@link AudioSource} for WAV files.
 *
 * OGG files may be streamed or loaded into memory. While the WAV format supports 
 * more than 2 channels, LibGDX only supports mono and stereo.
 */
public class WavSource implements AudioSource {
    /** The source file */
    protected FileHandle source;
    /** The number of channels (1 for mono, 2 for stereo) */
    protected int channels;
    /** The number of audio samples (per channel) per second */
    protected int sampleRate;
    /** The duration of the MP3 asset in seconds */
    protected float duration;
    /** The length of the MP3 asset in bytes */
    protected long byteSize;

    /** An initial WAV input stream for header data */
    protected WavInputStream input;

    /** 
     * Creates an WAV source from the given file.
     *
     * @param file  The WAV file
     *
     * @throws GdxRuntimeException if the asset could not be loaded
     */
    public WavSource(FileHandle file) {
        source = file;
        input = new WavInputStream( file );
        channels = input.channels;
        sampleRate = input.sampleRate;
        byteSize = input.dataRemaining;
        long samples = byteSize / (2 * channels);
        duration = samples / (float)sampleRate;
    }

    /**
     * Deletes this MP3 source, disposing of all resources.
     */
    @Override
    public void dispose() {
        input = null;
        source = null;
        channels = 0;
        sampleRate = 0;
        duration = 0;
        byteSize = 0;
    }
    
    /**
     * Returns the file that generated this source.
     *
     * The currently supported formats are WAV, MP3 and OGG.
     *
     * @return the file that generated this source.
     */
    @Override
    public FileHandle getFile() {
        return source;
    }
    
    /**
     * Returns the number of audio channels in this source.
     *
     * A mono sound will have 1 channel, while a stereo sound will have two 
     * channels.  While OGG and WAV support more than 2 channels, MP3 does
     * not.  Furthermore, LibGDX cannot playback any sample with more than
     * 2 channels in it.
     *
     * @return the number of audio channels in this source.
     */
    @Override
    public int getChannels() {
        return channels;
    }

    /**
     * Returns the sample rate of this audio asset
     *
     * The sample rate is the number of samples per second of audio.  CD
     * quality sound has a sample rate of 44100.  Many games use a sample
     * rate of 48000.
     *
     * @return the sample rate of this audio asset
     */
    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Returns the duration of this audio asset in seconds
     *
     * This value may be a slight approximation, given the variability within
     * the different formats (WAV, MP3, OGG).
     *
     * @return  the duration of this audio asset in seconds
     */
    @Override
    public float getDuration() {
        return duration;
    }

    /**
     * Returns a new {@link AudioStream} to stream this asset.
     *
     * This is the interface used by {@link MusicBuffer} to stream music from
     * the file.  A sample may have multiple independent streams.  The will
     * all be distinct from one another.
     *
     * @return a new {@link AudioStream} to stream this asset.
     */
    @Override
    public AudioStream getStream() {
        return new Stream();
    }
    
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
    @Override
    public ByteBuffer getData() {
        ByteBuffer result = null;
        try {
            if (input == null) {
                input = new WavInputStream( source );
            }
            result = ByteBuffer.allocateDirect( (int)byteSize );
            result.order( ByteOrder.nativeOrder() );
            StreamUtils.copyStream( input, result, (int)byteSize );
        } catch (IOException e) {
            result = null;
        } finally {
            StreamUtils.closeQuietly( input );
        }
        return result;
    }

    /**
     * Returns a string representation of this audio source (for debugging)
     * 
     * @return a string representation of this audio source (for debugging)
     */
    @Override
    public String toString() {
        return "'"+getFile().toString()+" ["+String.format("@%x", hashCode())+"]'";
    }

    // #mark -
    /**
     * This class is an {@link AudioStream} for WAV files.
     *
     * This stream is really chunky.  If we cared enough, we would have an internal
     * buffer for finer grained reads.
     */
    public class Stream implements AudioStream {
        /** The current WAV input stream */
        private WavInputStream input;
        /** The current byte position in the stream */
        private long byteOffs;
        /** A byte array for grabbing data for sample queries */
        private byte[] tempBytes;
        
        /**
         * Creates a new WAV stream
         *
         * @throw GdxRuntimeException if the stream could not be initialized
         */
        public Stream() {
            input = new WavInputStream( source );
            byteOffs = 0;
        }
        
        /** 
         * Returns the {@link AudioSource} that generated this stream.
         *
         * @return the {@link AudioSource} that generated this stream.
         */
         public AudioSource getSource() {
            return WavSource.this;
         }
        
        /**
         * Returns the total number of bytes in this stream.
         *
         * This value is different from {@link #getSampleSize} in that the number of
         * bytes per audio frame is platform dependent. You should avoid using this
         * value unless you know what you are doing.
         *
         * @return the total number of bytes in this stream.
         */
        @Override
        public long getByteSize() {
            return byteSize;
        }
        
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
        @Override
        public long getByteOffset() {
            return byteOffs;
        }
        
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
        @Override
        public long getSampleSize() {
            return byteSize/2;
        }
        
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
        @Override
        public long getSampleOffset() {
            return byteOffs/2;
        }
        
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
        @Override
        public int read(byte[] buffer) {
            if (input == null) {
                input = new WavInputStream( source );
            }
            try {
                int chunk = input.read(buffer);
                byteOffs += chunk;
                return chunk;
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error reading WAV file: " + source, ex);
            }
        }
        
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
        @Override
        public int read(short[] buffer) {
            if (tempBytes == null || tempBytes.length < 2*buffer.length) {
                tempBytes = new byte[2*buffer.length];
            }
            int length = read(tempBytes);
            for(int ii = 0; ii < length/2; ii++) {
                int lower = (int)(tempBytes[2*ii  ]) & 0xFF;
                int upper = (int)(tempBytes[2*ii+1]) & 0xFF;
                buffer[ii] = (short)((upper << 8) | lower);
            }
            return length/2;
        }
        
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
        public int read(float[] buffer) {
            if (tempBytes == null || tempBytes.length < 2*buffer.length) {
                tempBytes = new byte[2*buffer.length];
            }
            int length = read(tempBytes);
            for(int ii = 0; ii < length/2; ii++) {
                int lower = (int)(tempBytes[2*ii  ]) & 0xFF;
                int upper = (int)(tempBytes[2*ii+1]) & 0xFF;
                short value = (short)((upper << 8) | lower);
                buffer[ii] = value/32767.0f;
            }
            return length/2;
        }
        
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
         * @return the number of audio samples read
         */
        @Override
        public int seek(long pos, byte[] buffer) {
            if (pos <= 0 || buffer.length <= 1) {
                throw new IllegalArgumentException( "Illegal seek parameters" );
            }
            byte[] temp = new byte[buffer.length/2];
            
            if (pos < byteOffs) {
                StreamUtils.closeQuietly( input );
                input = new WavInputStream( source );
                byteOffs = 0;
            }
            
            try {
                int chunk = 1;
                while (chunk > 0 && byteOffs < pos) {
                    chunk = input.read(temp);
                    byteOffs += chunk;
                }
                
                if (byteOffs < pos) {
                    reset();
                    return 0;
                }
                
                int mark = (int)(byteOffs-pos);
                System.arraycopy( temp, mark, buffer,0,chunk-mark );
                mark = chunk-mark;
                chunk = input.read( temp );
                System.arraycopy( temp,0, buffer, mark, chunk );
                
                return chunk+mark;
            } catch (Exception e) {
                reset();
                return 0;
            }
        }
        
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
        public int seek(long pos, short[] buffer) {
            if (tempBytes == null || tempBytes.length < 2*buffer.length) {
                tempBytes = new byte[2*buffer.length];
            }
            int length = seek(pos, tempBytes);
            for(int ii = 0; ii < length/2; ii++) {
                int lower = (int)(tempBytes[2*ii  ]) & 0xFF;
                int upper = (int)(tempBytes[2*ii+1]) & 0xFF;
                buffer[ii] = (short)((upper << 8) | lower);
            }
            return length/2;
        }
        
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
        public int seek(long pos, float[] buffer) {
            if (tempBytes == null || tempBytes.length < 2*buffer.length) {
                tempBytes = new byte[2*buffer.length];
            }
            int length = seek(pos, tempBytes);
            for(int ii = 0; ii < length/2; ii++) {
                int lower = (int)(tempBytes[2*ii  ]) & 0xFF;
                int upper = (int)(tempBytes[2*ii+1]) & 0xFF;
                short value = (short)((upper << 8) | lower);
                buffer[ii] = value/32767.0f;
            }
            return length/2;
        }
        
        /**
         * Resets this audio stream rewinding it to the beginning.
         *
         * This differs from {@link #loop} in that it assumes the asset will not need
         * to be streamed immediately, so it is safe to delete internal memory.
         */
        @Override
        public void reset() {
            StreamUtils.closeQuietly(input);
            input = null;
            byteOffs = 0;
        }
        
        /**
         * Resets this audio stream rewinding it to the beginning.
         *
         * This differs from {@link #reset} in that it assumes the asset will need
         * to be streamed immediately, so it preserves any internal memory.
         */
        @Override
        public void loop() {
            StreamUtils.closeQuietly(input);
            input = null;
            byteOffs = 0;
        }
    }


    }
