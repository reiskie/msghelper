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

        // 02-16 not work，重启后没进来
        // 02-18 work. 不确定是否与在主活动添加运行时权限RECEIVE_BOOT_COMPLETED有关
        Log.i(TAG,"onReceive: executed.");
        //成功打出日志后，也看不到toast消息
        Toast.makeText(context, "Boot Complete", Toast.LENGTH_LONG).show();

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            //开机启动一个活动
            //02-18 成功，开机后大概30秒启动活动成功
            Intent i = new Intent(context, MsgGroupActivity.class);
            i.putExtra("howtostart","BOOT_COMPLETED");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }
}
