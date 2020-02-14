package com.yxh.msghelper;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FgService extends Service {

    FgBinder mBinder = new FgBinder();

    class FgBinder extends Binder {

        public void doSomeThing(){
            Log.i("FgService", "in do something.");
        }
    }

    public FgService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i("FgService", "in onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("fgService","in onCreate");
        Intent intent1 = new Intent(this, MainActivity.class);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("fgService","in onStratCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("fgService","in onDestroy");

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("fgService","in onUnbind");

        return super.onUnbind(intent);
    }
}
