package com.xstudios.salvage.audio;

import com.badlogic.gdx.Gdx;
import de.pottgames.tuningfork.*;


public class AudioController {

    private Audio audio;
    private BufferedSoundSource heartbeat;
    private BufferedSoundSource oxygen_alarm;
    private SoundBuffer wall_collision;
    private StreamedSoundSource music;
    private StreamedSoundSource bubbles;
    private SoundBuffer background_roar;
    private float max_oxygen;
    private int ticks;

    public AudioController(float initial_oxygen){
        audio = Audio.init();
        SoundBuffer heartbeat_wav = WaveLoader.load(Gdx.files.internal("audio/heartbeat.wav"));
        SoundBuffer oxygen_alarm_wav = WaveLoader.load(Gdx.files.internal("audio/oxygen_alarm.wav"));
        music = new StreamedSoundSource(Gdx.files.internal("audio/background_music.ogg"));
        bubbles = new StreamedSoundSource(Gdx.files.internal("audio/bubbles.ogg"));
        background_roar = WaveLoader.load(Gdx.files.internal("audio/suble_roar.wav"));
        wall_collision = WaveLoader.load(Gdx.files.internal("audio/wall_collision.wav"));
        oxygen_alarm = audio.obtainSource(oxygen_alarm_wav);
        heartbeat = audio.obtainSource(heartbeat_wav);
        music.setLooping(true);
        heartbeat.setLooping(true);
        bubbles.setLooping(true);
        oxygen_alarm.setLooping(true);
        music.setVolume(0.2f);
        bubbles.setVolume(0.4f);
        oxygen_alarm.setVolume(0.4f);
        max_oxygen = initial_oxygen;
        ticks = 0;
    }

    public void intialize(){
        music.play();
        bubbles.play();
        heartbeat.play();
        heartbeat.setVolume(0.0f);
    }

    public void update(float oxygen){
        if (ticks > 750){
            double rand = Math.random();
            if (rand > 0.5){
                float roar_volume = (float)(1.15 - rand);
                audio.play(background_roar, roar_volume);
            }
            ticks = 0;
        }
        ticks++;

        float volume = (max_oxygen-oxygen)/max_oxygen;
        heartbeat.setVolume(volume-0.1f);

        if (((oxygen/max_oxygen) < 0.25f) && !oxygen_alarm.isPlaying()){
            float oxygen_volume = 0.4f + ((25.0f - oxygen)/50.0f);
            oxygen_alarm.setVolume(oxygen_volume);
            oxygen_alarm.play();
        }
    }

    public void wall_collision(float force){
        //float volume = (force)/20.f;
        audio.play(wall_collision, 0.5f);
    }

    public void dispose(){
        music.dispose();
        audio.dispose();
    }

}
