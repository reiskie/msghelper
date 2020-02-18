package com.yxh.msghelper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class FgService extends Service {
    private static final String TAG = "FgService";

    FgBinder mBinder = new FgBinder();

    class FgBinder extends Binder {

        public void doSomeThing(){
            Log.i(TAG, "FgBinder:doSomeThing: executed.");
        }

        public int getStatus(){
            int status=0; // 0-空闲，1-忙碌（读短信，写入自己的库）

            return status;
        }
    }

    public FgService() {
    }

    private NotificationManager getNotiManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i(TAG, "onBind: executed");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG,"onRebind: executed.");

        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate: executed.");
        super.onCreate();

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            //只在Android O之上需要channel
            NotificationChannel notificationChannel =
                    new NotificationChannel("chnl_id","MsgHelper",NotificationManager.IMPORTANCE_HIGH);
            getNotiManager().createNotificationChannel(notificationChannel);
        }

        Intent intent1 = new Intent(this, ActivityTry.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent1,0);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,"chnl_id");
        builder.setContentTitle("标题");
        builder.setContentText("正文");
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        startForeground(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStratCommand: executed.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy: executed.");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Boolean res = super.onUnbind(intent);
        Log.i(TAG,"onUnbind: executed.res="+res);
        return res;
        // 返回ture/false影响下一次:
        // -- bindService()会不会触发onBind()
        // -- unbindService()会不会触发onUnBind()
        // 前提是两次中间service没有被销毁、重建
    }
}
