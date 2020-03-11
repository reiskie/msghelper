package com.yxh.msghelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

// 静态注册的receiver
public class ReceiverStaticSMS extends BroadcastReceiver {
    private static final String TAG = "ReceiverStaticSMS";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        // 02-16, not work，来短信时，收不到广播
        // 02-18, 在主活动添加运行时权限RECEIVE_SMS后， it work
        Log.i(TAG, "onReceive: executed.");
        //Toast.makeText(context, "ReceiverStaticSMS:onReceive executed.", Toast.LENGTH_LONG).show();

        // 收到任何短信，都尝试启动服务
        Intent in = new Intent(MsgApp.getContext(), FgService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MsgApp.getContext().startForegroundService(in);
        } else {
            MsgApp.getContext().startService(in);
        }

/*

        // for test
        Bundle bundle = intent.getExtras();
        // pdu是短信的基本承载单元，短信要求长度，超过长度则被分为许多个pdu
        Object[] objects = (Object[]) bundle.get("pdus");

        // 下面读取当前广播对应的一条短信
        // 长短信需要循环多次才能读取完整
        for (Object object:objects){
            SmsMessage message = SmsMessage.createFromPdu((byte[]) object);
            // 发信人号码
            String number = message.getOriginatingAddress();
            String content = message.getMessageBody();
            String dipBody = message.getDisplayMessageBody();
            Log.i(TAG,number+":"+content);
        }
*/

    }
}
