package com.yxh.msghelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

// 静态注册的receiver
public class ReceiverStaticSelfDef extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        // work fine on honor
        // not work on mate10  --20200313 好像是高版本增加了自定义广播的权限
        // 接收自定义广播，以及飞行模式改变的广播
        Toast.makeText(context,
                "ReceiverStaticSelfDef:onReceive intent.getAction()="+intent.getAction(),
                Toast.LENGTH_LONG).show();

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Log.e("ReceiverStaticSelfDef", "飞行模式状态 1为开启状态，0为关闭状态 airState=="
                    + bundle.getBoolean("state"));
        }else{
            Log.e("ReceiverStaticSelfDef", "bundle is null");
        }
    }
}
