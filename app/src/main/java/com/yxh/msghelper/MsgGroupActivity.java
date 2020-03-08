package com.yxh.msghelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MsgGroupActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MsgGroupActivity";
    private DataAccess mDataAccess;
    private ArrayList<String> permissionNeeded;
    private boolean isJustCreated;
    private boolean isBinded;
    private FgService.FgBinder mFgBinder;

    private TextView mTextView1;
    private EditText mEditText1;

    private TextView mTextMajor;
    private TextView mTextMinor;
    private TextView mTextTrivial;
    private TextView mTextClear;
    private TextView mTextWorksheet;
    private TextView mTextOther;

    private Badge mBadgeMajor;
    private Badge mBadgeMinor;
    private Badge mBadgeTrivial;
    private Badge mBadgeClear;
    private Badge mBadgeWorksheet;
    private Badge mBadgeOther;
    private ActivityGroupUpdater updater = new ActivityGroupUpdater();

    private ServiceConnection fgServiceConn = new ServiceConnection(){
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"ServiceConnection:onServiceDisconnected: from fgService ");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG,"ServiceConnection:onServiceConnected:ThreadID = " + Thread.currentThread().getId());
            isBinded = true;
            mFgBinder = (FgService.FgBinder)service;
            mFgBinder.setGroupUpdater(updater);

            // 如果是新创建的活动，bing成功后触发一次异步读短信，后面会自动回调updater刷新ui
            // 因为活动销毁后，服务可能也挂了，没人去自动读取SMS
            if (isJustCreated){
                mFgBinder.triggerReadSmsAsync();
                isJustCreated = false;
            }else{
                // 从后台回到前台，或切回桌面后又切回来，在onResume中再次bind服务
                // 因为pause时unbind了，期间无法接收到updater的更新调用，所以resume后需要更新一下
                refreshFixedGroup();
            }
        }
    };

     class ActivityGroupUpdater{
        public void update(){
            Log.i(TAG, "GroupUpdater:update() executed.");
            refreshFixedGroup();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //outState.putString("","");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
     }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.manual_read_sms:

                Log.i(TAG,"onOptionsItemSelected: manual trigger to read sms " );

                if (mFgBinder != null){
                    mFgBinder.triggerReadSmsAsync();
                }else{
                    // this might be slow
                    Toast.makeText(this, "前台读取短信可能较慢，请耐心等待，谢谢！", Toast.LENGTH_LONG).show();
                    DataAccess.copySMSFromInboxToDB();
                    refreshFixedGroup();
                }
                break;
            case R.id.about:
                Toast.makeText(this, "Thank you for using this app:)", Toast.LENGTH_LONG).show();
                break;
             default:
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate:ThreadID = " + Thread.currentThread().getId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_group);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        mBadgeMajor = new QBadgeView(this).bindTarget(mTextMajor);
        mBadgeMajor.setShowShadow(false);
        mBadgeMinor = new QBadgeView(this).bindTarget(mTextMinor);
        mBadgeMinor.setShowShadow(false);
        mBadgeTrivial = new QBadgeView(this).bindTarget(mTextTrivial);
        mBadgeTrivial.setShowShadow(false);
        mBadgeClear = new QBadgeView(this).bindTarget(mTextClear);
        mBadgeClear.setShowShadow(false);//.setBadgeBackgroundColor(Color.WHITE)
        //        .stroke(0xFF32CD32,1,true).setBadgeTextColor(0xFF32CD32);
        mBadgeWorksheet = new QBadgeView(this).bindTarget(mTextWorksheet);
        mBadgeWorksheet.setShowShadow(false);//.setBadgeBackgroundColor(Color.WHITE)
                //.stroke(0xFFAE57A4,1,true).setBadgeTextColor(0xFFAE57A4);
        mBadgeOther = new QBadgeView(this).bindTarget(mTextOther);
        mBadgeOther.setShowShadow(false);//.setBadgeBackgroundColor(Color.WHITE)
                //.stroke(0xFF9D9D9D,1,true).setBadgeTextColor(0xFF9D9D9D);

        String howtostart=getIntent().getStringExtra("howtostart");
        Log.e(TAG,"howtostart="+howtostart);
        mTextView1.setText(howtostart);

        // check/apply permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
//        PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_SMS},1);
//        }
//        // for broadcast receiver
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
//                PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECEIVE_SMS},2);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) !=
//                PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},3);
//        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) !=
//                    PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.FOREGROUND_SERVICE},4);
//            }
//        }

        // 高版本申请动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            Log.i(TAG, "onCreate(): permissionNeeded=" + permissionNeeded);
            checkPermission();
        }
        
        isJustCreated = true;
        SQLiteDatabase db = LitePal.getDatabase();
        // 考虑创建索引

        mDataAccess = new DataAccess();
        // move below work to ServiceConnection.onServiceConnected: mFgBinder.triggerReadSmsAsync()
        // DataAccess.copySMSFromInboxToDB();

        if (permissionNeeded.size() == 0) {
            // 服务中注册contentObserver，需要权限
            Intent in = new Intent(this, FgService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(in);
            } else {
                startService(in);
            }
        }
     }

     private void checkPermission(){

         if (permissionNeeded == null) {
             permissionNeeded = new ArrayList<String>();
             permissionNeeded.add(Manifest.permission.READ_SMS);     
             permissionNeeded.add(Manifest.permission.RECEIVE_SMS);  
             permissionNeeded.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                 permissionNeeded.add(Manifest.permission.FOREGROUND_SERVICE);  
             }
         }

         ArrayList<String> tmpList = (ArrayList<String>)permissionNeeded.clone();
         for (String key : tmpList ) {
             if (ContextCompat.checkSelfPermission(this, key) == PackageManager.PERMISSION_GRANTED) {
                 permissionNeeded.remove(key);
                 Log.i(TAG, "onCreate(): had permission: " + key);
             }else{
                 Log.i(TAG, "onCreate(): have not permission: " + key);
             }
         }

         if (permissionNeeded.size() > 0 ){
             String[] permissions = permissionNeeded.toArray(new String[permissionNeeded.size()]);
             ActivityCompat.requestPermissions(this, permissions, 100);
         }
     }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
//        switch (requestCode) {
//            case 1:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    Toast.makeText(this, "您拒绝授予读取短信权限，无法完成正常功能!",
//                            Toast.LENGTH_LONG).show();
//                }
//                break;
//            case 2:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    Toast.makeText(this, "您拒绝授予接收短信权限，无法完成正常功能!",
//                            Toast.LENGTH_LONG).show();
//                }
//                break;
//            case 3:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    Toast.makeText(this, "您拒绝授予开机启动权限，无法完成正常功能!",
//                            Toast.LENGTH_LONG).show();
//                }
//                break;
//            case 4:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    Toast.makeText(this, "您拒绝授予前台服务权限，无法完成正常功能!",
//                            Toast.LENGTH_LONG).show();
//                }
//                break;
//            default:
//                break;
//        }

        Log.i(TAG, "onRequestPermissionsResult(): requestCode=" + requestCode);
        Log.i(TAG, "onRequestPermissionsResult(): permissions.length=" + permissions.length);
        Log.i(TAG, "onRequestPermissionsResult(): grantResults.length = " + grantResults.length);

        if (requestCode == 100){
            for (int i = 0; i < grantResults.length; i++) {
                Log.i(TAG, "onRequestPermissionsResult, i="+i +", grantResult[i]="+ grantResults[i]);

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "您拒绝授予权限，无法完成正常功能!", Toast.LENGTH_LONG).show();
                }else { // PackageManager.PERMISSION_GRANTED
                    permissionNeeded.remove(permissions[i]);
                }
            }
        }

        // have enough permissions
        if (permissionNeeded.size() == 0) {
            // 服务中注册contentObserver，需要权限
            Intent in = new Intent(this, FgService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(in);
            } else {
                startService(in);
            }
            // 到这里说明第一次获得权限，onResume可能已经结束，但其中没有bind（因为没权限）
            bindService(in, fgServiceConn, BIND_AUTO_CREATE);
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
//                Log.i(TAG,"onClick:refresh sms " );
//                if (mFgBinder == null){
//                    // this might be slow
//                    DataAccess.copySMSFromInboxToDB();
//                    refreshFixedGroup();
//                }else{
//                    mFgBinder.triggerReadSmsAsync();
//                }
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
        groupMap = da.aggregateMsgfromDB("msg_category", "is_read = 0");
        int badgeOther = 0;
        int badgeWorksheet = 0;

        for (String key : groupMap.keySet()) {
            if (key.equals("-1")){
                badgeOther     = groupMap.get(key);
            }else if (key.equals("2")){
                badgeWorksheet = groupMap.get(key);
            }else{
                // 1-告警
                // 0-初始化值，不可能有
            }
        }

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

        //da.setCategory(1);
        groupMap = da.aggregateMsgfromDB("al_level", "is_read = 0");

        int badgeMajor = 0;
        int badgeMinor = 0;
        int badgeTrivial = 0;
        int badgeClear = 0;

        for (String key : groupMap.keySet()) {
            if (key.equals("1")){
                badgeMajor = groupMap.get(key);
            }else if (key.equals("2")){
                badgeMinor = groupMap.get(key);
            }else if (key.equals("3")){
                badgeTrivial = groupMap.get(key);
            }else if (key.equals("4")){
                badgeClear = groupMap.get(key);
            }else{
            }
        }

        Log.i(TAG,"refreshGroupLevel:cntMajor = " + cntMajor + ",badgeMajor="+badgeMajor);
        Log.i(TAG,"refreshGroupLevel:cntMinor = " + cntMinor + ",badgeMinor="+badgeMinor);
        Log.i(TAG,"refreshGroupLevel:cntTrivial = " + cntTrivial + ",badgeTrivial="+badgeTrivial);
        Log.i(TAG,"refreshGroupLevel:cntOther = " + cntOther + ",badgeOther="+badgeOther);

        mTextMajor.setText(Integer.toString(cntMajor));
        mTextMinor.setText(Integer.toString(cntMinor));
        mTextTrivial.setText(Integer.toString(cntTrivial));
        mTextClear.setText(Integer.toString(cntClear));
        mTextWorksheet.setText(Integer.toString(cntWorksheet));
        mTextOther.setText(Integer.toString(cntOther));


        mBadgeMajor.setBadgeNumber(badgeMajor);
        mBadgeMinor.setBadgeNumber(badgeMinor);
        mBadgeTrivial.setBadgeNumber(badgeTrivial);
        mBadgeClear.setBadgeNumber(badgeClear);
        mBadgeWorksheet.setBadgeNumber(badgeWorksheet);
        mBadgeOther.setBadgeNumber(badgeOther);

//        mBadgeMajor.setBadgeNumber(10);
//        mBadgeMinor.setBadgeNumber(2);
//        mBadgeTrivial.setBadgeNumber(3);
//        mBadgeClear.setBadgeNumber(5);
//        mBadgeWorksheet.setBadgeNumber(7);
//        mBadgeOther.setBadgeNumber(10);

    }

    @Override
    protected void onStart() {
        Log.i(TAG,"onStart executed" );
        super.onStart();
        // move this to onResume
        // refreshFixedGroup();
    }
    @Override
    protected void onRestart() {
        Log.i(TAG,"onRestart executed" );
        super.onRestart();
    }
    @Override
    protected void onStop() {
        Log.i(TAG,"onStop executed" );
        super.onStop();
        // not guarantee to be called
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume executed" );
        super.onResume();

        if (permissionNeeded.size() == 0 && !isBinded){
            Intent in=new Intent(this, FgService.class);
            bindService(in, fgServiceConn, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause executed" );
        super.onPause();
        if (isBinded){
            Log.i(TAG,"onPause(), clear updater and unbindService." );
            mFgBinder.setGroupUpdater(null);
            // 连续调用unbindService第二次，就会崩溃
            unbindService(fgServiceConn);
            isBinded = false;
            Log.i(TAG, "onPause(), after unbindService, mFgBinder = " + mFgBinder);
            mFgBinder = null;
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy executed" );
        super.onDestroy();
        // not guarantee to be called
    }


}
