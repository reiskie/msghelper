package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri;
import android.os.Message;
import android.os.Handler;
import android.widget.Toast;

public class MsgGroupActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTextView1;
    private EditText mEditText1;
    private SMSContentObserver smsContentObserver;

    private  Handler mHandler = new Handler(new Handler.Callback() {

        public boolean  handleMessage(Message msg) {

            Log.i("MsgGroupActivity", "in handleMessage");
            switch (msg.what) {
                case 1:
                    String outbox = (String) msg.obj;
                    //mTextView1.setText(outbox);
                    mEditText1.setText(outbox);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_group);

        Button button3 = (Button) findViewById(R.id.button_3);
        button3.setOnClickListener(this);
        Button button_list = (Button) findViewById(R.id.button_list);
        button_list.setOnClickListener(this);

        mTextView1 = (TextView) findViewById(R.id.text_1);
        mEditText1 = findViewById(R.id.edittext_1);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
        PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},1);
        }else{
            registerContentObservers();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerContentObservers();
                } else {
                    Toast.makeText(this, "您拒绝授予此应用权限，无法完成正常功能",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void registerContentObservers(){
        smsContentObserver = new SMSContentObserver(this, mHandler);
        Uri smsUri = Uri.parse("content://sms");
        getContentResolver().registerContentObserver(smsUri,
                true, smsContentObserver);

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_3:
                finish();
                break;
            case R.id.button_list:
                Intent in1=new Intent(this, MsgListActivity.class);
                startActivity(in1);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (smsContentObserver != null) {
            getContentResolver().unregisterContentObserver(smsContentObserver);
        }
    }

}
