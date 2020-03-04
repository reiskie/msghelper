package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri;
import android.os.Message;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import org.litepal.LitePal;

import java.util.Map;

import q.rorbin.badgeview.QBadgeView;

public class MsgGroupActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MsgGroupActivity";
    private DataAccess mDataAccess;
    private TextView mTextView1;
    private EditText mEditText1;
    TextView mTextMajor;
    TextView mTextMinor;
    TextView mTextTrivial;
    TextView mTextClear;
    TextView mTextWorksheet;
    TextView mTextOther;

    private boolean isBinded = false;
    private ServiceConnection fgServiceConn = new ServiceConnection(){
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"ServiceConnection:onServiceDisconnected: from fgService ");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG,"ServiceConnection:onServiceConnected:ThreadID = " + Thread.currentThread().getId());

            isBinded = true;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //outState.putString("","");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_group);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataAccess = new DataAccess();

        Button button3 = findViewById(R.id.btn_try);
        button3.setOnClickListener(this);
        Button button_refresh = findViewById(R.id.btn_refresh);
        button_refresh.setOnClickListener(this);
        Button button_list = findViewById(R.id.button_list);
        button_list.setOnClickListener(this);

        mTextView1 = findViewById(R.id.text_1);
        mEditText1 = findViewById(R.id.edittext_1);
        mTextMajor = findViewById(R.id.text_major);
        mTextMajor.setOnClickListener(this);
        mTextMinor = findViewById(R.id.text_minor);
        mTextMinor.setOnClickListener(this);
        mTextTrivial = findViewById(R.id.text_trivial);
        mTextTrivial.setOnClickListener(this);
        mTextClear = findViewById(R.id.text_clear);
        mTextClear.setOnClickListener(this);
        mTextWorksheet = findViewById(R.id.text_worksheet);
        mTextWorksheet.setOnClickListener(this);
        mTextOther = findViewById(R.id.text_other);
        mTextOther.setOnClickListener(this);


        String howtostart=getIntent().getStringExtra("howtostart");
        Log.e(TAG,"howtostart="+howtostart);
        mTextView1.setText(howtostart);

        SQLiteDatabase db = LitePal.getDatabase();
        // 创建索引

        // for content observer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
        PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},1);
        }else{
        }

        // for broadcast receiver
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS},2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},3);
        }

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) !=
                    PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},4);
            }
        }

        DataAccess.copySMSFromInboxToDB();
        Log.i(TAG,"onCreate:ThreadID = " + Thread.currentThread().getId());

        Intent in=new Intent(this, FgService.class);
        startService(in);
        bindService(in, fgServiceConn, BIND_AUTO_CREATE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "您拒绝授予读取短信权限，无法完成正常功能!",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "您拒绝授予接收短信权限，无法完成正常功能!",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "您拒绝授予开机启动权限，无法完成正常功能!",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "您拒绝授予前台服务权限，无法完成正常功能!",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }



    @Override
    public void onClick(View v){
        DataAccess da = null;
        Intent in = null;
        switch (v.getId()){
            case R.id.btn_try:
                in=new Intent(this, ActivityTry.class);
                startActivity(in);
                break;
            case R.id.btn_refresh:
                Log.i(TAG,"onClick:refresh sms " );
                DataAccess.copySMSFromInboxToDB();
                //mDataAccess.getMsgfromDB(true);
                refreshFixedGroup();
                break;
            case R.id.button_list:
                da = new DataAccess();
                in = new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
                break;
            case R.id.text_major:
                da = new DataAccess(mDataAccess);
                da.setAlertLevel(1);
                in = new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
                break;
            case R.id.text_minor:
                da = new DataAccess(mDataAccess);
                da.setAlertLevel(2);
                in = new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
                break;
            case R.id.text_trivial:
                da = new DataAccess(mDataAccess);
                da.setAlertLevel(3);
                in = new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
                break;
            case R.id.text_clear:
                da = new DataAccess(mDataAccess);
                da.setAlertLevel(4);
                in=new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
            case R.id.text_worksheet:
                da = new DataAccess(mDataAccess);
                da.setCategory(2);
                in=new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
            case R.id.text_other:
                da = new DataAccess(mDataAccess);
                da.setCategory(-1);
                in=new Intent(this, MsgListActivity.class);
                in.putExtra("dataAccess", da);
                startActivity(in);
                break;
            default:
                break;
        }
    }

    protected void refreshFixedGroup() {
        Log.i(TAG,"refreshGroupLevel:ThreadID = " + Thread.currentThread().getId());
        Map<String, Integer> groupMap = null;
        DataAccess da = new DataAccess(mDataAccess);
        groupMap = da.aggregateMsgfromDB("msg_category");
        int cntOther = 0;
        int cntWorksheet = 0;

        for (String key : groupMap.keySet()) {
            if (key.equals("-1")){
                cntOther     = groupMap.get(key);
            }else if (key.equals("2")){
                cntWorksheet = groupMap.get(key);
            }else{
                // 1-告警
                // 0-初始化值，不可能有
            }
        }

        da = new DataAccess(mDataAccess);
        //da.setCategory(1);
        groupMap = da.aggregateMsgfromDB("al_level");

        int cntMajor = 0;
        int cntMinor = 0;
        int cntTrivial = 0;
        int cntClear = 0;

        for (String key : groupMap.keySet()) {
            if (key.equals("1")){
                cntMajor = groupMap.get(key);
            }else if (key.equals("2")){
                cntMinor = groupMap.get(key);
            }else if (key.equals("3")){
                cntTrivial = groupMap.get(key);
            }else if (key.equals("4")){
                cntClear = groupMap.get(key);
            }else{
                //0 : 初始化值，非告警(cate!=1)时会有
                //-1: 是告警但未能识别出级别，这种情况提取信息时已经归到category = -1 了
                // 因此这个分支的数据通过category都能覆盖
            }
        }
        Log.i(TAG,"refreshGroupLevel:cntMajor = " + cntMajor);
        Log.i(TAG,"refreshGroupLevel:cntMinor = " + cntMinor);
        Log.i(TAG,"refreshGroupLevel:cntTrivial = " + cntTrivial);
        Log.i(TAG,"refreshGroupLevel:cntOther = " + cntOther);

        mTextMajor.setText(Integer.toString(cntMajor));
        mTextMinor.setText(Integer.toString(cntMinor));
        mTextTrivial.setText(Integer.toString(cntTrivial));
        mTextClear.setText(Integer.toString(cntClear));
        mTextWorksheet.setText(Integer.toString(cntWorksheet));
        mTextOther.setText(Integer.toString(cntOther));
        new QBadgeView(this).bindTarget(mTextMajor)
                .setShowShadow(false)
                .setBadgeNumber(10);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshFixedGroup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBinded){
            // 连续调用unbindService第二次，就会崩溃
            unbindService(fgServiceConn);
            isBinded = false;
        }
    }
}
