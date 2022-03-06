/*
 * GDXApp.java
 *
 * This is a replacement for LwjglApplication. It allows us to force GL30 and to add our
 * new audio engine.
 *
 * @author Walker M. White
 * @data   04/29/20
 */
package com.xstudios.backend;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglProxy;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALLwjglAudio;
import com.xstudios.salvage.assets.ResourceManager;
import com.xstudios.salvage.util.Controllers;

/**
 * This class represents a desktop game application using Lwjgl.
 *
 * This class is preferable to {@link LwjglApplication} because it is guaranteed to use 
 * GL30, and it has a slightly more useful audio engine.  It extends {@link LwjglProxy} 
 * because we needed to access some package internals.
 */
public class GDXApp extends LwjglProxy {
    /** Settings, remembered for second initialization phase */
    protected GDXAppSettings config;

    /** The replacement audio engine */
    protected GDXAudio engine;

    /**
     * Creates a game application with the given listener and settings.
     *
     * The application listener drives the primary game loop.  It should be a 
     * platform-independent class in the core package.
     *
     * @param listener  The game driver
     * @param config    The game settings
     */
    public GDXApp(ApplicationListener listener, GDXAppSettings config) {
        super( listener, config.getLwjglConfiguration() );
        this.config = config;

        // Bootup the resource and the controller manager
        ResourceManager.get();
        Controllers.get().setActive(config.useControllers);
    }

    /**
     * Returns the settings for this application
     *
     * @return the settings for this application
     */
    public GDXAppSettings getSettings() {
        return config;
    }

    /**
     * Initializes the audio engine
     * 
     * This encapsulates the initialization so it can be overridden (for say, a 
     * multi-step initialization).
     */
    @Override
    protected void initAudio(LwjglApplicationConfiguration config) {
        if (!LwjglApplicationConfiguration.disableAudio) {
            try {
                // Piece of sh*t OpenAL only guarantees 16 sources, so we need to be guilty
                audio = new OpenALLwjglAudio(config.audioDeviceSimultaneousSources,
                        config.audioDeviceBufferCount,
                        config.audioDeviceBufferSize);
            } catch (Throwable t) {
                log("GDXApp", "Couldn't initialize audio; disabling audio", t);
                LwjglApplicationConfiguration.disableAudio = true;
            }
        }

        boolean fallback = false;
        if (!LwjglApplicationConfiguration.disableAudio) {
            try {
                // No that we have initialized OpenAL, steel its resources
                engine = new GDXAudio(config.audioDeviceSimultaneousSources,
                        config.audioDeviceBufferCount,
                        config.audioDeviceBufferSize);
            } catch (Throwable t) {
                log("GDXApp", "Couldn't initialize secondary audio; falling back", t);
                engine = null;
                fallback = true;
            }
        }

        if (fallback) {
            try {
                // Dispose of the audio engine and reinitialize
                if (audio != null) { audio.dispose(); }
                audio = new OpenALLwjglAudio(config.audioDeviceSimultaneousSources,
                                             config.audioDeviceBufferCount,
                                             config.audioDeviceBufferSize);
            } catch (Throwable t) {
                log("LwjglApplication", "Couldn't reinitialize audio; disabling audio", t);
                LwjglApplicationConfiguration.disableAudio = true;
            }
        }
    }

    /**
     * Disposes the audio on cleanup.
     */
    @Override
    protected void disposeAudio() {
        if (engine != null) {
            engine.dispose();
            engine = null;
        }
        if (audio != null) {
            audio.dispose();
            audio = null;
        }
    }

    /**
     * Returns the active audio engine.
     *
     * @return the active audio engine.
     */
    @Override
    protected Audio chooseAudio() {
        if (engine != null) {
            return engine;
        } else if (audio != null) {
            return audio;
        }
        return null;
    }

    /**
     * Updates the audio loop for any PCM buffering
     */
    @Override
    protected void updateAudio() {
        if (engine != null) {
            engine.update();
        } else if (audio != null) {
            audio.update();
        }
    }

    /**
     * Dispose any additional resources in the final cleanup phase.
     */
    protected void disposeResources() {
        ResourceManager.dispose();
    }

}