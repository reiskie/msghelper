package com.yxh.msghelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

public class UtilSound {
    private static final String TAG = "UtilSound";

    private static SoundPool mSoundPool;
    private static boolean isSoundLoaded;
    private static boolean isMute;
    private static AudioManager mAudioManager;
    private static int mVolumnSound;
    private static int mVolumnRing;
    private static Ringtone mRing;
    private static Uri mUri;


    public static void cleanup() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
            isSoundLoaded = false;
        }
    }

    public static boolean toggleMute() {
        isMute = !isMute;
        return isMute;
    }

    private static boolean isTimeOKToSound() {
        Log.i(TAG, "checkTimeForSound: executed.");
        boolean res = true;
        SimpleDateFormat df = new SimpleDateFormat("HHmm");
        int nowtime = Integer.parseInt(df.format(new Date()));
        if (nowtime > 2300 || nowtime < 800) {
            res = false;
        }
        return res;
    }

    public static void initSound() {
        Log.i(TAG, "initSound() executed.");

        if (isSoundLoaded) {
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
        //mSoundPool.load(mRingUri.getPath(), 1); // this not work
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isSoundLoaded = true;
            }
        });

        // init Ringtone/MediaPlayer
        String uristr = pref.getString("ringuri", "");
        if ("".equals(uristr)) {
            // defaultUri = content://settings/system/notification_sound
            mUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            mUri = Uri.parse(uristr);
        }
        if (mUri == null) {
            mUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        mRing = RingtoneManager.getRingtone(MsgApp.getContext(), mUri);
//        mPlayer = MediaPlayer.create(MsgApp.getContext(), mUri);
//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer arg0) {
//                //player.release();
//                mPlayer.seekTo(0);
//                Log.i(TAG, "player seekTo 0");
//            }
//        });
    }

    public static String getRingTitle() {
        String title = "";
        if (mRing != null) {
            title = mRing.getTitle(MsgApp.getContext());
        }
        return title;
    }

    public static Uri getRingUri() {
        return mUri;
    }

    public static void setRingUri(Uri ringUri) {
        // Non-default uri = content://media/internal/audio/media/49
        mUri = ringUri;
        mRing = RingtoneManager.getRingtone(MsgApp.getContext(), mUri);
    }

    public static void setVolumnBySetting(int volumnSetting) {
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumnSound = (int) (volumnSetting * maxVolume / 100);

        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mVolumnRing = (int) (volumnSetting * maxVolume / 100);
    }

    public static void playRing() {
        Log.i(TAG, "playRing() mVolumnRing = " + Integer.valueOf(mVolumnRing));

        int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mVolumnRing, 0);

        if (mRing.isPlaying()) {
            mRing.stop();
        }
        mRing.play();

        try {
            sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, currentVolumn, 0);

    }

    public static void playSound() {
        //AudioManager audioManager = (AudioManager) MsgApp.getContext().getSystemService(Context.AUDIO_SERVICE);
        //当前音量
        int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //设置为最大值
        //int maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumn, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumnSound, 0);
        //第一个参数id，即传入池中的顺序，第二个和第三个参数为左右声道，第四个参数为优先级
        //第五个是否循环播放，0不循环，-1循环
        //第六个参数为播放比率，范围0.5到2，通常为1表示正常播放
        mSoundPool.play(1, 1, 1, 0, 0, 1);

        try {
            sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 恢复原值, 播放完成时才恢复，如何知道？A: 无法知道
        // 可以通过MediaPlayer获取duration，然后按时间等待
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumn, 0);

    }

    public static void play(boolean isCheckCondition) {

        Log.i(TAG, "playSound(): mSoundPool=" + mSoundPool + ", isSoundLoaded=" + isSoundLoaded);

        if (mSoundPool == null || mRing == null || !isSoundLoaded) {
            return;
        }

        if (isCheckCondition && (isMute || !isTimeOKToSound())) {
            return;
        }

        // 待优化 避免集中并发多次播放声音
        // 提供开关，关闭声音播放
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (MsgApp.getFlagUser() == MsgApp.FLAG_USER_SELF){
                    playSound();
                }else{
                    playRing();
                }

            }
        }).start();
    }

}

