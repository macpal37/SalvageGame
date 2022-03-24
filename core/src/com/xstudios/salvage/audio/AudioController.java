package com.xstudios.salvage.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.xstudios.salvage.assets.AssetDirectory;
import de.pottgames.tuningfork.*;


public class AudioController {

    private Audio audio;
    private BufferedSoundSource heartbeat;
    private SoundBuffer wall_collision;
    private StreamedSoundSource background;
    private AssetDirectory assetDirectory;
    private float max_oxygen;

    public AudioController(float initial_oxygen){
        audio = Audio.init();
        SoundBuffer heartbeat_wav = WaveLoader.load(Gdx.files.internal("audio/heartbeat.wav"));
        wall_collision = WaveLoader.load(Gdx.files.internal("audio/wall_collision.wav"));
        background = new StreamedSoundSource(Gdx.files.internal("audio/background_music.ogg"));
        heartbeat = audio.obtainSource(heartbeat_wav);
        background.setLooping(true);
        heartbeat.setLooping(true);
        background.setVolume(0.5f);
        max_oxygen = initial_oxygen;
    }

    public void intialize(){
        background.play();
        heartbeat.play();
        heartbeat.setVolume(0.0f);
    }

    public void update(float oxygen){
        float volume = (max_oxygen-oxygen)/max_oxygen;
        heartbeat.setVolume(volume+0.1f);
    }

    public void wall_collision(float force){
        float volume = (force)/20.f;
        audio.play(wall_collision, volume);
    }

    public void dispose(){
        background.dispose();
        audio.dispose();
    }

}
