package com.xstudios.salvage.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.xstudios.salvage.assets.AssetDirectory;
import de.pottgames.tuningfork.Audio;
import de.pottgames.tuningfork.SoundBuffer;
import de.pottgames.tuningfork.SoundBufferLoader;
import de.pottgames.tuningfork.BufferedSoundSource;
import de.pottgames.tuningfork.WaveLoader;


public class AudioController {

    private Audio audio;
    private BufferedSoundSource background;
    private SoundBuffer heartbeat;
    private AssetDirectory assetDirectory;

    public AudioController(){
        audio = Audio.init();
        //SoundBuffer background_wav = assetDirectory.get("background_music.wav", SoundBuffer.class);
        SoundBuffer background_wav = WaveLoader.load(Gdx.files.internal("../../core/assets/audio/background_music.wav"));
        background = audio.obtainSource(background_wav);
        background.setLooping(true);
    }

    public void intialize(){
        background.play();
    }

}
