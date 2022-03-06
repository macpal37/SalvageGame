/*
 * GDXApp.java
 *
 * This is a complete rewrite of OpenALAudio. It is an attempt to provide more features 
 * to the students who have been struggling with this awful audio engine for years. If 
 * you want a real audio engine with DSP graphs, cross-fade support, and 7.1+ surround 
 * sound, take the advanced class.
 *
 * @author Walker M. White
 * @data   4/16/20
 */
package com.xstudios.backend;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.audio.JavaSoundAudioRecorder;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudioDevice;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.xstudios.backend.audio.*;
import com.xstudios.salvage.audio.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * This class is an implementation of the {@link Audio} interface using OpenAL.
 *
 * This is what I refer to as a "Cuckoo class".  Ideally, this class would initialize
 * OpenAL itself.  However, it cannot do that.  The native OpenAL libraries are bound
 * (by gradle it seems) to {@link OpenALAudio}, and they are loaded dynamically when 
 * that class initializes the audio. The natives are NOT attached to {@link AL} 
 * (like you would assume) so it is impossible to load the OpenAL natives from our
 * external backend.  Even subclassing OpenALAudio changes the classpath in a way
 * that causes OpenAL initialization to fail.
 *
 * This is where the Cuckoo class comes in. It lets another class (in this case
 * {@link OpenALAudio}) load the native libraries and initialize them. It then steals
 * all resources from this class and kicks it out of the nest like a Cuckoo bird.
 *
 * This type of class is a horrendous breach of abstraction, but it is necessary when
 * resources are walled off in a brittle, private memory area that breaks under 
 * subclassing.  A similar problem happens with Box2d (in C++).  Box2d has a custom
 * allocator that works in global space, and breaks if there is ever a context 
 * switch between global memory regions.  This is why it is unsafe to use Box2d in
 * a DLL (it must be statically linked).
 *
 * This file is very large because we have taken three classes from the original OpenAL
 * implementation -- {@link OpenALSound}, {@link OpenALMusic}, and {@link OpenALAudioDevice}
 * -- and turned them into inner classes.  They always were sort-of inner classes, made
 * explicit by a reference to the parent.  But that implementation required a lot of
 * package restrictions that we do not want to have to deal with.  Despite the increase
 * in this file size, we find the design of this class to be a lot more manageable.
 */
public class GDXAudio implements AudioEngine {
    /** The buffer size of an audio device */
    private final int deviceBufferSize;
    /** The number of buffers in an audio device */
    private final int deviceBufferCount;
    
    /** Whether audio has been disabled */
    private boolean noDevice = false;
    
    /** The audio source classes for each file type */
    private ObjectMap<String, Class<?>> extensionToFormat = new ObjectMap<String, Class<?>>();
    
    /** The OpenAL sources generated for this engine */
    private IntArray allSources;
    /** Mapping from an audio source to an array index */
    private IntIntMap sourceToIndex = new IntIntMap();
    /** Mapping from an array index to an audio source */
    private IntIntMap indexToSource = new IntIntMap();

    /** The ring buffer of audio source managers */
    private OpenALBuffer[] buffers;
    /** The ring buffer pointer */
    private int recentIndex;
        
    /** Whether we have instituted a global pause */
    private boolean globalPause;

    /** The sources affected by a global pause */
    private boolean[] paused;
    
    /** A float buffer to query OpenAL */
    private FloatBuffer floatdata;
    
    /**
     * Creates an audio engine with the default settings.
     *
     * This engine will support 24 simultaneous sources (classic SoundBlaster style).  
     * It will have 512 byte buffers which are the minimum recommended size for any 
     * audio card.
     *
     * Simultaneous sources refers to any any source that can generate audio.  This
     * includes {@link Sound}, {@link Music}, and {@link AudioDevice}.
     */
    public GDXAudio () {
        this(24, 9, 512);
    }

    /**
     * Creates an audio engine with the given settings.
     *
     * Only the first setting applies directly for this audio engine.  The second
     * two settings are for the {@link AudioDevice} generated by the method
     * {@link #newAudioDevice}.
     *
     * Simultaneous sources refers to any any source that can generate audio.  This
     * includes {@link Sound}, {@link Music}, and {@link AudioDevice}.
     *
     * @param simultaneousSources    The number of simultaneous audio sources
     * @param deviceBufferCount        The number of buffers to create for an {@link AudioDevice}.
     * @param deviceBufferSize        The buffer size to allocate for an {@link AudioDevice}.
     */
    public GDXAudio (int simultaneousSources, int deviceBufferCount, int deviceBufferSize) {
        this.deviceBufferSize = deviceBufferSize;
        this.deviceBufferCount = deviceBufferCount;

        registerFormat("ogg", OggSource.class);
        registerFormat("wav", WavSource.class);
        registerFormat("mp3", Mp3Source.class);

        try {
            allSources = new IntArray( false, simultaneousSources );
            for (int ii = 0; ii < simultaneousSources; ii++) {
                int sourceId = AL10.alGenSources();
                int errorCode = AL10.alGetError();
                if (errorCode != AL10.AL_NO_ERROR) {
                    Gdx.app.error( "OpenAL", "Unable to allocated source: " + AL10.alGetString( errorCode ) );
                    ii = simultaneousSources;
                } else {
                    sourceToIndex.put( sourceId, allSources.size );
                    indexToSource.put( allSources.size, sourceId );
                    allSources.add( sourceId );
                }
            }

            buffers = new OpenALBuffer[simultaneousSources];
            recentIndex = simultaneousSources - 1;

            paused = new boolean[simultaneousSources];
            globalPause = false;
            floatdata = BufferUtils.createFloatBuffer( 4 );
        } catch (Exception e) {
            Gdx.app.log("OpenAL", "Unable to initialize secondary engine");
            noDevice = true;
            return;
        }
    }

    /**
     * Disposes this audio engine, releasing all resources.
     */
    public void dispose () {
        if (noDevice) {
            return;
        }

        for(int ii = 0; ii < buffers.length; ii++) {
            if (buffers[ii] != null) {
                buffers[ii].stop();
                buffers[ii] = null;
            }
        }

        for (int ii = 0, n = allSources.size; ii < n; ii++) {
            int sourceId = allSources.get(ii);
            int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_STOPPED) {
                AL10.alSourceStop(sourceId);
            }
            AL10.alDeleteSources(sourceId);
        }
        
        allSources.clear();
        sourceToIndex.clear();
        indexToSource.clear();

        // OpenAL clean-up is responsibility of primary engine
    }

    /**
     * Registers an {@link AudioSource} class with the given extension.
     *
     * This is used to support multiple audio formats.  Right now, we support WAV, OGG, and MP3.
     * Ideally, we would support FLAC too.
     *
     * @param extension The file extension
     * @param theClass  The {@link AudioSource} class for the extension
     */
    public void registerFormat(String extension, Class<?> theClass) {
        if (extension  == null) {
            throw new IllegalArgumentException("extension cannot be null.");
        }
        if (theClass == null) {
            throw new IllegalArgumentException("soundClass cannot be null.");
        }
        extensionToFormat.put(extension, theClass);
    }

    // #mark Factory Methods
    /**
     * Creates a new {#link AudioSource} from the given file.
     *
     * A sample is a music asset that is not explicitly associated with the audio engine.
     * You can read data directly and pass it to an {@link AudioDevice}. Alternatively,
     * you can queue the sample on to a {@link MusicBuffer} to support gapless
     * transitions in your music.
     *
     * The currently supported formats are WAV, MP3 and OGG.
     *
     * The audio source should be disposed if it is no longer used via the
     * {@link AudioSource#dispose()} method.
     *
     * @param file The sound asset
     *
     * @throws GdxRuntimeException if the asset could not be loaded
     * @return a new {#link Sample} from the given file.
     */
    @Override
    public AudioSource newSource (FileHandle file) {
        if (file == null) {
            throw new IllegalArgumentException( "File cannot be null." );
        } else if (!file.exists()) {
            throw new IllegalArgumentException( "File "+file+" does not exist." );
        }
        Class<?> format = extensionToFormat.get(file.extension().toLowerCase());
        if (format == null) throw new GdxRuntimeException("Unknown file extension for sound: " + file);
        try {
            return (AudioSource)format.getConstructor(new Class[] {FileHandle.class}).newInstance(file);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error creating " + format.getName() + " for file: " + file, ex);
        }
    }

    /**
     * Creates a new {@link SoundBuffer} which to play back audio effects.
     *
     * Sound buffers should be used for low latency effects such as gun shots or
     * explosions. The audio data is retrieved from the file specified and loaded
     * fully into memory. While there is no upper limit on the audio file size, you
     * should avoid using this method for any sound asset greater than 1 MB.
     *
     * The currently supported formats are WAV, MP3 and OGG.
     *
     * The sound buffer should be disposed if it is no longer used via the
     * {@link SoundBuffer#dispose()} method.
     *
     * @param file The sound asset
     *
     * @throws GdxRuntimeException if the asset could not be loaded
     * @return a new {#link SoundBuffer} from the given file.
     */
    @Override
    public SoundHandle newSound (FileHandle file) {
        if (noDevice) {
            return null;
        }

        if (file == null) {
            throw new IllegalArgumentException( "File cannot be null." );
        } else if (!file.exists()) {
            throw new IllegalArgumentException( "File "+file+" does not exist." );
        }
        Class<?> format = extensionToFormat.get(file.extension().toLowerCase());
        if (format == null) throw new GdxRuntimeException("Unknown file extension for sound: " + file);
        try {
            AudioSource sample = (AudioSource)format.getConstructor(new Class[] {FileHandle.class}).newInstance(file);
            return new SoundHandle(sample);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error creating " + format.getName() + " for file: " + file, ex);
        }
    }

    /**
     * Creates a new {@link SoundBuffer} which to play back audio effects.
     *
     * Sound buffers should be used for low latency effects such as gun shots or 
     * explosions. The audio data is retrieved from the file specified and loaded
     * fully into memory. While there is no upper limit on the audio file size, you
     * should avoid using this method for any sound asset greater than 1 MB.
     * 
     * The currently supported formats are WAV, MP3 and OGG.
     *
     * The sound buffer should be disposed if it is no longer used via the 
     * {@link SoundBuffer#dispose()} method.
     *
     * @param source    The sound asset
     *
     * @return a new {#link SoundBuffer} from the given audio source.
     */
    public SoundBuffer newSoundBuffer(AudioSource source) {
        if (noDevice) {
            return null;
        }

        return new SoundHandle(source);
    }
    
    /**
     * Creates a new {@link MusicBuffer} to stream from the given file.
     *
     * A music buffer streams music from the sound asset without fully loading it into
     * memory. This is idea for long running music. The currently supported formats are
     * WAV, MP3 and OGG.
     *
     * It is possible to append additional {@link AudioSource} instances to a music
     * buffer. Doing so creates gapless playback from one music track to another. All
     * sources added to this buffer must have the same sample rate and audio channels
     * (mono or stereo) as the original.
     *
     * Despite what {@link Audio} claims, no sound instances (not even {@link Music})
     * are paused when the application is minimized (it was not clear that this was
     * respected in all instances of class Audio). It is your responsibility to
     * {@link #pause()} the engine from the appropriate {@link ApplicationListener}.
     *
     * @param file The sound asset
     *
     * @throws GdxRuntimeException if the asset could not be loaded
     * @return a new {#link MusicBuffer} from the given file.
     */
    @Override
    public MusicHandle newMusic(FileHandle file) {
        if (noDevice) {
            return null;
        }

        if (file == null) {
            throw new IllegalArgumentException( "File cannot be null." );
        } else if (!file.exists()) {
            throw new IllegalArgumentException( "File "+file+" does not exist." );
        }
        Class<?> format = extensionToFormat.get(file.extension().toLowerCase());
        if (format == null) throw new GdxRuntimeException("Unknown file extension for sound: " + file);
        try {
            AudioSource sample = (AudioSource)format.getConstructor(new Class[] {FileHandle.class}).newInstance(file);
            return new MusicHandle( sample );
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error creating " + format.getName() + " for file: " + file, ex);
        }
    }

    /**
     * Creates a new {@link MusicBuffer} with the given properties.
     *
     * A music buffer streams music from the sound asset without fully loading it into
     * memory. This is idea for long running music. The currently supported formats are
     * WAV, MP3 and OGG.
     *
     * This music asset starts out with no contents and so playing it will not produce
     * any sound. To create music, you should append additional {@link AudioSource}
     * instances to a music buffer. Doing so creates gapless playback from one music
     * track to another. All sources added to this buffer must have the same sample
     * rate and audio channels (mono or stereo) as this buffer.
     *
     * Despite what {@link Audio} claims, no sound instances (not even {@link Music})
     * are paused when the application is minimized (it was not clear that this was
     * respected in all instances of class Audio). It is your responsibility to
     * {@link #pause()} the engine from the appropriate {@link ApplicationListener}.
     *
     * @param isMono        Whether this is a mono stream (as opposed to stereo)
     * @param sampleRate    The fixed sample rate (in Hz) of this stream
     *
     * @return a new {#link MusicBuffer} with the given properties.
     */
    @Override
    public MusicBuffer newMusicBuffer(boolean isMono, int sampleRate) {
        if (noDevice) {
            return null;
        }

        return new MusicHandle( isMono, sampleRate );
    }

    /** 
     * Creates a new {@link AudioDevice} either in mono or stereo mode. 
     * 
     * This device counts as an audio source for the {@link #getCapacity()} method
     * of this audio engine. The AudioDevice must be disposed via its
     * {@link AudioDevice#dispose()} method when it is no longer used.
     * 
     * @param sampleRate    The sampling rate in Hz
     * @param isMono        Whether the device should be mono or stereo
     *
     * @throws GdxRuntimeException if the device could not be created
     * @return a new {@link AudioDevice} either in mono or stereo mode. 
     */
    public AudioDevice newAudioDevice (int sampleRate, final boolean isMono) {
        if (noDevice) {
            return new AudioDeviceAdapter(isMono);
        }
        return new Device(isMono, sampleRate, deviceBufferSize, deviceBufferCount);
    }

    /** 
     * Creates a new {@link AudioRecorder}. 
     *
     * Audio recorders are distinct from playback and do no count against the
     * simultaneous sources for the {@link #getCapacity()} method. The recorder 
     * has to be disposed after it is no longer used.
     * 
     * @param samplingRate  The sampling rate in Hz
     * @param isMono        Whether the recorder should be mono or stereo
     *
     * @throws GdxRuntimeException if the recorder could not be created
     * @return a new {@link AudioRecorder} either in mono or stereo mode. 
     */
    public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
        if (noDevice) {
            return new AudioRecorderAdapter();
        }
        return new JavaSoundAudioRecorder(samplingRate, isMono);
    }

    // #mark Audio Engine Extensions
    /**
     * Returns the number of simultaneous sound sources supported by this audio engine.
     *
     * Possible simultaneous sound sources include instances of {@link SoundBuffer},
     * {@link MusicBuffer}, and {@link AudioDevice}.
     */
    public int getCapacity() {
        return buffers.length;
    }

    /**
     * Pauses all sound instances associated with this audio engine.
     *
     * This will pause everything, and not just music.  This is the method that
     * should be called when your application is minimized.
     */
    public void pause() {
        if (!noDevice) {
            globalPause = true;
            for(int ii = 0; ii < paused.length; ii++) {
                int sourceId = indexToSource.get( ii, -1 );
                if (getSourceState( sourceId ) == AL10.AL_PLAYING) {
                    paused[ii] = true;
                    AL10.alSourcePause( sourceId );
                } else {
                    paused[ii] = false;
                }
            }
        }
    }
    
    /**
     * Pauses all sound instances previously paused.
     *
     * This will only resume sound instances that were paused by the global {@link #pause()}
     * method.  Sound instances paused via their own local pause interface will not be
     * affected.
     */
    public void resume() {
        if (!noDevice) {
            for(int ii = 0; ii < paused.length; ii++) {
                if (paused[ii]) {
                    int sourceId = indexToSource.get( ii, -1 );
                    AL10.alSourcePlay( sourceId );
                    paused[ii] = false;
                }
            }
        }
        globalPause = false;
    }
    
    // #mark OpenAL Source Controls
    /**
     * Returns (and claims) a new OpenAL source for this buffer.
     *
     * The source will be permanently claimed until it is free with the method
     * freeSource.
     *
     * @param sound The buffer to claim this OpenAL source
     */
    protected int obtainSource (OpenALBuffer sound) {
        if (noDevice) {
            return 0;
        }
        
        // Try to find an available buffer
        int sourceId = -1;
        for (int next = (recentIndex + 1) % buffers.length; next != recentIndex && sourceId == -1; next = (next + 1) % buffers.length) {
            if (buffers[next] == null) {
                buffers[next] = sound;
                sourceId = indexToSource.get( next, -1 );
                recentIndex = next;
            }
        }
        
        // Try to evict the oldest evictable buffer
        if (sourceId == -1) {
            for (int next = (recentIndex + 1) % buffers.length; next != recentIndex && sourceId == -1; next = (next + 1) % buffers.length) {
                if (buffers[next] != null && buffers[next].evictable()) {
                    sourceId = indexToSource.get( next, -1 );
                    stopSource(sourceId);
                    buffers[next] = sound;
                    recentIndex = next;
                }
            }
        }
        
        return sourceId;
    }
    
    /**
     * Frees a previously claimed OpenAL source.
     *
     * This method releases a source claimed by obtainSource.
     *
     * @param sourceId  The OpenAL source
     */
    protected void freeSource (int sourceId) {
        if (noDevice) {
            return;
        }

        AL10.alSourceStop(sourceId);
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0);
        int index = sourceToIndex.get(sourceId, -1);
        buffers[index] = null;
    }

    /**
     * Stops (but does not free) an OpenAL source.
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     */
    public void stopSource(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            AL10.alSourceStop(sourceId);
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0);
            buffers[sourceToIndex.get(sourceId,-1)] = null;
        }
    }
    
    /**
     * Pauses an OpenAL source.
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     */
    public void pauseSource(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            if (globalPause) {
                paused[sourceToIndex.get(sourceId, -1)] = true;
            } else if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                AL10.alSourcePause(sourceId);
            }
        }
    }

    /**
     * Resumes a paused OpenAL source.
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     */
    public void resumeSource(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            if (globalPause) {
                paused[sourceToIndex.get(sourceId, -1)] = true;
            } else if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) {
                AL10.alSourcePlay(sourceId);
            }
        }
    }

    /**
     * Returns the OpenAL state for a source.
     *
     * This method returns AL10.AL_STOPPED if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return  the OpenAL state for a source.
     */
    public int getSourceState(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        }
        return AL10.AL_STOPPED;
    }

    /**
     * Sets the gain (volume) for an OpenAL source.
     *
     * The gain should be a value between 0 and 1 (though this is not checked).
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param gain      The new gain.
     */
    public void setSourceGain(int sourceId, float gain) {
        if (sourceId != -1 && !noDevice) {
            gain = Math.max(0,Math.min(gain,1));
            AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        }
    }

    /**
     * Returns the gain (volume) for an OpenAL source.
     *
     * The gain should be a value between 0 and 1 (though this is not checked).
     * This method returns -1.0 if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return the gain (volume) for an OpenAL source.
     */
    public float getSourceGain(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcef(sourceId, AL10.AL_GAIN);
        }
        return -1.0f;
    }

    /**
     * Sets whether to loop an OpenAL source.
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param loop      Whether to loop the source
     */
    public void setSourceLoop(int sourceId, boolean loop) {
        if (sourceId != -1 && !noDevice) {
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
        }
    }
    
    /**
     * Returns whether to loop an OpenAL source.
     *
     * This method returns false if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return whether to loop an OpenAL source.
     */
    public boolean getSourceLoop(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcei(sourceId, AL10.AL_LOOPING) == AL10.AL_TRUE;
        }
        return false;
    }

    /**
     * Sets the pitch (speed) for an OpenAL source.
     *
     * The pitch should be a value between 0.5 and 2.0 (though this is not checked).
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param pitch     The new pitch.
     */
    public void setSourcePitch(int sourceId, float pitch) {
        if (sourceId != -1 && !noDevice) {
            pitch = Math.max(0,pitch);
            AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
        }
    }
    
    /**
     * Returns the pitch (speed) for an OpenAL source.
     *
     * The pitch should be a value between 0.5 and 2.0 (though this is not checked).
     * This method returns 1.0 if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return the pitch (speed) for an OpenAL source.
     */
    public float getSourcePitch(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcef(sourceId, AL10.AL_PITCH);
        }
        return 1.0f;
    }

    /**
     * Sets the pan for an OpenAL source.
     *
     * The pan should be a value between -1 (all left) and 1 (all right) with 0 for
     * the center. This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param pan       The new pan.
     */
    public void setSourcePan(int sourceId, float pan) {
        if (sourceId != -1 && !noDevice) {
            pan = Math.max(-1,Math.min(pan,1));
            AL10.alSource3f(sourceId, AL10.AL_POSITION,
                            MathUtils.cos((pan - 1) * MathUtils.PI / 2), 0,
                            MathUtils.sin((pan + 1) * MathUtils.PI / 2));
        }
    }

    /**
     * Returns the pan for an OpenAL source.
     *
     * The pan should be a value between -1 (all left) and 1 (all right) with 0 for
     * the center. This method returns 0 if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return the pan for an OpenAL source.
     */
    public float getSourcePan(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            AL10.alGetSource(sourceId, AL10.AL_POSITION, floatdata);
            float x = (float)Math.acos(floatdata.get());
            floatdata.clear();
            return (2*x/MathUtils.PI)+1;
        }
        return 0.0f;
    }
    
    /**
     * Sets the offset (in seconds) in an OpenAL source
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param seconds   The offset in seconds
     */ 
    public void setSourceSecOffset(int sourceId, float seconds) {
        if (sourceId != -1 && !noDevice) {
            seconds = Math.max(0,seconds);
            AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, seconds);
        }
    }

    /**
     * Returns the offset (in seconds) in an OpenAL source
     *
     * This method returns -1 if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return the offset (in seconds) in an OpenAL source
     */
    public float getSourceSecOffset(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcef(sourceId, AL11.AL_SEC_OFFSET);
        }
        return -1.0f;
    }

    /**
     * Sets the offset (in bytes) in an OpenAL source
     *
     * This method has no effect if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     * @param offset    The offset in bytes
     */ 
    public void setSourceByteOffset(int sourceId, int offset) {
        if (sourceId != -1 && !noDevice) {
            offset = Math.max(0,offset);
            AL10.alSourcei(sourceId, AL11.AL_BYTE_OFFSET, offset);
        }    
    }

    /**
     * Returns the offset (in bytes) in an OpenAL source
     *
     * This method returns -1 if the source id is invalid.
     *
     * @param sourceId  The OpenAL source
     *
     * @return the offset (in bytes) in an OpenAL source
     */
    public int getSourceByteOffset(int sourceId) {
        if (sourceId != -1 && !noDevice) {
            return AL10.alGetSourcei(sourceId, AL11.AL_BYTE_OFFSET);
        }
        return -1;
    }
    
    /**
     * Updates the audio engine buffers
     *
     * This method is used to push data to the music buffers, and to invoke
     * any necessary callback functions.  It must be executed in the main
     * thread.
     */
    public void update () {
        if (noDevice) {
            return;
        }
        for(int ii = 0; ii < buffers.length; ii++) {
            if (buffers[ii] != null) {
                buffers[ii].update( indexToSource.get(ii, -1) );
            }
        }
    }

    // #mark -
    // #mark Sound Buffer
    /**
     * This class implements a sound buffer for this audio engine
     *
     * OpenAL sound buffers obtain a lock on an OpenAL source, so they must be an inner 
     * class of this engine.  The original {@link OpenALSound} was an ersatz inner class 
     * with a reference to the audio engine. However, because it was not a true inner 
     * class, this created all sorts of package access issues.  We have decided to 
     * simplify this by making the class a true inner class.
     *
     * This is an abusive subclass of OpenALSound.  It subclasses it for interface 
     * purposes only.  As the constructor for OpenALSound does not acquire any resources
     * (that is done in setup) this causes no problems.
     *
     * A sound handle will release all locks on OpenAL sources when it is not playing.  
     * However, it is still best to dispose of it when it is no longer being used.
     */
    private class SoundHandle extends OpenALSound implements SoundBuffer, OpenALBuffer {
        /** The preallocated OpenAL buffer */
        private int bufferId = -1;
        /** The associated audio source */
        private AudioSource sample;
        /** A callback function for when it is finished */
        private OnCompletionListener onCompletionListener = null;
        
        /** Mapping logical sound ids to OpenAL ids */
        private LongMap<Integer> soundToSource;
        /** Mapping OpenAL ids to logical sound ids */
        private IntMap<Long>     sourceToSound;
        /** The next logical sound id to use */
        private long nextSound = 0;
        
        /**
         * Creates a new sound handle from the given source
         *
         * @param sample    The audio source
         */
        public SoundHandle(AudioSource sample) {
            super(null);
            this.sample = sample;
            ByteBuffer buffer = sample.getData();
            int bytesize = buffer.limit();
            
            // Generate an OpenAL buffer
            if (!noDevice) {
                bufferId = AL10.alGenBuffers();
                int format = sample.getChannels() > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
                AL10.alBufferData(bufferId, format, buffer.asShortBuffer(), sample.getSampleRate());
            }
                        
            // Track simultaneous plays
            soundToSource = new LongMap<Integer>();
            sourceToSound = new IntMap<Long>();
        }
        
        /**
         * Disposes this sound buffer, releasing all resources 
         */
        @Override
        protected void finalize() throws Throwable {
            dispose();
        }
        
        // #mark Sound API
        /**
         * Disposes this sound buffer, releasing all resources 
         */
        @Override
        public void dispose () {
            if (noDevice || bufferId == -1) {
                return;
            }
            
            for(IntMap.Entry<Long> entry : sourceToSound.entries()) {
                stopSource( entry.key );
            }
            
            soundToSource.clear();
            sourceToSound.clear();
            sample = null;
            
            AL10.alDeleteBuffers(bufferId);
            bufferId = -1;
            
            onCompletionListener = null;
        }

        /** 
         * Plays an instance of this sound. 
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long play() {
            return play(1);
        }

        /** 
         * Plays an instance of this sound at the given volume.
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @param volume    The volume in the range [0,1]
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long play(float volume) {
            int sourceId = obtainSource(this);
            
            // In case it still didn't work
            if (sourceId == -1) {
                return -1;
            }
            
            Long oldSoundId = sourceToSound.remove(sourceId);
            if (oldSoundId != null) {
                soundToSource.remove(oldSoundId);
            }
            
            long soundId = nextSound++;
            sourceToSound.put(sourceId, soundId);
            soundToSource.put(soundId, sourceId);
            
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_FALSE);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
            if (globalPause) {
                paused[sourceToIndex.get(sourceId, -1)] = true;
            } else {
                AL10.alSourcePlay( sourceId );
            }
            return soundId;
        }

        /** 
         * Plays an instance of this sound with the given settings
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @param volume    The volume in the range [0,1]
         * @param pitch     The pitch in the range [0.5, 2.0]
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long play(float volume, float pitch, float pan) {
            long id = play();
            if (id != -1) {
                int sourceId = soundToSource.get(id);
                setSourcePitch(sourceId, pitch);
                setSourcePan(sourceId, pan);
                setSourceGain(sourceId, volume);
            }
            return id;
        }

        /** 
         * Plays an instance of this sound on a continuous loop.
         *
         * This sound will not stop playing until it is explicitly stopped or 
         * the looping attribute is set to false.
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long loop() {
            return loop(1);
        }

        /** 
         * Plays an instance of this sound on a continuous loop.
         *
         * This sound will not stop playing until it is explicitly stopped or 
         * the looping attribute is set to false.
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @param volume    The volume in the range [0,1]
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long loop(float volume) {
            int sourceId = obtainSource(this);
            
            // In case it still didn't work
            if (sourceId == -1) {
                return -1;
            }
            
            Long oldSoundId = sourceToSound.remove(sourceId);
            if (oldSoundId != null) {
                soundToSource.remove(oldSoundId);
            }
            
            long soundId = nextSound++;
            sourceToSound.put(sourceId, soundId);
            soundToSource.put(soundId, sourceId);
            
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
            if (globalPause) {
                paused[sourceToIndex.get(sourceId, -1)] = true;
            } else {
                AL10.alSourcePlay( sourceId );
            }
            return soundId;
        
        }

        /** 
         * Plays an instance of this sound on a continuous loop.
         *
         * This sound will not stop playing until it is explicitly stopped or 
         * the looping attribute is set to false.
         *
         * If the sound is already playing, it will be played again, concurrently.
         *
         * @param volume    The volume in the range [0,1]
         * @param pitch     The pitch in the range [0.5, 2.0]
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         *
         * @return the id of the sound instance if successful, or -1 on failure. 
         */
        @Override
        public long loop(float volume, float pitch, float pan) {
            long id = loop();
            if (id != -1) {
                int sourceId = soundToSource.get(id);
                setSourcePitch(sourceId, pitch);
                setSourcePan(sourceId, pan);
                setSourceGain(sourceId, volume);
            }
            return id;
        }

        /**
         * Stops all sound instances associated with this buffer.
         */
        @Override
        public void stop() {
            for(IntMap.Entry<Long> entry : sourceToSound.entries()) {
                stopSource( entry.key );
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion( this, entry.value );
                }
            }
            soundToSource.clear();
            sourceToSound.clear();
        }

        /** 
         * Stops the sound instance with the given id.
         * 
         * The id should be one returned by {@link #play()} or {@link #play(float)}. 
         * Once stoped, the id is no longer valid.  If the sound is no longer
         * playing, this has no effect.
         *
         * @param soundId   The sound id 
         */
        @Override
        public void stop(long soundId) {
            Integer sourceId = soundToSource.remove( soundId );
            if (sourceId != null ) {
                stopSource( sourceId );
                sourceToSound.remove(sourceId);
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion( this, soundId );
                }
            }
        }

        /**
         * Pauses all sound instances associated with this buffer.
         */
        @Override
        public void pause() {
            for(IntMap.Entry<Long> entry : sourceToSound.entries()) {
                pauseSource(entry.key);
            }
        }

        /** 
         * Pauses the sound instance with the given id.
         * 
         * The id should be one returned by {@link #play()} or {@link #play(float)}. 
         * If the sound is no longer playing, this has no effect.
         *
         * @param soundId   The sound id 
         */
        @Override
        public void pause(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            pauseSource(sourceId != null ? sourceId : -1);
        }

        /**
         * Resumes all sound instances associated with this buffer.
         */
        @Override
        public void resume() {
            for(IntMap.Entry<Long> entry : sourceToSound.entries()) {
                resumeSource(entry.key);
            }
        }

        /** 
         * Resumes the sound instance with the given id.
         * 
         * The id should be one returned by {@link #play()} or {@link #play(float)}. 
         * If the sound is no longer playing, this has no effect.
         *
         * @param soundId   The sound id 
         */
        @Override
        public void resume(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            resumeSource(sourceId != null ? sourceId : -1);
        }


        /** 
         * Sets the volume of the given instance
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method has no effect.
         *
         * @param soundId   The playback instance
         * @param volume    The volume in range [0,1]
         */
        @Override
        public void setVolume(long soundId, float volume) {
            Integer sourceId = soundToSource.get(soundId);
            setSourceGain(sourceId != null ? sourceId : -1, volume);
        }

        /** 
         * Sets whether the given instance should be on a continuous loop.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method has no effect. Setting this
         * to false on a currently playing instance will cause it to stop once it
         * reaches the end.
         *
         * @param soundId   The playback instance
         * @param looping   Whether to loop the given instance.
         */
        @Override
        public void setLooping(long soundId, boolean looping) {
            Integer sourceId = soundToSource.get(soundId);
            setSourceLoop(sourceId != null ? sourceId : -1, looping);
        }

        /** 
         * Sets the pitch of the given instance
         *
         * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
         * >1 is faster, and <1 is slower.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns 1.
         *
         * @param soundId   The playback instance
         * @param pitch     The pitch in the range [0.5, 2.0]
         */
        @Override
        public void setPitch(long soundId, float pitch) {
            Integer sourceId = soundToSource.get(soundId);
            setSourcePitch(sourceId != null ? sourceId : -1, pitch);
        }

        /** 
         * Sets the pan and volume of the given instance
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method has no effect.
         *
         * @param soundId   The playback instance
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         * @param volume    The volume in the range [0,1]
         */
        @Override
        public void setPan(long soundId, float pan, float volume) {
            Integer sourceId = soundToSource.get(soundId);
            int id = sourceId != null ? sourceId : -1;
            setSourcePan(id, pan);
            setSourceGain(id, volume);
        }

        // #mark Sound Buffer API
        /**
         * Returns the file that generated this asset.
         *
         * The currently supported formats are WAV, MP3 and OGG.
         *
         * @return the file that generated this asset.
         */
        @Override
        public FileHandle getFile() {
            return  sample.getFile();
        }

        /**
         * Returns true if this is a mono audio asset.
         *
         * LibGDX only supports mono and stereo audio assets. It does not support
         * complex multi-channel assets (such as 7.1 surround).
         *
         * @return true if this is a mono audio asset.
         */
        @Override
        public boolean isMono() {
            return sample.getChannels() == 1;
        }

        /**
         * Returns the number of audio samples (per channel) per second.
         *
         * @return the number of audio samples (per channel) per second.
         */
        @Override
        public int getSampleRate() {
            return sample.getSampleRate();
        }

        /**
         * Returns the duration of this audio asset in seconds.
         *
         * @return the duration of this audio asset in seconds.
         */
        @Override
        public float getDuration() {
            return sample.getDuration();
        }

        /** 
         * Returns true if the given instance is actively playing
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns false.
         *
         * @param soundId   The playback instance
         *
         * @return true if the given instance is actively playing
         */
        @Override
        public boolean isPlaying(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourceState(sourceId != null ? sourceId : -1) == AL10.AL_PLAYING;
        }

        /**
         * Returns the current position of this instance in seconds.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns -1.
         *
         * @param soundId   The playback instance
         *
         * @return the current position of this instance in seconds.
         */
        @Override
        public float getPosition(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourceState(sourceId != null ? sourceId : -1);
        }

        /**
         * Sets the current position of this instance in seconds.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method has no effect.  A value outside
         * of the bounds of the assets sets the position to the appropriate end point.
         *
         * @param soundId   The playback instance
         * @param seconds   The current position of this instance in seconds.
         */
        @Override
        public void setPosition(long soundId, float seconds) {
            Integer sourceId = soundToSource.get(soundId);
            setSourceSecOffset(sourceId != null ? sourceId : -1, seconds);
        }
        
        /** 
         * Returns the volume of the given instance
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns -1.
         *
         * @param soundId   The playback instance
         *
         * @return the volume of the given instance
         */
        @Override
        public float getVolume(long soundId, float volume) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourceGain(sourceId != null ? sourceId : -1);
        }

        /** 
         * Returns the loop setting of the given instance
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns false.
         *
         * @param soundId   The playback instance
         *
         * @return the loop setting of the given instance
         */
        @Override
        public boolean getLooping(long soundId, boolean looping) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourceLoop(sourceId != null ? sourceId : -1);
        }

        /** 
         * Returns the pitch of the given instance
         *
         * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
         * >1 is faster, and <1 is slower.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns 1.
         *
         * @param soundId   The playback instance
         *
         * @return the pitch of the given instance
         */
        @Override
        public float getPitch(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourcePitch(sourceId != null ? sourceId : -1);
        }

        /** 
         * Sets the pan of the given instance
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method has no effect.
         *
         * @param soundId   The playback instance
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         */
        @Override
        public void setPan(long soundId, float pan) {
            Integer sourceId = soundToSource.get(soundId);
            setSourcePan(sourceId != null ? sourceId : -1, pan);
        }
        
        /** 
         * Returns the pan value of the given instance
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * The sound id should be one given by {@link #play()} or {@link #play(float)}.
         * If the sound id is not valid, this method returns 0.
         *
         * @param soundId   The playback instance
         *
         * @return the pan value of the given instance
         */
        @Override
        public float getPan(long soundId) {
            Integer sourceId = soundToSource.get(soundId);
            return getSourcePan(sourceId != null ? sourceId : -1);
        }
        
        /** 
         * Register a callback to be invoked when the end of a music stream has been reached during playback.
          * 
          * @param listener the callback that will be run. */
        public void setOnCompletionListener (OnCompletionListener listener) {
            onCompletionListener = listener;
        }
        
        // #mark OpenAL Buffer API
        /**
         * Updates all OpenAL sources for this buffer
         */ 
        public void update() {
            for(IntMap.Entry<Long> entry : sourceToSound.entries()) {
                update(entry.key);
            }
        }
        
        /**
         * Updates the given OpenAL source with the data from this buffer
         *
         * @param sourceId  The OpenAL source
         */
        @Override
        public void update(int sourceId) {
            int state  = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
                Long soundId = sourceToSound.get(sourceId);
                if (soundId != null) {
                    stopSource(sourceId);
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion( this, soundId );
                    }
                }
            }
        }

        /**
         * Returns whether this buffer can be evicted from is current OpenAL source
         *
         * This method is whenever we run out of sources.
         *
         * @return whether this buffer can be evicted from is current OpenAL source
         */
        @Override
           public boolean evictable() {
            return true;
        } 
    }
    
    // #mark -
    // #mark Music Buffer
    /**
     * This class implements a music buffer for this audio engine
     *
     * OpenAL music buffers obtain a lock on an OpenAL source, so they must be an inner 
     * class of this engine.  The original {@link OpenALMusic} was an ersatz inner class 
     * with a reference to the audio engine. However, because it was not a true inner 
     * class, this created all sorts of package access issues.  We have decided to 
     * simplify this by making the class a true inner class.
     *
     * This is an abusive subclass of OpenALMusic.  It subclasses it for interface 
     * purposes only.  As the constructor for OpenALMusic does not acquire any resources
     * (that is done in setup) this causes no problems.
     *
     * A music handle will release all locks on OpenAL sources when it is not playing.  
     * However, it is still best to dispose of it when it is no longer being used.
     */
    private class MusicHandle extends OpenALMusic implements MusicBuffer, OpenALBuffer {
        /** The minimum allowable buffer size (mandated by the simple MP3 decoder) */
        private static final int MINIMUM_SIZE = 16384;
        /** The (maximum) size of an individual OpenAL buffer */
        private final int bufferSize = Math.max(MINIMUM_SIZE,deviceBufferSize);
        /** The number of buffers to use for music */
        private final int bufferCount = 3;
        /** The number of bytes per sample (fixed in OpenAL) */
        private final int bytesPerSample = 2;
        /** An array for reading bytes from the stream */
        private final byte[] tempBytes  = new byte[bufferSize];
        /** A native buffer for sending data to OpenAL */
        private final ByteBuffer tempBuffer = BufferUtils.createByteBuffer( bufferSize );
        
        /** The current locked source id (or -1 for none) */
        private int sourceId = -1;
        
        /** The OpenAL buffers for implementing the audio queue */
        private IntBuffer allBuffers;
        /** The OpenAL buffers actively in use at this time */
        private IntIntMap usedBuffers;
        /** The number of buffers available (to determine blocking) */
        private int bufferAvail;
        /** A pointer to treat the OpenAL buffers as a ring buffer */
        private int nextBuffer;
        
        /** Whether this stream is currently playing */
        private boolean isPlaying;
        /** Whether this audio should be looped */
        private boolean isLooping;
        /** Whether to applying looping to current track only */
        private boolean loopLocal;
        /** The volume of this stream */
        private float volume = 1;
        /** The pitch setting of this stream */
        private float pitch = 1.0f;
        /** The stereo pan of this stream */
        private float pan = 0;
        
        /** The channel format (mono or stereo) of this stream */
        private int format;
        /** The number of samples per second (per channel) */
        private int sampleRate;
        
        /** The audio source queue (in sync with the stream queue) */
        private Array<AudioSource> samples;
        /** The audio stream queue (in sync with the source queue) */
        private Array<AudioStream> streams;
        /** The current position in the audio queue */
        private int position = 0;
        /** The cumulative position of the last rendered audio sample in bytes */
        private long renderedBytes = 0;
        /** A recently deleted audio sample (for delayed callback notifications) */
        private AudioSource orphaned;
        
        /** The first OpenAL buffer values */
        private int bufferStart = 0;
        /** The source mostly recently finished for each buffer id (for transition callbacks) */
        private AudioSource[] leaving;
        /** The source mostly recently acquired for each buffer id (for transition callbacks) */
        private AudioSource[] arriving;
        /** The position mostly recently rendered for each buffer id (for renderedBytes) */
        private long[] byteoffs;
        
        /** A callback function for when it is finished */
        private OnCompletionListener onCompletionListener = null;
        /** A callback function for queue transitions */
        private OnTransitionListener onTransitionListener = null;
        
        /**
         * Creates a new music buffer with the given properties.
         *
         * This music buffer starts out with no contents and playing it will not produce
         * any sound. To create music, you should append additional {@link AudioSource} 
         * instances to a buffer with {@link #addSource}. Doing so creates gapless 
         * playback from one music track to another. 
         * 
         * All sources added to this buffer must have the same sample rate and audio 
         * channels (mono or stereo) as this buffer.
         *
         * @param isMono        Whether this is a mono stream (as opposed to stereo)
         * @param sampleRate    The fixed sample rate of this stream
         */
        public MusicHandle(boolean isMono, int sampleRate) {
            super(null,null);
            format = isMono ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            this.sampleRate = sampleRate;
            samples = new Array<AudioSource>();
            streams = new Array<AudioStream>();
            position = -1;
            allocBuffers();
        }

        /** 
         * Creates a new music buffer to stream from the given file. 
         * 
         * A music buffer streams music from the sound asset without fully loading it 
         * into memory. This is idea for long running music. The currently supported 
         * formats are WAV, MP3 and OGG.
         *
         * It is possible to append additional {@link AudioSource} instances to a music 
         * buffer with {@link #addSource}.. Doing so creates gapless playback from one 
         * music track to another. 
         * 
         * All sources added to this buffer must have the same sample rate and audio 
         * channels (mono or stereo) as this buffer.
         *
         * @param sample    The sound asset
         */
        public MusicHandle(AudioSource sample) {
            this(sample.getChannels() == 1, sample.getSampleRate());
            if (sample.getChannels() > 2) {
                throw new IllegalArgumentException( String.format("OpenAL does not support more than two channels (found %d)",sample.getChannels()) );
            }
            
            samples.add(sample);
            streams.add(sample.getStream());
            position = 0;
        }

        /**
         * Allocates the OpenAL buffers for this music buffer.
         */
        private void allocBuffers() {
            if (allBuffers == null) {
                allBuffers = BufferUtils.createIntBuffer( bufferCount );
                AL10.alGenBuffers( allBuffers );
                int errorCode = AL10.alGetError();
                if (errorCode != AL10.AL_NO_ERROR) {
                    throw new GdxRuntimeException( "Unable to allocate audio buffers. AL Error: " + errorCode );
                }
                bufferAvail = bufferCount;
                usedBuffers = new IntIntMap();
                for(int ii = 0; ii < bufferCount; ii++) {
                    usedBuffers.put(allBuffers.get(ii),0);
                }
                nextBuffer = 0;
                int min = allBuffers.get(0);
                int max = allBuffers.get(0);
                for(int ii = 1; ii < bufferCount; ii++) {
                    int temp = allBuffers.get(ii);
                    if (temp < min) {
                        min = temp;
                    }
                    if (temp > max) {
                        max = temp;
                    }
                }
                bufferStart = min;
                leaving  = new AudioSource[max-min+1];
                arriving = new AudioSource[max-min+1];
                byteoffs = new long[max-min+1];
            }
        }

        /**
         * Initialize the OpenAL buffers, filling them with data.
         */
        private boolean initBuffers() {
            boolean filled = false;
            while (bufferAvail > 0) {
                int bufferId = obtainBuffer();
                if (bufferId == -1 || !fill( bufferId )) {
                    break;
                }
                filled = true;
                AL10.alSourceQueueBuffers( sourceId, bufferId );
                
                int error = AL10.alGetError();
                if (error != AL10.AL_NO_ERROR) {
                    Gdx.app.error("OpenAL", "Music buffer "+bufferId+" could not be initialized: "+AL10.alGetString(error));
                    stop();
                    return false;
                }
            }
            
            
            return filled;
        }

        // #mark Music API
        /**
         * Disposes of this music buffer, releasing all resources
         */
        @Override
        public void dispose() {
            stop();
            if (allBuffers != null) {
                AL10.alDeleteBuffers( allBuffers );
                allBuffers = null;
            }
            
            samples.clear();
            streams.clear();
            
            onCompletionListener = null;
            onTransitionListener = null;
        }
        

        /** 
         * Starts the play back of the music stream. 
         * 
         * In case the stream was paused this will resume the play back. In case the 
         * music stream is finished playing this will restart the play back. 
         *
         * If the parent {@link AudioEngine} is currently paused, this will queue up
         * the music, but it will not be played until the engine is resumed.
         */
        @Override
        public synchronized void play() {
            if (sourceId == -1) {
                sourceId = obtainSource( this );
                if (sourceId == -1) return;
                
                position = 0;
                setSourceLoop( sourceId, false );
                setPan( pan, volume );
                
                boolean filled = initBuffers();
                if (!filled && onCompletionListener != null) {
                    onCompletionListener.onCompletion( this );
                }
            }
            if (!isPlaying) {
                if (globalPause) {
                    paused[sourceToIndex.get(sourceId, -1)] = true;
                } else {
                    setSourceGain( sourceId, volume );
                    setSourcePitch( sourceId, pitch );
                    setSourcePan( sourceId, pan );
                    AL10.alSourcePlay( sourceId );
                }
                isPlaying = true;
            }
        }

        /** 
         * Pauses the play back. 
         * 
         * If the music stream has not been started yet or has finished playing a call 
         * to this method will be ignored. 
         *
         * If this is called when the parent {@link AudioEngine} is currently paused, 
         * the music will not automatically resume when the parent resumes.
         */
        @Override
        public synchronized void pause() {
            pauseSource(sourceId);
            isPlaying = false;
        }

        /** 
         * Resumes the play back. 
         * 
         * This method is similar to {@link #play} except that it only resumes a paused
         * play back, not a finished one.  If the play back is finished, it will be
         * ignored. 
         *
         * If the parent {@link AudioEngine} is currently paused, this will queue up
         * the music, but it will not be played until the engine is resumed.
         */
        public synchronized void resume() {
            resumeSource(sourceId);
            isPlaying = true;
        }
        
        
        /** 
         * Stops a playing or paused stream. 
         *
         * Any locks on the OpenAL source are released. Next time {@link #play()} is 
         * invoked the stream will start from the beginning. 
         */
        @Override
        public synchronized void stop() {
            if (sourceId != -1) {
                reset();
                freeSource( sourceId );
                sourceId = -1;
                isPlaying = false;
            }
        }

        /**
         * Returns whether this music stream is actively playing
         *
         * This method will return false if the parent {@link AudioEngine} is
         * paused.
         * 
         * @return whether this music stream is playing 
         */
        @Override
        public synchronized boolean isPlaying() {
            return isPlaying && !globalPause;
        }

        /** 
         * Sets the volume of the music stream
         *
         * @param volume    The volume in range [0,1]
         */
        @Override
        public synchronized void setVolume(float volume) {
            this.volume = volume;
            setSourceGain(sourceId, volume );
        }

        /** 
         * Returns the volume of the music stream
         *
         * @return the volume of the music stream
         */
        @Override
        public synchronized float getVolume() {
            return this.volume;
        }

        /** 
         * Sets whether the music stream should play in a continuous loop. 
         * 
         * Looping behavior can either be local (the current source position in
         * the queue) or global (across the entire queue).  By default is it global
         * unless otherwise set by {@link #setLoopBehavior}.
         *
         * This can be called at any time, even when the stream is playing.
         * 
         * @param isLooping whether to loop the stream 
         */
        @Override
        public synchronized void setLooping(boolean isLooping) {
            this.isLooping = isLooping;
        }

        /** 
         * Returns true if the music stream plays in a continuous loop. 
         * 
         * Looping behavior can either be local (the current source position in
         * the queue) or global (across the entire queue).  By default is it global
         * unless otherwise set by {@link #setLoopBehavior}.
         *
         * This can be called at any time, even when the stream is playing.
         * 
         * @return true if the music stream plays in a continuous loop.
         */
        @Override
        public synchronized boolean isLooping() {
            return isLooping;
        }


        /** 
         * Sets the pan and volume of the music stream
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         * @param volume    The volume in the range [0,1]
         */
        @Override
        public synchronized void setPan(float pan, float volume) {
            this.volume = volume;
            this.pan = pan;
            setSourcePan(sourceId,pan);
            setSourceGain(sourceId,volume);
        }

        /** 
         * Sets the cumulative playback position in seconds. 
         * 
         * The position is computed globally across all sources in the stream.  So if 
         * the position exceeds the bounds of one source, it will move on to the next
         * source.  The end of the stream is the sum of the duration of all of the
         * component streams. A value outside of the bounds of the stream sets the 
         * position to the appropriate end point.
         *
         * It is safe to call this method while the stream is playing. Calling this 
         * method will clear all of the internal buffers and requeue the audio starting 
         * from the given position.  
         *
         * @param seconds   The cumulative playback position in seconds. 
         */ 
        @Override
        public synchronized void setPosition(float seconds) {
            // The original renderedSeconds implementation was a mess.
            // If you want to seek in a stream, seek in the damn stream.
            // Use renderedSeconds only for getting
            if (sourceId == -1) {
                return;
            }
            
            boolean wasPlaying = isPlaying;
            isPlaying = false;
            AL10.alSourceStop( sourceId );
            unqueueBuffers();
            
            // Determine the byte position we want
            long bytesPerFrame = (format == AL10.AL_FORMAT_MONO16 ? bytesPerSample : 2*bytesPerSample);
            long byteOffs = ((long)(seconds*sampleRate))*bytesPerFrame;
            
            // Find the sample that has that byte position
            int location = 0;
            while(location < samples.size && byteOffs >= streams.get(location).getByteSize()) {
                 byteOffs -= streams.get(location).getByteSize();
                 location++;
            }
            
            if (location != position) {
                streams.get(position).reset();
                position = location;
            }
            
            // Fill buffers starting at that position
            boolean filled = false;
            if (position < streams.size) {
                int length = 0;
                length = streams.get( position ).seek(byteOffs, tempBytes );
                if (length > 0) {
                    int bufferId = obtainBuffer();
                    tempBuffer.clear();
                    tempBuffer.put( tempBytes, 0, length ).flip();
                    AL10.alBufferData( bufferId, format, tempBuffer, sampleRate );
                    AL10.alSourceQueueBuffers( sourceId, bufferId );
                    
                    int error = AL10.alGetError();
                    if (error != AL10.AL_NO_ERROR) {
                        Gdx.app.error("OpenAL", "Stream seek position failed: "+AL10.alGetString(error));
                        stop();
                    } else {
                        initBuffers();
                        filled = true;
                    }
                 } else {
                    filled = initBuffers();
                }
            }
            
            // Position is not there?  We are at end.
            if (!filled) {
                stop();
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion( this );
                }
                if (onTransitionListener != null) {
                    onTransitionListener.onCompletion( this, samples.get( samples.size - 1 ) );
                }
                return;
            }
            
            if (wasPlaying) {
                AL10.alSourcePlay( sourceId );
                isPlaying = true;
            }
        }

        /** 
         * Returns the cumulative playback position in seconds. 
         * 
         * The position is computed globally across all sources in the stream.  So if 
         * the position exceeds the bounds of one source, it will move on to the next
         * source.  The end of the stream is the sum of the duration of all of the
         * component streams. A value outside of the bounds of the stream sets the 
         * position to the appropriate end point.
         *
         * @return the cumulative playback position in seconds. 
         */ 
        @Override
        public synchronized float getPosition() {
            long offset = renderedBytes + (sourceId != -1 ? AL10.alGetSourcei( sourceId, AL11.AL_BYTE_OFFSET) : 0);
            long bytesPerFrame = (format == AL10.AL_FORMAT_MONO16 ? bytesPerSample : 2*bytesPerSample);
            return offset/(float)(sampleRate*bytesPerFrame);
        }

        /** 
         * Registers a callback invoked as a music stream completes.
         *
         * @param listener  The callback that will be run. 
         */
        @Override
        public synchronized void setOnCompletionListener(OnCompletionListener listener) {
            onCompletionListener = listener;
        }

        // #mark Music Buffer API
        /** 
         * Registers a callback invoked as the music queue is processed.
         *
         * @param listener  The callback that will be run. 
         */
        @Override
        public synchronized void setOnTransitionListener(OnTransitionListener listener) {
            onTransitionListener = listener;
        }

        /**
         * Returns true if this is a mono music stream.
         *
         * LibGDX only supports mono and stereo audio streams. It does not support
         * complex multi-channel streams (such as 7.1 surround).
         *
         * @return true if this is a mono music stream.
         */
        @Override
        public synchronized boolean isMono() {
            return format == AL10.AL_FORMAT_MONO16;
        }

        /**
         * Returns the number of audio samples (per channel) per second.
         *
         * All sources added to a music buffer must have the same sample rate (unless
         * you do not care about your pitch, that is.
         *
         * @return the number of audio samples (per channel) per second.
         */
        @Override
        public synchronized int getSampleRate() {
            return sampleRate;
        }

        /**
         * Returns the current duration of this music stream in seconds.
         *
         * The duration is the sum of the durations of all of the sources
         * in the music buffer.
         */
        @Override
        public synchronized float getDuration() {
            long totalBytes = 0;
            for(int ii = 0; ii < streams.size; ii++) {
                totalBytes += streams.get(ii).getByteSize();
            }
            return totalBytes/(float)(sampleRate*(format == AL10.AL_FORMAT_MONO16 ? bytesPerSample: 2*bytesPerSample));
        }

        /** 
         * Sets the pitch of the music stream
         *
         * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
         * >1 is faster, and <1 is slower.
         *
         * @param pitch The pitch of the  music stream
         */
        public synchronized void setPitch(float pitch) {
            this.pitch = pitch;
            setSourcePitch(sourceId,pitch);

        }

        /** 
         * Returns the pitch of the music stream
         *
         * The pitch multiplier is value between 0.5 and 2.0, where 1 is unchanged, 
         * >1 is faster, and <1 is slower.
         *
         * @return the pitch of the  music stream
         */
        public synchronized float getPitch() {
            return pitch;
        }

        /** 
         * Sets the pan of the given music stream
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * @param pan       The pan value -1 (left) to 1 (right). Use 0 for center.
         */
        public synchronized void setPan(float pan) {
            this.pan = pan;
            setSourcePan(sourceId,pan);
        }

        /** 
         * Returns the pan value of the music stream
         *
         * The pan is a value -1 to 1.  The value 0 is the default center position. -1
         * is full left and 1 is full right.
         *
         * @return the pan value of the music stream
         */
        public synchronized float getPan() {
            return pan;
        }

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
        public synchronized void setLoopBehavior(boolean local) {
            loopLocal = local;
        }

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
        public synchronized boolean getLoopBehavior() {
            return loopLocal;
        }

        /**
         * Returns the number of audio sources in this buffer
         *
         * @return the number of audio sources in this buffer
         */
        @Override
        public synchronized int getNumberOfSources() {
            return samples.size;
        }

        /**
         * Returns the current audio sources being played.
         *
         * The buffer does not have to be playing to return a value.  If it does
         * return a source, this is the source that will first be played when 
         * playback resumes.
         *
         * @return the current audio sources being played.
         */
        @Override
        public synchronized AudioSource getCurrent() {
            if (position >= 0 && position < samples.size) {
                return samples.get(position);
            }
            return null;
        }

        /**
         * Returns the audio source at the given queue position.
         *
         * If the position is invalid, this method returns null.
         *
         * @return the audio source at the given queue position.
         */
        @Override
        public synchronized AudioSource getSource(int pos) {
            return samples.get( pos );
        }

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
        public synchronized void setSource(int pos, AudioSource source) {
            int nformat = AL10.AL_FORMAT_VORBIS_EXT;
            if (source.getChannels() <= 2) {
                nformat = source.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            }
            if (source.getSampleRate() != sampleRate || format != nformat) {
                throw new IllegalArgumentException("Source "+source+" does not match the format of this music buffer.");
            }
            samples.set(pos,source);
            streams.set(pos,source.getStream());
        }

        /**
         * Adds the given source as the source queue.
         *
         * It is safe to call this method while the buffer is playing, though
         * gapless playback is only guaranteed if it is added with enough
         * lead time.
         *
         * @param source    The source to add
         */
        @Override
        public synchronized void addSource(AudioSource source) {
            int nformat = AL10.AL_FORMAT_VORBIS_EXT;
            if (source.getChannels() <= 2) {
                nformat = source.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            }
            if (source.getSampleRate() != sampleRate || format != nformat) {
                throw new IllegalArgumentException("Source "+source+" does not match the format of this music buffer.");
            }
            samples.add(source);
            streams.add(source.getStream());
        }

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
        @Override
        public synchronized void insertSource(int pos, AudioSource source) {
            int nformat = AL10.AL_FORMAT_VORBIS_EXT;
            if (source.getChannels() <= 2) {
                nformat = source.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            }
            if (source.getSampleRate() != sampleRate || format != nformat) {
                throw new IllegalArgumentException("Source "+source+" does not match the format of this music buffer.");
            }
            samples.insert(pos,source);
            streams.insert(pos,source.getStream());
            if (pos < position) {
                position++;
            }
        }

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
        @Override
        public synchronized AudioSource removeSource(int pos) {
            if (position == pos) {
                orphaned = samples.get(pos);
            }
            streams.removeIndex(pos);
            return samples.removeIndex( pos );
        }

        /**
         * Clears the music buffer, removing all sources.
         *
         * It is safe to call them method while the buffer is playing.
         * It will immediately halt all playback.
         */
        @Override
        public synchronized void clearSources() {
            streams.clear();
            samples.clear();
        }

        /**
         * Advances the music buffer to the next audio source in the queue
         *
         * The position will be set to the start of the next audio source.
         * If there is no audio source, it will stop playback. If there is
         * an attached transition listener, it will be notified of the
         * transition.
         */
        @Override
        public synchronized void advanceSource() {
            advanceSource(1);
        }

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
        @Override
        public synchronized void advanceSource(int steps) {
            if (steps < 0) {
                throw new IllegalArgumentException("Advance may not move backwards");
            } 
            
            boolean wasPlaying = false;
            boolean filled = false;
            if (sourceId != -1) {
                wasPlaying = isPlaying;
                isPlaying = false;
                AL10.alSourceStop( sourceId );
                unqueueBuffers();
            }
            
            if (steps == 0 && position >= 0 && position < streams.size) {
                streams.get(position).reset();
            } else {
                if (position >= 0 && position < streams.size) {
                    orphaned = samples.get(position);
                }
                if (isLooping && position+steps >= streams.size) {
                    int previous = position;
                    reset();
                    position = (previous + steps) % streams.size;
                } else {
                    position += steps;
                }
            }
            
            if (sourceId != -1) {
                filled = initBuffers( );
                // Position is not there?  We are at end.
                if (!filled) {
                    stop();
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion( this );
                    }
                    if (onTransitionListener != null) {
                        onTransitionListener.onCompletion( this, samples.get( position ) );
                    }
                    return;
                }
        
                if (wasPlaying) {
                    AL10.alSourcePlay( sourceId );
                    isPlaying = true;
                }
            }
        }

        /**
         * Jumps to the given source in the music buffer queue
         *
         * If there is no audio source at the given position, it will stop 
         * playback. If there is an attached transition listener, it will be 
         * notified of the transition.
         *
         * @param pos   The position in the source queue
         */
        @Override
        public synchronized void jumpToSource(int pos) {
            if (pos < 0) {
                pos = 0;
            } else if (pos >= streams.size) {
                pos = streams.size;
            }
            
            boolean wasPlaying = false;
            boolean filled = false;
            if (sourceId != -1) {
                wasPlaying = isPlaying;
                isPlaying = false;
                AL10.alSourceStop( sourceId );
                unqueueBuffers();
            }
            
            reset();
            position = pos;
            
            if (sourceId != -1) {
                filled = initBuffers( );
                // Position is not there?  We are at end.
                if (!filled) {
                    stop();
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion( this );
                    }
                    if (onTransitionListener != null) {
                        onTransitionListener.onCompletion( this, samples.get( position ) );
                    }
                    return;
                }
                
                if (wasPlaying) {
                    AL10.alSourcePlay( sourceId );
                    isPlaying = true;
                }
            }
        }

        /**
         * Resets the music buffer to very beginning of the stream.
         *
         * The stream will return to the very beginning of the first source
         * in the buffer queue.
         */
        @Override
        public synchronized void reset() {
            for(AudioStream stream : streams) {
                stream.reset();
            }
            position = streams.size > 0 ? 0 : -1;
        }

        // #mark OpenAL Buffer API
        /**
         * Updates all OpenAL sources for this buffer
         */
        public synchronized void update() {
            update(sourceId);
        }
        
        /**
         * Updates the given OpenAL source with the data from this buffer
         *
         * @param sourceId  The OpenAL source
         */
        public synchronized void update(int sourceId) {
            if (sourceId != -1) {
                boolean end = false;
                int buffers = AL10.alGetSourcei( sourceId, AL10.AL_BUFFERS_PROCESSED );
                while (buffers-- > 0) {
                    int bufferId = AL10.alSourceUnqueueBuffers( sourceId );
                    if (bufferId == AL10.AL_INVALID_VALUE) {
                        Gdx.app.error( "OpenAL", "Invalid buffer for music "+this );
                        return;
                    }
                    
                    int offset = bufferId-bufferStart;
                    renderedBytes = byteoffs[offset];
                    usedBuffers.put( bufferId, 0 );
                    bufferAvail++;
                    
                    if (!end) {
                        if (fill( bufferId )) {
                            AL10.alSourceQueueBuffers( sourceId, bufferId );
                            if (onTransitionListener != null) {
                                if (arriving[offset] != null) {
                                    if (leaving[offset] != null) {
                                        onTransitionListener.onTransition( this, leaving[offset], arriving[offset] );
                                    } else {
                                        onTransitionListener.onLoopback( this, arriving[offset] );
                                    }
                                }
                            }
                        } else {
                            end = true;
                        }
                    }
                    leaving[offset]  = null;
                    arriving[offset] = null;
                }
                
                if (end && AL10.alGetSourcei( sourceId, AL10.AL_BUFFERS_QUEUED ) == 0) {
                    stop();
                    if (onTransitionListener != null) {
                        onTransitionListener.onCompletion( this, samples.get( samples.size - 1 ) );
                    }
                    if (onCompletionListener != null) {
                        onCompletionListener.onCompletion( this );
                    }
                } else if (isPlaying && AL10.alGetSourcei( sourceId, AL10.AL_SOURCE_STATE ) != AL10.AL_PLAYING) {
                    if (!globalPause) {
                        // A buffer underflow will cause the source to stop.
                        AL10.alSourcePlay( sourceId );
                    }
                }
            }
        }

        /**
         * Returns whether this buffer can be evicted from is current OpenAL source
         *
         * This method is whenever we run out of sources.
         *
         * @return whether this buffer can be evicted from is current OpenAL source
         */
        public boolean evictable() {
            return false;
        }

        // #mark OpenAL Music API
        /**
         * Returns the number of audio channels in this stream.
         *
         * A mono sound will have 1 channel, while a stereo sound will have two 
         * channels.  While OGG and WAV support more than 2 channels, MP3 does
         * not.  Furthermore, LibGDX cannot playback any stream with more than
         * 2 channels in it.
         *
         * @return the number of audio channels in this stream.
         */
        @Override
        public synchronized int getChannels() {
            return format == AL10.AL_FORMAT_MONO16 ? 1 : 2;
        }

        
        /** 
         * Fills as much of the buffer as possible and returns the number of bytes filled. 
         * 
         * Data is read cumulatively, across all sources in the stream.  If there is not
         * enough fata in one source to fill a buffer, it will go to the next one (or
         * loop if that is the current behavior). This method returns <= 0 to indicate 
         * the end of the stream. 
         *
         * Unlike the method fill, this method does not update any callback listeners, nor
         * does it send any of the data to the playback stream.  It is for reading data only.
         *
         * @param buffer    The array to hold the data read
         *
         * @return the number of bytes filled
         */
        @Override
        public synchronized int read(byte[] buffer) {
            int length = 0;
            if (position >= 0 && position < streams.size) {
                length = streams.get(position).read( tempBytes );
                int previous = position;
                if (length <= 0 && isLooping && loopLocal) {
                    streams.get(position).reset();
                    length =  streams.get(position).read( tempBytes );
                }
                while (length <= 0 && position < streams.size-1) {
                    position++;
                    length = streams.get(position).read( tempBytes );
                }
                if (length <= 0 && isLooping) {
                    reset();
                    position = -1;
                    while (length <= 0 && position <= previous) {
                        position++;
                        length = streams.get(position).read( tempBytes );
                    }
                }
            }
            
            return length;
        }

        /**
         * Resets this audio stream rewinding it to the beginning.
         *
         * This differs from {@link #reset} in that it assumes the stream will
         * continue playing.
         */
        @Override
        public synchronized void loop() {
            reset();
        }

        // #mark Internal Methods
        /**
         * Returns the OpenAL source owned by this buffer (or -1 for none)
         *
         * @return the OpenAL source owned by this buffer (or -1 for none)
         */
        public synchronized int getSourceId() {
            return sourceId;
        }

        /**
         * Fills the given OpenAL buffer with as many bytes as possible.
         *
         * Data is read cumulatively, across all sources in the stream.  If there is not
         * enough fata in one source to fill a buffer, it will go to the next one (or
         * loop if that is the current behavior). This method returns false to indicate 
         * the end of the stream. 
         *
         * This method sets internal state to notify transition listeners of transitions
         * in the source queue.  However, the callbacks are not called until the given
         * bufferId is actually rendered.
         *
         * @param bufferID  The OpenAL buffer to fill
         */
        private boolean fill(int bufferID) {
            tempBuffer.clear();
            int length = 0;
            if (orphaned != null) {
                leaving[bufferID-bufferStart]  = orphaned;
                arriving[bufferID-bufferStart] = samples.get( position );
                orphaned = null;
            }
            
            if (position >= 0 && position < streams.size) {
                length = streams.get(position).read( tempBytes );
                int previous = position;
                if (length <= 0 && isLooping && loopLocal) {
                    streams.get(position).reset();
                    leaving[bufferID-bufferStart]  = null;
                    arriving[bufferID-bufferStart] = samples.get( position );
                    length =  streams.get(position).read( tempBytes );
                }
                while (length <= 0 && position < streams.size-1) {
                    position++;
                    leaving[bufferID-bufferStart]  = samples.get( previous );
                    arriving[bufferID-bufferStart] = samples.get( position );
                    length = streams.get(position).read( tempBytes );
                }
                if (length <= 0 && isLooping) {
                    reset();
                    position = -1;
                    while (length <= 0 && position <= previous) {
                        position++;
                        leaving[bufferID-bufferStart]  = null;
                        arriving[bufferID-bufferStart] = samples.get( position );
                        length = streams.get(position).read( tempBytes );
                    }
                }
            }

            if (length <= 0) {
                leaving[bufferID-bufferStart] = null;
                arriving[bufferID-bufferStart] = null;
                return false;
            }
            
            long totalbytes = 0;
            for (int ii = 0; ii < position; ii++) {
                totalbytes += streams.get(ii).getByteSize();
            }
            byteoffs[bufferID-bufferStart] = totalbytes + streams.get(position).getByteOffset();

            tempBuffer.put( tempBytes, 0, length ).flip();
            AL10.alBufferData( bufferID, format, tempBuffer, sampleRate );
            return true;
        }

        /**
         * Returns the next empty and available OpenAL buffer for writing.
         *
         * This method uses a ring buffer pointer to make sure we are not trashing the
         * same buffer over and over.  The buffer returned is locked and cannot be
         * used again until OpenAL releases it.
         *
         * @return the next empty and available OpenAL buffer for writing.
         */
        private int obtainBuffer() {
            int endBuffer = (nextBuffer+bufferCount-1) % bufferCount;
            for(int ii = nextBuffer; ii != endBuffer; ii = ((ii+1) % bufferCount)) {
                int bufferId = allBuffers.get(ii);
                if (usedBuffers.get(bufferId,1) == 0) {
                    usedBuffers.put(bufferId,1);
                    nextBuffer = (ii+1) % bufferCount;
                    bufferAvail--;
                    return bufferId;
                }
            }
            return -1;
        }
        
        /**
         * Recovers any available buffers
         */
        private void unqueueBuffers() {
            int buffers = AL10.alGetSourcei( sourceId, AL10.AL_BUFFERS_PROCESSED );
            while (buffers-- > 0) {
                int bufferId = AL10.alSourceUnqueueBuffers( sourceId );
                if (bufferId == AL10.AL_INVALID_VALUE) {
                    Gdx.app.error( "OpenAL", "Invalid buffer unqueued for music "+this );
                    return;
                }
                int offset = bufferId-bufferStart;
                renderedBytes = byteoffs[offset];
                usedBuffers.put( bufferId, 0 );
                bufferAvail++;
            }
        }
    }

    // #mark -
    // #mark Audio Device
    /** 
     * This class represents a direct audio device for OpenAL
     *
     * This class is heavily adapted from OpenALAudioDevice by Nathan Sweet.  It
     * has been moved to an inner class and implements the {@link OpenALBuffer} class
     * to make it consistent with the other sources.  In particular, this addition 
     * allows us to be more proactive with recycling the OpenAL buffers, preventing
     * unnecessary blocking.  It also fixes a lot of spec mistakes in what was clearly
     * a hastely written class (Nathan is generally very good).
     *
     * Because devices are often uses in a multithreaded context, we have tried to
     * make this class as thread safe as possible.
     */
    public class Device implements AudioDevice, OpenALBuffer {
        /** The number of bytes per sample (fixed in OpenAL) */
        static private final int bytesPerSample = 2;
        /** The (maximum) size of an individual OpenAL buffer */
        private final int bufferSize;
        /** The number of buffers to use for the device */
        private final int bufferCount;
        /** The native byte buffer for writing to OpenAL */
        private final ByteBuffer tempBuffer;
        /** The internal byte buffer for processing audio samples */
        private byte[] bytes;
        
        /** The OpenAL buffers for implementing the audio queue */
        private IntBuffer allBuffers;
        /** The OpenAL buffers actively in use at this time */
        private IntIntMap usedBuffers;
        /** The number of buffers available (to determine blocking) */
        private int bufferAvail;
        /** A pointer to treat the OpenAL buffers as a ring buffer */
        private int nextBuffer;
        
        /** The OpenAL source currently owned by this device (-1 for none) */
        private int sourceId = -1;
        /** The channel format (mono or stereo) of this stream */
        private int format;
        /** The number of samples per second (per channel) */
        private int sampleRate;
        
        /** Whether this device is currently playing */
        private boolean isPlaying;
        /** The volume of this device */
        private float volume = 1;
        
        /** The cumulative position of the last rendered audio sample in bytes */
        private long renderedBytes;
        /** The first OpenAL buffer values */
        private int bufferStart = 0;
        /** The position mostly recently rendered for each buffer id (for renderedBytes) */
        private long[] byteoffs;

        /** The number of seconds to render a single audio buffer */
        float secondsPerBuffer = 0;

        /**
         * Creates a new audio device with the given parameters.
         *
         * 
         * @param sampleRate    The sampling rate in Hz
         * @param isMono        Whether the device should be mono or stereo
         * @param bufferSize    The number of bytes per OpenAL buffer
         * @param bufferCount   The number of OpenAL buffers to use
         */
        private Device(boolean isMono, int sampleRate, int bufferSize, int bufferCount) {
            int channels = isMono ? 1 : 2;
            this.bufferSize = bufferSize;
            this.bufferCount = bufferCount;
            bufferAvail = bufferCount;
            this.format = channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
            this.sampleRate = sampleRate;
            secondsPerBuffer = (float)bufferSize / bytesPerSample / channels / sampleRate;
            tempBuffer = BufferUtils.createByteBuffer( bufferSize );
            allocBuffers();
        }

        /**
         * Allocates the OpenAL buffers to use for this device
         */
        private void allocBuffers() {
            if (allBuffers == null) {
                usedBuffers = new IntIntMap();
                allBuffers = BufferUtils.createIntBuffer( bufferCount );
                AL10.alGenBuffers( allBuffers );
                int errorCode = AL10.alGetError();
                if (errorCode != AL10.AL_NO_ERROR) {
                    throw new GdxRuntimeException( "Unable to allocate audio buffers. AL Error: " + errorCode );
                }
                for(int ii = 0; ii < bufferCount; ii++) {
                    usedBuffers.put(allBuffers.get(ii),0);
                }
                bufferAvail = bufferCount;
                nextBuffer = 0;
                int min = allBuffers.get(0);
                int max = allBuffers.get(0);
                for(int ii = 1; ii < bufferCount; ii++) {
                    int temp = allBuffers.get(ii);
                    if (temp < min) {
                        min = temp;
                    }
                    if (temp > max) {
                        max = temp;
                    }
                }
                bufferStart = min;
                byteoffs = new long[max-min+1];
            }
        }

        // #mark Audio Device API
        /**
         * Disposes this audio device, releasing all resources.
         */
        @Override
        public void dispose() {
            if (allBuffers == null) {
                return;
            }
            if (sourceId != -1) {
                freeSource( sourceId );
                sourceId = -1;
            }
            AL10.alDeleteBuffers( allBuffers );
            allBuffers = null;
        }

        /** 
         * Writes the array of 16-bit signed PCM samples to the audio device.
         *
         * Stereo data should have its samples interleaved. This method blocks until 
         * all samples have been processed.
         *
         * Calling this method while the parent audio engine is paused is dangerous.
         * This will block because it is unable to clear the queue. Unless you are 
         * using this device in a multithreaded context, this can lead to a deadlock.
         * 
         * @param samples       The samples.
         * @param offset        The offset into the samples array
         * @param numSamples    The number of samples to write to the device 
         */
        @Override
        public void writeSamples(short[] samples, int offset, int numSamples) {
            if (bytes == null || bytes.length < numSamples * 2) bytes = new byte[numSamples * 2];
            int end = Math.min( offset + numSamples, samples.length );
            for (int i = offset, ii = 0; i < end; i++) {
                short sample = samples[i];
                bytes[ii++] = (byte) (sample & 0xFF);
                bytes[ii++] = (byte) ((sample >> 8) & 0xFF);
            }
            writeBytes( bytes, 0, numSamples * 2 );
        }

        /** 
         * Writes the array of float values PCM samples to the audio device.
         *
         * Stereo data should have its samples interleaved. This method blocks until 
         * all samples have been processed.
         *
           * Calling this method while the parent audio engine is paused is dangerous.
         * This will block because it is unable to clear the queue. Unless you are 
         * using this device in a multithreaded context, this can lead to a deadlock.
         * 
         * @param samples       The samples.
         * @param offset        The offset into the samples array
         * @param numSamples    The number of samples to write to the device 
         */
        @Override
        public void writeSamples(float[] samples, int offset, int numSamples) {
            if (bytes == null || bytes.length < numSamples * 2) bytes = new byte[numSamples * 2];
            int end = Math.min( offset + numSamples, samples.length );
            for (int i = offset, ii = 0; i < end; i++) {
                float floatSample = samples[i];
                floatSample = MathUtils.clamp( floatSample, -1f, 1f );
                int intSample = (int) (floatSample * 32767);
                bytes[ii++] = (byte) (intSample & 0xFF);
                bytes[ii++] = (byte) ((intSample >> 8) & 0xFF);
            }
            writeBytes( bytes, 0, numSamples * 2 );
        }

        /** 
         * Returns whether this device is in mono or stereo mode.
         *
         * @return whether this device is in mono or stereo mode. 
         */
        @Override
        public boolean isMono() {
            return format == AL10.AL_FORMAT_MONO16;
        }

        /** 
         * Sets the volume in the range [0,1]
         *
         * @param volume    The device volume 
         */
        @Override
        public void setVolume(float volume) {
            synchronized (this) {
                this.volume = volume;
                setSourceGain( sourceId, volume );
            }
        }

        /**
         * Returns the device latency in number of samples
         *
         * This is the number of samples that must be processed before the currently
         * written batch can be processed.  It exists because an audio devices does
         * a lot of internal buffering.
         * 
         * This value is NOT samples per channel. If the device is stereo, then it is
         * multiplied times two.
         *
         * @return the device latency in number of samples
         */
        @Override
        public int getLatency() {
            // The original OpenAL device read the spec VERY wrong.
            // There a quite a few spec bugs like this in the audio code (see pausing music)
            int bufferLatency = bufferSize / bytesPerSample;
            return bufferLatency * bufferCount;
        }

        // #mark OpenAL Buffer API
        /**
         * Stops this device, freeing all OpenAL resources.
         */
        @Override
        public void stop() {
            synchronized (this) {
                if (sourceId != -1) {
                    freeSource( sourceId );
                    sourceId = -1;
                    renderedBytes = 0;
                    isPlaying = false;
                }
            }
        }

        /**
         * Pauses this audio device.
         */
        @Override
        public void pause() {
            synchronized (this) {
                pauseSource(sourceId);
                isPlaying = false;
            }
        }

        /**
         * Resumes this audio device after pausing.
         */
        @Override
        public void resume() {
            synchronized (this) {
                resumeSource(sourceId);
                isPlaying = true;
            }
        }

        /**
         * Updates all OpenAL sources for this buffer
         */
        @Override
        public void update() {
            // I know Java is reentrant, but I always feel dirty doing this
            synchronized (this) {
                update(sourceId);
            }
        }
        
        /**
         * Updates the given OpenAL source with the data from this buffer
         *
         * @param sourceId  The OpenAL source
         */
        @Override
        public void update(int sourceId) {
            synchronized (this) {
                if (sourceId != -1 && sourceId == this.sourceId) {
                    int buffers = AL10.alGetSourcei( sourceId, AL10.AL_BUFFERS_PROCESSED );
                    while (buffers-- > 0) {
                        int bufferId = AL10.alSourceUnqueueBuffers( sourceId );
                        if (bufferId == AL10.AL_INVALID_VALUE) {
                            Gdx.app.error( "OpenAL", "Invalid buffer for music " + this );
                            return;
                        }
                        renderedBytes += byteoffs[bufferId-bufferStart];
                        usedBuffers.put( bufferId, 0 );
                        bufferAvail++;
                    }
                }
            }
        }

        /**
         * Returns whether this buffer can be evicted from is current OpenAL source
         *
         * This method is whenever we run out of sources.
         *
         * @return whether this buffer can be evicted from is current OpenAL source
         */
        @Override
        public boolean evictable() {
            return false;
        }
        
        // #mark Addition Public Methods
        /**
         * Returns the number of audio samples (per channel) per second.
         *
         * @return the number of audio samples (per channel) per second.
         */
        public int getSampleRate() {
            return sampleRate;
        }
        
        
        /**
         * Returns whether this audio device is actively playing
         *
         * This method will return false if the parent {@link AudioEngine} is
         * paused.
         * 
         * @return whether this audio device is actively playing
         */
        public boolean isPlaying() {
            synchronized (this) {
                return isPlaying && !globalPause;
            }
        }

        /** 
         * Returns the volume of the music stream
         *
         * @return the volume of the music stream
         */
        public float getVolume() {
            synchronized (this) {
                return volume;
            }
        }
        
        /**
         * Sets the logical playback position in seconds. 
         *
         * Unlike {@link Music}, setting this value does not actually change the
         * playback position of the device.  Instead, it marks the current playback
         * position as the as the position given in seconds.
         *
         * @param position  The logical playback position in seconds. 
         */
        public void setPosition(float position) {
            synchronized (this) {
                int channels = getChannels();
                double pos = position;
                renderedBytes = (long)(pos*sampleRate*channels*bytesPerSample);
            }
        }

        /** 
         * Returns the logical playback position in seconds. 
         * 
         * Unlike a {@link Music} stream, a device cannot be rewound.  So this is the
         * number of seconds of audio that has been written to the device since it
         * was created.  This value can be reset by calling {@link #setPosition}.
         *
         * @return the logical playback position in seconds. 
         */ 
        public float getPosition() {
            synchronized (this) {
                if (sourceId == -1) return 0;
                int channels = getChannels();
                long offset = renderedBytes + AL10.alGetSourcei( sourceId, AL11.AL_BYTE_OFFSET );
                return (float)offset/bytesPerSample/channels/sampleRate;
            }
        }

        /** 
         * Writes the array of bytes to the audio device.
         *
         * The byte representation of audio data is platform dependent.  You should 
         * not call this method unless you know what you are doing.  We can only
         * guaranteed the the {@link AudioStream#read} and {@link AudioStream#seek}
         * methods of {@link AudioStream} have the write byte format for this method.
         *
         * Calling this method while the parent audio engine is paused is dangerous.
         * This will block because it is unable to clear the queue. Unless you are 
         * using this device in a multithreaded context, this can lead to a deadlock.
         * 
         * @param data      The bytes to write to the device.
         * @param offset    The offset into the samples array
         * @param length    The number of bytes to write to the device
         */
        public void writeBytes(byte[] data, int offset, int length) {
            if (length < 0) {
                throw new IllegalArgumentException( "length cannot be < 0." );
            }
            
            if (globalPause) {
                Gdx.app.log("OpenAL", "Writing samples to a paused audio sytem [potential deadlock]");
            }
            
            // If I cared, I would make this more thread-efficent.
            // But this is OpenAL, and there is only so much lipstick this pig will take
            synchronized (this) {
                if (sourceId == -1) {
                
                    sourceId = obtainSource( this );
                    if (sourceId == -1) return;
                
                    AL10.alSourcei( sourceId, AL10.AL_LOOPING, AL10.AL_FALSE );
                    AL10.alSourcef( sourceId, AL10.AL_GAIN, volume );
                
                    // Fill and queue some the initial buffers
                    int written = fill(data, offset, length);
                    length -= written;
                    offset += written;
                
                    if (globalPause) {
                        paused[sourceToIndex.get(sourceId, -1)] = true;
                    } else {
                        AL10.alSourcePlay( sourceId );
                    }
                    isPlaying = true;
                }
            
                while (length > 0) {
                    drain();
                    int written = fill( data, offset, length );
                    length -= written;
                    offset += written;
                
                    // A buffer underflow will cause the source to stop.
                    if (!isPlaying || getSourceState( sourceId ) != AL10.AL_PLAYING) {
                        if (globalPause) {
                            paused[sourceToIndex.get(sourceId, -1)] = true;
                        } else {
                            AL10.alSourcePlay( sourceId );
                        }
                        isPlaying = true;
                    }
                }
            }
        }

        // #mark Internal Methods
        /**
         * Returns the number of channels (1 for mono, 2 for stereo) of this audio device
         *
         * @return the number of channels (1 for mono, 2 for stereo) of this audio device
         */
        private int getChannels() {
            return format == AL10.AL_FORMAT_STEREO16 ? 2 : 1;
        }

        /**
         * Returns the next empty and available OpenAL buffer for writing.
         *
         * This method uses a ring buffer pointer to make sure we are not trashing the
         * same buffer over and over.  The buffer returned is locked and cannot be
         * used again until OpenAL releases it.
         *
         * @return the next empty and available OpenAL buffer for writing.
         */
        private int obtainBuffer() {
            int endBuffer = (nextBuffer+bufferCount-1) % bufferCount;
            for(int ii = nextBuffer; ii != endBuffer; ii = ((ii+1) % bufferCount)) {
                int bufferId = allBuffers.get(ii);
                if (usedBuffers.get(bufferId,1) == 0) {
                    usedBuffers.put(bufferId,1);
                    nextBuffer = (ii+1) % bufferCount;
                    bufferAvail--;
                    return bufferId;
                }
            }
            return -1;
        }
        
        /**
         * Reads as much audio data as possible, sending the bytes to OpenAL
         *
         * Unlike {@link #writeBytes}, this method will not block.  Instead, it
         * will return the number of bytes that could be sent to OpenAL (using
         * all available buffers). This value could be 0.
         *
         * @param data      The audio data
         * @param offset    The starting offset in the array
         * @param length    The number of bytes to send
         *
         * @return the number of bytes read
         */
        private int fill(byte[] data, int offset, int length) {
            int written = 0;
            while (written < length) {
                if (bufferAvail == 0) {
                    return written;
                }
                int bufferId = obtainBuffer();
                int amount = Math.min( bufferSize, length-written );
                tempBuffer.clear();
                tempBuffer.put(data, offset, amount).flip();
                AL10.alBufferData(bufferId, format, tempBuffer, sampleRate);
                AL10.alSourceQueueBuffers(sourceId, bufferId);
                written += amount;
                offset  += amount;
            }
            return written;
        }
        
        /**
         * Blocks until an OpenAL buffer is available for use.
         */
        private void drain() {
            while (bufferAvail == 0) {
                update(sourceId);
                
                if (bufferAvail == 0) {
                    // Wait for buffer to be free.
                    try {
                        Thread.sleep( (long) (1000 * secondsPerBuffer) );
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

    }
}