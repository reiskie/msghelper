package com.yxh.msghelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// 静态注册的receiver
public class ReceiverStaticBoot extends BroadcastReceiver {
    private static final String TAG = "ReceiverStaticBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        // not work，重启后没进来
        Log.i(TAG,"onReceive: executed.");
        Toast.makeText(context, "Boot Complete", Toast.LENGTH_LONG).show();

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            Intent i = new Intent(context, ActivityTry.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }
}
