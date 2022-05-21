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
    private BufferedSoundSource attack_roar;
    private BufferedSoundSource idle_roar;
    private BufferedSoundSource loud_roar;
    private BufferedSoundSource alarm;
    private BufferedSoundSource wood_collision;
    private BufferedSoundSource metal_collision;
    private BufferedSoundSource level_transmission;


    private StreamedSoundSource music;
    private StreamedSoundSource bubbles;
    private StreamedSoundSource loading_screen;
    private StreamedSoundSource white_noise;
    private int ticks;
    private int last_playing_tick;
    private int time_apart;

    private float volume_tick;

    private float sound_effects_volume;
    private float music_volume;

    public AudioController() {
        audio = Audio.init();
        sound_effects_volume = 1.0f;
        music_volume = 1.0f;
        SoundBuffer heartbeat_wav = WaveLoader.load(Gdx.files.internal("audio/heartbeat.wav"));
        music = new StreamedSoundSource(Gdx.files.internal("audio/background.ogg"));
        bubbles = new StreamedSoundSource(Gdx.files.internal("audio/bubbles.ogg"));
        loading_screen = new StreamedSoundSource(Gdx.files.internal("audio/loading_screen.ogg"));
        white_noise = new StreamedSoundSource (Gdx.files.internal ("audio/white_noise.ogg"));
        SoundBuffer attack_roar_wav = WaveLoader.load(Gdx.files.internal("audio/attack_roar.wav"));
        SoundBuffer loud_roar_wav = WaveLoader.load(Gdx.files.internal("audio/loud_roar.wav"));
        SoundBuffer idle_roar_wav = WaveLoader.load(Gdx.files.internal("audio/higher_growl.wav"));
        SoundBuffer alarm_wav = WaveLoader.load(Gdx.files.internal("audio/alarm.wav"));
        SoundBuffer wood_collision_wav = WaveLoader.load(Gdx.files.internal("audio/wood_collision.wav"));
        SoundBuffer metal_collision_wav = WaveLoader.load(Gdx.files.internal("audio/metal_collision.wav"));
        alarm = audio.obtainSource(alarm_wav);
        heartbeat = audio.obtainSource(heartbeat_wav);
        idle_roar = audio.obtainSource(idle_roar_wav);
        attack_roar = audio.obtainSource(attack_roar_wav);
        loud_roar = audio.obtainSource(loud_roar_wav);
        wood_collision = audio.obtainSource(wood_collision_wav);
        metal_collision = audio.obtainSource(metal_collision_wav);
        music.setLooping(true);
        white_noise.setLooping(true);
        heartbeat.setLooping(false);
        loud_roar.setLooping(false);
        idle_roar.setLooping(false);
        attack_roar.setLooping(false);
        bubbles.setLooping(true);
        loading_screen.setLooping(false);

        alarm.setLooping(true);
        white_noise.setVolume(sound_effects_volume * 0.05f);
        music.setVolume(0.07f * music_volume);
        set_sound_effect_volume(sound_effects_volume);
        ticks = 0;
        time_apart = 400;
        volume_tick = 0.0f;
    }

    public void setUp(float m, float se){
        music_volume = m;
        sound_effects_volume = se;
    }

    public static AudioController getInstance() {
        if (theController == null) {
            theController = new AudioController();
        }
        return theController;
    }

    public void initialize() {
//        music.setVolume(0.4f * music_volume);
//        set_sound_effect_volume(sound_effects_volume);
        white_noise.play();
    }

    public void start_level(int level) {
        //music.setVolume(music_volume * 0.5f);
        setMusic(music_volume);
        music.play();
        bubbles.play();
        heartbeat.play();
        heartbeat.setVolume(0.0f);
        white_noise.setVolume(0.0f);
        last_playing_tick = 0;
        ticks = 0;
        set_sound_effect_volume(sound_effects_volume);

        //levels
        if (level == 0 || level == 4 || level == 5) {
            SoundBuffer level_transmission_wav = WaveLoader.load(Gdx.files.internal("audio/levels/" + level +".wav"));
            level_transmission = audio.obtainSource(level_transmission_wav);
            //level_transmission.setVolume(0.35f);
            set_sound_effect_volume(sound_effects_volume);
            level_transmission.play();
            level_transmission.setLooping(false);
        }
    }

    public void setMusic(float v) {
        music_volume = v;
        music.setVolume(music_volume * 0.07f);
    }

    public void update(float oxygen, float max_oxygen) {

        ticks++;

        float volume = (max_oxygen - oxygen) / max_oxygen;
        heartbeat.setVolume(sound_effects_volume * (volume - 0.1f) * 0.75f);
        time_apart = (int) ((1 - volume) * 200);

        if (!heartbeat.isPlaying()) {
            if (ticks > last_playing_tick + time_apart) {
                heartbeat.play();
            }
        } else {
            last_playing_tick = ticks;
        }

    }


    public void wood_collision(float force) {
        if (!wood_collision.isPlaying()){
            wood_collision.setVolume(sound_effects_volume * 0.1f);
            wood_collision.play();
        }
    }

    public void metal_collision(float force) {
        if (!metal_collision.isPlaying()){
            metal_collision.setVolume(sound_effects_volume * 0.1f);
            metal_collision.play();
        }
    }

    public void chase() {
        music.play();
        alarm.play();
    }

    public void idle_roar() {
        if (idle_roar.getPlaybackPosition() > idle_roar.getDuration() || idle_roar.getPlaybackPosition() == 0.0f ){
            idle_roar.play();
        }
    }

    public void reset() {
        alarm.stop();
        heartbeat.stop();
        loud_roar.stop();
        attack_roar.stop();
        idle_roar.stop();
        alarm.stop();
        bubbles.stop();
        music.stop();
        if (level_transmission != null){
            level_transmission.stop();
        }
        white_noise.setVolume(sound_effects_volume * 0.05f);
        ticks = 0;
    }

    public void dispose() {
        music.dispose();
        audio.dispose();
    }

    public void loading_screen() {
        loading_screen.setVolume(sound_effects_volume * 0.5f);
        loading_screen.play();
    }

    public float loading_screen_length() {
        return loading_screen.getDuration() - 6.3f;
    }

    public float loading_screen_progress() {
        return loading_screen.getPlaybackPosition();
    }

    public Boolean is_loading() {
        return loading_screen.isPlaying();
    }


    public void attack_roar() {
        if (!attack_roar.isPlaying()) {
            attack_roar.play();
        }
    }


    public void loud_roar_play(boolean hasRoared) {
        if (!hasRoared) {
            music.stop();
            heartbeat.stop();
            attack_roar.stop();
            idle_roar.stop();
            loud_roar.play();
        }
        else {
            music.play();
            heartbeat.play();
            attack_roar.play();
            bubbles.play();
        }
    }

    public void dying() {
        attack_roar.stop();
        idle_roar.stop();
        alarm.stop();
        bubbles.stop();
        music.stop();
        white_noise.setVolume(sound_effects_volume * 0.05f);
        volume_tick -= 0.01f;
        time_apart += 10;
    }

    public boolean is_loud_roaring() {
        return loud_roar.isPlaying();
    }

    public void set_sound_effect_volume(float volume){
        sound_effects_volume = volume;
        alarm.setVolume(sound_effects_volume * 0.05f);
        attack_roar.setVolume(sound_effects_volume * 0.75f);
        idle_roar.setVolume(sound_effects_volume * 0.05f);
        loud_roar.setVolume(sound_effects_volume * 0.5f);
        bubbles.setVolume(sound_effects_volume * 0.2f);
        white_noise.setVolume(sound_effects_volume * 0.07f);
        if (level_transmission != null){
            level_transmission.setVolume(sound_effects_volume * 0.15f);
        }
    }

}

