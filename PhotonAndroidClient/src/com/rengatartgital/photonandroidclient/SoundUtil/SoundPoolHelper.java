package com.rengatartgital.photonandroidclient.SoundUtil;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPoolHelper extends SoundPool{
    private Set<Integer> mLoaded;
    private Context mContext;

    public SoundPoolHelper(int maxStreams, Context context) {
        this(maxStreams, AudioManager.STREAM_MUSIC, 0, context);
    }

    public SoundPoolHelper(int maxStreams, int streamType, int srcQuality, Context context) {
        super(maxStreams, streamType, srcQuality);
        mContext = context;
        mLoaded = new HashSet<Integer>();
        setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded.add(sampleId);
            }
        });
    }

    public void play(int soundID){
        AudioManager audioManager = (AudioManager) mContext.getSystemService( Context.AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        // Is the sound loaded already?
        if (mLoaded.contains(soundID)){
            play(soundID, volume, volume, 1, 0, 1f);
        }
    }
}
