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
    private BufferedSoundSource attack_roar;
    private BufferedSoundSource idle_roar_high;
    private BufferedSoundSource idle_roar_low;
    private BufferedSoundSource loud_roar;
    private BufferedSoundSource alarm;
    private SoundBuffer wall_collision;
    private SoundBuffer wood_collision;


    private StreamedSoundSource music;
    private StreamedSoundSource bubbles;
    private int ticks;
    private int last_playing_tick;
    private int time_apart;

    private float volume_tick;

    private float sound_effects_volume;
    private float music_volume;

    public AudioController(float se, float m){
        sound_effects_volume = se/4;
        music_volume = m/4;
        audio = Audio.init();
        SoundBuffer heartbeat_wav = WaveLoader.load(Gdx.files.internal("audio/heartbeat.wav"));
        SoundBuffer oxygen_alarm_wav = WaveLoader.load(Gdx.files.internal("audio/oxygen_alarm.wav"));
        music = new StreamedSoundSource(Gdx.files.internal("audio/background.ogg"));
        bubbles = new StreamedSoundSource(Gdx.files.internal("audio/bubbles.ogg"));
        SoundBuffer attack_roar_wav = WaveLoader.load(Gdx.files.internal("audio/attack_roar.wav"));
        SoundBuffer loud_roar_wav = WaveLoader.load(Gdx.files.internal("audio/loud_roar.wav"));
        SoundBuffer idle_roar_high_wav = WaveLoader.load(Gdx.files.internal("audio/higher_growl.wav"));;
        SoundBuffer idle_roar_low_wav = WaveLoader.load(Gdx.files.internal("audio/lower_growl.wav"));
        SoundBuffer alarm_wav = WaveLoader.load(Gdx.files.internal("audio/alarm.wav"));
        wall_collision = WaveLoader.load(Gdx.files.internal("audio/wall_collision.wav"));
        wood_collision = WaveLoader.load(Gdx.files.internal("audio/wood_collision.wav"));
        alarm = audio.obtainSource(alarm_wav);
        oxygen_alarm = audio.obtainSource(alarm_wav);
        heartbeat = audio.obtainSource(heartbeat_wav);
        idle_roar_high = audio.obtainSource(idle_roar_high_wav);
        idle_roar_low = audio.obtainSource(idle_roar_low_wav);
        attack_roar = audio.obtainSource(attack_roar_wav);
        loud_roar = audio.obtainSource(loud_roar_wav);
        music.setLooping(true);
        heartbeat.setLooping(false);
        loud_roar.setLooping(false);
        idle_roar_low.setLooping(false);
        idle_roar_high.setLooping(false);
        attack_roar.setLooping(false);
        bubbles.setLooping(true);
        alarm.setLooping(true);
        music.setVolume(0.3f);
        bubbles.setVolume(0.4f);
        alarm.setVolume(0.4f);
        music.setVolume(0.4f * music_volume);
        bubbles.setVolume(0.4f * sound_effects_volume);
        ticks = 0;
        time_apart = 400;
        volume_tick = 0.0f;
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
        time_apart = (int)((1-volume) * 200);
        System.out.println(time_apart);

        if (!heartbeat.isPlaying()){
            if (ticks > last_playing_tick + time_apart){
                heartbeat.play();
            }
        }
        else {
            last_playing_tick = ticks;
        }

    }

    public void wall_collision(float force) {
        //float volume = (force)/20.f;
        audio.play(wall_collision, 0.5f * sound_effects_volume);
    }


    public void wood_collision(float force) {
        //float volume = (force)/20.f;
        audio.play(wood_collision, 0.3f * sound_effects_volume);

    }

    public void chase() {
        //float volume = (force)/20.f;
        music.play();
        alarm.play();
    }

    public void idle_roar() {
        double rand = Math.random();
        float roar_volume = (float) (0.2);
        idle_roar_low.setVolume(roar_volume);
        idle_roar_high.setVolume(roar_volume);
            if (rand > 0.5) {
                idle_roar_high.stop();
                idle_roar_low.play();
            } else {
                idle_roar_low.stop();
                idle_roar_high.play();
            }
    }

    public void reset() {
        alarm.stop();
        heartbeat.stop();
        ticks = 0;
    }

    public void dispose() {
        music.dispose();
        audio.dispose();
    }

    public void attack_roar() {
        if (!attack_roar.isPlaying()){
            attack_roar.play();
        }
    }


    public void loud_roar_play(boolean hasRoared) {
        if (!hasRoared) {
            music.stop();
            heartbeat.stop();
            attack_roar.stop();
            idle_roar_low.stop();
            idle_roar_high.stop();
            loud_roar.setVolume(1.0f);
            loud_roar.play();
        }
    }

    public void dying() {
        attack_roar.stop();
        idle_roar_low.stop();
        idle_roar_high.stop();
        music.setVolume(0.2f - volume_tick);
        music.setVolume(0.2f - volume_tick);
        volume_tick -= 0.01f;
        time_apart += 10;
    }

    public boolean is_loud_roaring() {
        return loud_roar.isPlaying();
    }


}

