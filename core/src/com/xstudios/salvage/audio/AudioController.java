package com.xstudios.salvage.audio;

import com.badlogic.gdx.Gdx;
import com.xstudios.salvage.game.InputController;
import de.pottgames.tuningfork.*;


public class AudioController {

    /**
     * The singleton instance of the input controller
     */
    private static AudioController theController = null;

    private Audio audio;
    private BufferedSoundSource heartbeat;
    private BufferedSoundSource oxygen_alarm;
    private SoundBuffer wall_collision;
    private SoundBuffer wood_collision;
    private StreamedSoundSource music;
    private StreamedSoundSource bubbles;
    private SoundBuffer background_roar;
    private int ticks;

    private float sound_effects_volume;
    private float music_volume;

    public AudioController(float se, float m){
        sound_effects_volume = se/4;
        music_volume = m/4;
        audio = Audio.init();
        SoundBuffer heartbeat_wav = WaveLoader.load(Gdx.files.internal("audio/heartbeat.wav"));
        SoundBuffer oxygen_alarm_wav = WaveLoader.load(Gdx.files.internal("audio/oxygen_alarm.wav"));
        music = new StreamedSoundSource(Gdx.files.internal("audio/background_music.ogg"));
        bubbles = new StreamedSoundSource(Gdx.files.internal("audio/bubbles.ogg"));
        background_roar = WaveLoader.load(Gdx.files.internal("audio/suble_roar.wav"));
        wall_collision = WaveLoader.load(Gdx.files.internal("audio/wall_collision.wav"));
        wood_collision = WaveLoader.load(Gdx.files.internal("audio/wood_collision.wav"));
        oxygen_alarm = audio.obtainSource(oxygen_alarm_wav);
        heartbeat = audio.obtainSource(heartbeat_wav);
        music.setLooping(true);
        heartbeat.setLooping(true);
        bubbles.setLooping(true);
        oxygen_alarm.setLooping(true);
        music.setVolume(0.4f * music_volume);
        bubbles.setVolume(0.4f * sound_effects_volume);
        oxygen_alarm.setVolume(0.4f * sound_effects_volume);
        ticks = 0;
    }

    public static AudioController getInstance(float se, float m) {
        if (theController == null) {
            theController = new AudioController(se, m);
        }
        return theController;
    }

    public void initialize() {
        music.play();
        bubbles.play();
        heartbeat.play();
        heartbeat.setVolume(0.0f);
    }

    public void setMusic(float v){
        music_volume = v/4;
        music.setVolume(music_volume * 0.4f);
    }

    public void setSoundEffects(float s){
        sound_effects_volume = s/4;
        bubbles.setVolume(sound_effects_volume * 0.4f);
        oxygen_alarm.setVolume(sound_effects_volume * 0.4f);
    }

    public void update(float oxygen, float max_oxygen) {

        ticks++;

        float volume = (max_oxygen - oxygen) / max_oxygen;
        heartbeat.setVolume(volume - 0.1f);

        if (((oxygen / max_oxygen) < 0.25f) && !oxygen_alarm.isPlaying()) {
            float oxygen_volume = 0.4f + ((25.0f - oxygen) / 50.0f);
            oxygen_alarm.setVolume(oxygen_volume);
            oxygen_alarm.play();

        }
    }

    public void wall_collision(float force) {
        //float volume = (force)/20.f;
        audio.play(wall_collision, 0.5f * sound_effects_volume);
    }


    public void wood_collision(float force) {
        //float volume = (force)/20.f;
        audio.play(wood_collision, 0.05f * sound_effects_volume);
    }

    public void roar() {
        double rand = Math.random();
        float roar_volume = (float) (0.8);
        audio.play(background_roar, roar_volume * sound_effects_volume);
    }

    public void reset() {
        oxygen_alarm.stop();
        heartbeat.stop();
    }

    public void dispose() {
        music.dispose();
        audio.dispose();
    }

}
