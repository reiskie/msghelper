package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.media.VolumeShaper;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.Toast;

import me.leolin.shortcutbadger.ShortcutBadger;

// 原来的 MainActivity, refactor为 ActivityTry
public class ActivityTry extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ActivityTry";
    private ReceiverDyn receiverDyn;

    private boolean isBinded = false;
    private ServiceConnection fgServiceConn = new ServiceConnection(){
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"ServiceConnection:onServiceDisconnected: from fgService ");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG,"ServiceConnection:onServiceConnected: to fgService");
            isBinded = true;
        }
    };

    SoundPool mSoundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try);

        // try 动态注册receiver for MsgItem, not work，短信来时，无法收到广播（02-16）
        //                              work. 在主活动添加运行时权限RECEIVE_SMS后 (02-18)
        // try 动态注册receiver for 自定义广播，work
        // try 动态注册receiver for 飞行模式，work
        IntentFilter interFilter = new IntentFilter();
        interFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        interFilter.addAction("com.yxh.msghelper.normal_broadcast_dyn");
        interFilter.addAction("android.intent.action.AIRPLANE_MODE");
        receiverDyn = new ActivityTry.ReceiverDyn();
        registerReceiver(receiverDyn, interFilter); // 动态注册receiver

        Button button1 = findViewById(R.id.button_1);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(ActivityTry.this,"hello",Toast.LENGTH_LONG).show();
            }
        });

        Button button2 = findViewById(R.id.button_finish);
        button2.setOnClickListener(this);

        findViewById(R.id.btn_send_broadcast_1).setOnClickListener(this);
        findViewById(R.id.btn_send_broadcast_2).setOnClickListener(this);
        findViewById(R.id.btn_svc_start).setOnClickListener(this);
        findViewById(R.id.btn_svc_stop).setOnClickListener(this);
        findViewById(R.id.btn_svc_bind).setOnClickListener(this);
        findViewById(R.id.btn_svc_unbind).setOnClickListener(this);
        findViewById(R.id.btn_clear_sms).setOnClickListener(this);
        findViewById(R.id.btn_save_sms).setOnClickListener(this);
        findViewById(R.id.btn_read_sms).setOnClickListener(this);
        findViewById(R.id.btn_badge).setOnClickListener(this);
        findViewById(R.id.btn_snd).setOnClickListener(this);

        initSound();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "您拒绝授予此应用权限，无法完成正常功能",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v){
        Intent in;
        switch (v.getId()){
            case R.id.button_finish:
                finish();
                break;
            case R.id.btn_send_broadcast_1:
                // 自定义广播，准备发给动态注册的broadcast receiver
                in = new Intent("com.yxh.msghelper.normal_broadcast_dyn");
                // 全局广播（此外还有 本地广播）
                sendBroadcast(in); // 发送标准（normal）广播
                //sendOrderedBroadcast(intent,null); // 发送有序（ordered）广播
                break;
            case R.id.btn_send_broadcast_2:
                Log.i(TAG,"onClick:btn_send_broadcast_2 send selfdef static broadcast " );
                in = new Intent("com.yxh.msghelper.normal_broadcast_static");
                sendBroadcast(in);
                break;
            case R.id.btn_svc_start:
                in=new Intent(this, FgService.class);
                startService(in);
                break;
            case R.id.btn_svc_stop:
                in=new Intent(this, FgService.class);
                stopService(in);
                break;
            case R.id.btn_svc_bind:
                in=new Intent(this, FgService.class);
                bindService(in, fgServiceConn, BIND_AUTO_CREATE);
                break;
            case R.id.btn_svc_unbind:
                Log.i(TAG,"onClick:unbind fgServiceConn = " + this);
                if (isBinded){
                    // 连续调用unbindService第二次，就会崩溃
                    unbindService(fgServiceConn);
                    isBinded = false;
                }
                break;
            case R.id.btn_clear_sms:
                Log.i(TAG,"onClick:clear_sms " );
                DataAccess.clearMSGFromDB();
                break;
            case R.id.btn_save_sms:
                Log.i(TAG,"onClick:save_sms " );
                DataAccess.copySMSFromInboxToDB();
                break;
            case R.id.btn_read_sms:
                Log.i(TAG,"onClick:read_sms" );
                DataAccess dataAccess =
                        new DataAccess(0, null, 0,0,0) ;
                dataAccess.getMsgfromDB(true);
                dataAccess.aggregateMsgfromDB("address");
                break;
            case R.id.btn_badge:
                Log.i(TAG,"onClick:btn_badge, isBadgeCounterSupported = " + ShortcutBadger.isBadgeCounterSupported(this) );
                ShortcutBadger.applyCount(this, (int)(Math.random()*100));

                break;
            case R.id.btn_snd:
                playSound(R.raw.snd1);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy: executed");

        // 观察：即使没有这句，主活动退出后，FgService.onUnbind也被触发
        //unbindService(fgServiceConn);

        // 观察：如果没有这句，主活动退出后，FgService仍然运行("开发者选项->正在运行的服务"中可见)
        //Intent in2=new Intent(this, FgService.class);
        //stopService(in2);

        unregisterReceiver(receiverDyn); // 动态注册的receiver需要unregister

        mSoundPool.release();
    }

    class ReceiverDyn extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"ReceiverDyn:onReceive: intent.getAction()="+intent.getAction());
            Toast.makeText(context,
                    "ReceiverDyn:onReceive intent.getAction()="+intent.getAction(),
                    Toast.LENGTH_LONG).show();

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.e(TAG, "ReceiverDyn:飞行模式状态 1为开启状态，0为关闭状态 airState=="
                        + bundle.getBoolean("state"));
            }else{
                Log.e(TAG, "ReceiverDyn:bundle is null");
            }

            //abortBroadcast(); // 截断有序广播
        }
    }

    private void initSound(){

        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频的数量
            builder.setMaxStreams(3);
            //AudioAttributes是一个封装音频各种属性的类
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();
        } else {
            //第一个参数是可以支持的声音数量，第二个是声音类型，第三个是声音品质
            mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 5);
        }


    }

    public void playSound(int resId) {

        mSoundPool.load(this, resId, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                //当前音量
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // 设置为最大值
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                //第一个参数id，即传入池中的顺序，第二个和第三个参数为左右声道，第四个参数为优先级，第五个是否循环播放，0不循环，-1循环
                //最后一个参数播放比率，范围0.5到2，通常为1表示正常播放
                soundPool.play(1, 1, 1, 0, 0, 1);
                // 恢复原值, 播放完成时才恢复，如何知道？
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);

            }
        });

        //soundPool.release();
    }
}
