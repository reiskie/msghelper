package com.yxh.msghelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class UtilSound {
    private static final String TAG = "UtilSound";

    private static SoundPool mSoundPool;
    private static boolean isSoundLoaded;
    private static boolean isMute;
    private static AudioManager mAudioManager;
    private static int mVolumn;

    public static void cleanup(){
        if (mSoundPool != null){
            mSoundPool.release();
            mSoundPool=null;
            isSoundLoaded = false;
        }
    }

    public static boolean toggleMute(){
        isMute = !isMute;
        return isMute;
    }

    private static boolean isTimeOKToSound(){
        Log.i(TAG,"checkTimeForSound: executed.");
        boolean res = true;
        SimpleDateFormat df = new SimpleDateFormat("HHmm");
        int nowtime = Integer.parseInt(df.format(new Date()));
        if (nowtime > 2300 || nowtime < 800){
            res = false;
        }
        return res;
    }

    public static void initSound(){
        Log.i(TAG,"initSound() executed.");

        if (isSoundLoaded){
            return;
        }

        mAudioManager = (AudioManager) MsgApp.getContext().getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences pref = MsgApp.getContext().getSharedPreferences("settings", MODE_PRIVATE);
        int volumnSetting = pref.getInt("volumn", 50);
        setVolumnBySetting(volumnSetting);

        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(attrBuilder.build());
            builder.setMaxStreams(3); //传入音频的最大数量
            mSoundPool = builder.build();
        } else {
            //第一个参数是可以支持的声音数量，第二个是声音类型，第三个是声音品质
            mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 5);
        }

        mSoundPool.load(MsgApp.getContext(), R.raw.alarm2, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isSoundLoaded = true;
            }
        });
    }

    public static void setVolumnBySetting(int volumnSetting){
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumn = (int)(volumnSetting * maxVolume / 100);
    }

    public static void playSound(boolean isCheckCondition) {

        Log.i(TAG, "playSound(): mSoundPool="+mSoundPool+", isSoundLoaded="+isSoundLoaded);

        if (mSoundPool == null || !isSoundLoaded){
            return;
        }

        if (isCheckCondition && (isMute || !isTimeOKToSound())){
            return;
        }

        // 待优化 避免集中并发多次播放声音
        // 提供开关，关闭声音播放
        new Thread(new Runnable() {
            @Override
            public void run() {

                //AudioManager audioManager = (AudioManager) MsgApp.getContext().getSystemService(Context.AUDIO_SERVICE);
                //当前音量
                int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                //设置为最大值
                //int maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumn, 0);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumn, 0);
                //第一个参数id，即传入池中的顺序，第二个和第三个参数为左右声道，第四个参数为优先级
                //第五个是否循环播放，0不循环，-1循环
                //第六个参数为播放比率，范围0.5到2，通常为1表示正常播放
                mSoundPool.play(1, 1, 1, 0, 0, 1);

                try{
                    Thread.sleep(600);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                // 恢复原值, 播放完成时才恢复，如何知道？A: 无法知道
                // 可以通过MediaPlayer获取duration，然后按时间等待
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumn, 0);

            }
        }).start();
    }
}
