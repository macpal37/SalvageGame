/*
 * LwjglProxy.java
 *
 * This is a modification of LwjglApplication to allow us to insert a custom audio
 * engine.  It exposes hooks to provide access at the subclass level.
 *
 * @author: Walker M. White (Based HEAVILY on work by LibGDX developers)
 * @date:	04/29/30
 */
package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALLwjglAudio;
import com.badlogic.gdx.utils.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.File;

/**
 * This application class provides an OpenGL surface fullscreen or in a lightweight window.
 *
 * This class is a modification of {@link LwjglApplication} that exposes the audio engine
 * to subclasses. It also provides some hooks to allow this engine to be replaced.
 */
public class LwjglProxy implements Application {
    protected final LwjglGraphics graphics;
    protected OpenALLwjglAudio audio;
    protected final Files files;
    protected final LwjglInput input;
    protected final LwjglNet net;
    protected final ApplicationListener listener;
    protected Thread mainLoopThread;
    protected boolean running = true;
    protected final Array<Runnable> runnables = new Array<Runnable>();
    protected final Array<Runnable> executedRunnables = new Array<Runnable>();
    protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(
            LifecycleListener.class);
    protected int logLevel = LOG_INFO;
    protected ApplicationLogger applicationLogger;
    protected String preferencesdir;
    protected Files.FileType preferencesFileType;

    protected boolean shutdown = false;

	// I am not going to comment these as the LibGDX team did not.
    public LwjglProxy(ApplicationListener listener, String title, int width, int height) {
        this(listener, createConfig(title, width, height));
    }

    public LwjglProxy(ApplicationListener listener) {
        this(listener, null, 640, 480);
    }

    public LwjglProxy(ApplicationListener listener, LwjglApplicationConfiguration config) {
        this(listener, config, new LwjglGraphics(config));
    }

    public LwjglProxy(ApplicationListener listener, Canvas canvas) {
        this(listener, new LwjglApplicationConfiguration(), new LwjglGraphics(canvas));
    }

    public LwjglProxy(ApplicationListener listener, LwjglApplicationConfiguration config, Canvas canvas) {
        this(listener, config, new LwjglGraphics(canvas, config));
    }

    public LwjglProxy(ApplicationListener listener, LwjglApplicationConfiguration config, LwjglGraphics graphics) {
        LwjglNativesLoader.load();
        setApplicationLogger(new LwjglApplicationLogger());
        if (config.title == null) config.title = listener.getClass().getSimpleName();
        this.graphics = graphics;
        initAudio( config );

        files = createFiles();
        input = createInput(config);
        net = new LwjglNet(config);
        this.listener = listener;
        this.preferencesdir = config.preferencesDirectory;
        this.preferencesFileType = config.preferencesFileType;

        Gdx.app = this;
        Gdx.graphics = graphics;
        Gdx.audio = chooseAudio();
        Gdx.files = files;
        Gdx.input = input;
        Gdx.net = net;
        initialize();
    }
    
    // #mark -
    // #mark Audio Hooks
    /**
     * Initializes the audio engine
     * 
     * This encapsulates the initialization so it can be overridden (for say, a 
     * multi-step initialization).
     */
    protected void initAudio(LwjglApplicationConfiguration config) {
        if (!LwjglApplicationConfiguration.disableAudio) {
            try {
                audio = new OpenALLwjglAudio(config.audioDeviceSimultaneousSources,
                                             config.audioDeviceBufferCount,
                                             config.audioDeviceBufferSize);
            } catch (Throwable t) {
                log("LwjglApplication", "Couldn't initialize audio, disabling audio", t);
                LwjglApplicationConfiguration.disableAudio = true;
            }
        }
    }

    /**
     * Disposes the audio on cleanup.
     */
    protected void disposeAudio() {
        if (audio != null) { audio.dispose(); }
    }

    /**
     * Returns the active audio engine.
     */
    protected Audio chooseAudio() {
        if (audio != null) { return audio; }
        return null;
    }

    /**
     * Updates the audio loop for any PCM buffering
     */
    protected void updateAudio() {
        if (audio != null) { audio.update(); }
    }
    
    /** 
     * Executed when the application enters background.
     *
     * This is necessary on some systems as certain features (e.g. controllers) are
     * unstable when the application is in the background.
     */
    protected void enterBackground() {}

    /** 
     * Executed when the application exits background.
     *
     * This is necessary on some systems as certain features (e.g. controllers) are
     * unstable when the application is in the background.
     */
    protected void exitBackground() {}

    /**
     * Dispose any additional resources in the final cleanup phase.
     */
    protected void disposeResources() {}

    // #mark -
    // Return to the LwjglApplication methods
    private static LwjglApplicationConfiguration createConfig (String title, int width, int height) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = title;
        config.width = width;
        config.height = height;
        config.vSyncEnabled = true;
        return config;
    }

    protected void initialize () {
        mainLoopThread = new Thread("LWJGL Application") {
            @Override
            public void run () {
                graphics.setVSync(graphics.config.vSyncEnabled);
                try {
                    LwjglProxy.this.mainLoop();
                } catch (Throwable t) {
                    if (audio != null) audio.dispose();
                    Gdx.input.setCursorCatched(false);
                    if (t instanceof RuntimeException)
                        throw (RuntimeException)t;
                    else
                        throw new GdxRuntimeException(t);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Cleanup") {
            public void run() {
                LwjglProxy.this.cleanup();
            }
        });
        mainLoopThread.start();
    }

    protected void mainLoop () {
        SnapshotArray<LifecycleListener> lifecycleListeners = this.lifecycleListeners;

        try {
            graphics.setupDisplay();
        } catch (LWJGLException e) {
            throw new GdxRuntimeException(e);
        }

        listener.create();
        graphics.resize = true;

        int lastWidth = graphics.getWidth();
        int lastHeight = graphics.getHeight();

        graphics.lastTime = System.nanoTime();
        boolean wasPaused = false;
        boolean wasBackground = false;
        try {
            while (running) {
                Display.processMessages();
                if (Display.isCloseRequested()) exit();

                boolean isMinimized = graphics.config.pauseWhenMinimized && !Display.isVisible();
                boolean isBackground = !Display.isActive();
                boolean paused = isMinimized || (isBackground && graphics.config.pauseWhenBackground);
                if (!wasPaused && paused) { // just been minimized
                    wasPaused = true;
                    synchronized (lifecycleListeners) {
                        LifecycleListener[] listeners = lifecycleListeners.begin();
                        for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
                            listeners[i].pause();
                        lifecycleListeners.end();
                    }
                    listener.pause();
                }
                if (wasPaused && !paused) { // just been restore from being minimized
                    wasPaused = false;
                    synchronized (lifecycleListeners) {
                        LifecycleListener[] listeners = lifecycleListeners.begin();
                        for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
                            listeners[i].resume();
                        lifecycleListeners.end();
                    }
                    listener.resume();
                }
                if (!wasBackground && isBackground) {
                    wasBackground = true;
                    enterBackground();
                } else if (wasBackground && !isBackground) {
                    wasBackground = false;
                    exitBackground();
                }

                boolean shouldRender = false;

                if (graphics.canvas != null) {
                    int width = graphics.canvas.getWidth();
                    int height = graphics.canvas.getHeight();
                    if (lastWidth != width || lastHeight != height) {
                        lastWidth = width;
                        lastHeight = height;
                        Gdx.gl.glViewport( 0, 0, lastWidth, lastHeight );
                        listener.resize( lastWidth, lastHeight );
                        shouldRender = true;
                    }
                } else {
                    graphics.config.x = Display.getX();
                    graphics.config.y = Display.getY();
                    if (graphics.resize || Display.wasResized()
                            || (int) (Display.getWidth() * Display.getPixelScaleFactor()) != graphics.config.width
                            || (int) (Display.getHeight() * Display.getPixelScaleFactor()) != graphics.config.height) {
                        graphics.resize = false;
                        graphics.config.width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
                        graphics.config.height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
                        Gdx.gl.glViewport( 0, 0, graphics.config.width, graphics.config.height );
                        if (listener != null) listener.resize( graphics.config.width, graphics.config.height );
                        shouldRender = true;
                    }
                }

                if (executeRunnables()) shouldRender = true;

                // If one of the runnables set running to false, for example after an exit().
                if (!running) break;

                input.update();
                if (graphics.shouldRender()) shouldRender = true;
                input.processEvents();

                // Use the active audio engine
                updateAudio();

                if (isMinimized)
                    shouldRender = false;
                else if (isBackground && graphics.config.backgroundFPS == -1) //
                    shouldRender = false;

                int frameRate = isBackground ? graphics.config.backgroundFPS : graphics.config.foregroundFPS;
                if (shouldRender) {
                    graphics.updateTime();
                    graphics.frameId++;
                    listener.render();
                    try {
                        Display.update( false );
                    } catch (Exception e) {
                        // wmw2: This can cause crashes in debugging mode if we do not do this
                        Gdx.app.log( "OpenGL", e.getMessage() );
                    }
                } else {
                    // Sleeps to avoid wasting CPU in an empty loop.
                    if (frameRate == -1) frameRate = 10;
                    if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
                    if (frameRate == 0) frameRate = 30;
                }
                if (frameRate > 0) Display.sync( frameRate );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cleanup();
        if (graphics.config.forceExit) System.exit(0);
    }

    public boolean executeRunnables () {
        synchronized (runnables) {
            for (int i = runnables.size - 1; i >= 0; i--)
                executedRunnables.add(runnables.get(i));
            runnables.clear();
        }
        if (executedRunnables.size == 0) return false;
        do
            executedRunnables.pop().run();
        while (executedRunnables.size > 0);
        return true;
    }

    public void cleanup() {
        if (shutdown) {
            return;
        }

        shutdown=true;
        synchronized (lifecycleListeners) {
            LifecycleListener[] listeners = lifecycleListeners.begin();
            for (int i = 0, n = lifecycleListeners.size; i < n; ++i) {
                listeners[i].pause();
                listeners[i].dispose();
            }
            lifecycleListeners.end();
        }
        listener.pause();
        listener.dispose();
        disposeResources();
        disposeAudio();
        Display.destroy();

    }

    @Override
    public ApplicationListener getApplicationListener () {
        return listener;
    }

    @Override
    public Audio getAudio () {
        return audio;
    }

    @Override
    public Files getFiles () {
        return files;
    }

    @Override
    public LwjglGraphics getGraphics () {
        return graphics;
    }

    @Override
    public Input getInput () {
        return input;
    }

    @Override
    public Net getNet () {
        return net;
    }

    @Override
    public ApplicationType getType () {
        return ApplicationType.Desktop;
    }

    @Override
    public int getVersion () {
        return 0;
    }

    public void stop () {
        running = false;
        try {
            mainLoopThread.join();
        } catch (Exception ex) {
        }
    }

    @Override
    public long getJavaHeap () {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap () {
        return getJavaHeap();
    }

    ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();

    @Override
    public Preferences getPreferences (String name) {
        if (preferences.containsKey(name)) {
            return preferences.get(name);
        } else {
            Preferences prefs = new LwjglPreferences(new LwjglFileHandle(new File(preferencesdir, name), preferencesFileType));
            preferences.put(name, prefs);
            return prefs;
        }
    }

    protected Files createFiles() {
        return new LwjglFiles();
    }

    public LwjglInput createInput(LwjglApplicationConfiguration config) {
        return new DefaultLwjglInput();
    }
    @Override
    public Clipboard getClipboard () {
        return new LwjglClipboard();
    }

    @Override
    public void postRunnable (Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    @Override
    public void debug (String tag, String message) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
    }

    @Override
    public void debug (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
    }

    @Override
    public void log (String tag, String message) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
    }

    @Override
    public void log (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
    }

    @Override
    public void error (String tag, String message) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
    }

    @Override
    public void error (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
    }

    @Override
    public void setLogLevel (int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel () {
        return logLevel;
    }

    @Override
    public void setApplicationLogger (ApplicationLogger applicationLogger) {
        this.applicationLogger = applicationLogger;
    }

    @Override
    public ApplicationLogger getApplicationLogger () {
        return applicationLogger;
    }

    @Override
    public void exit () {
        postRunnable(new Runnable() {
            @Override
            public void run () {
                running = false;
            }
        });
    }

    @Override
    public void addLifecycleListener (LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener (LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }
}
