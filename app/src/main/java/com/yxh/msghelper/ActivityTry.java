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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try);

        // try 动态注册receiver for SMS, not work，短信来时，无法收到广播（02-16）
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
                new DataAccess().clearMSGFromDB();
                break;
            case R.id.btn_save_sms:
                Log.i(TAG,"onClick:save_sms " );
                new DataAccess().getInboxSMSAndSaveToDB();
                break;
            case R.id.btn_read_sms:
                Log.i(TAG,"onClick:read_sms" );
                new DataAccess().getMsgfromDB();
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
}
