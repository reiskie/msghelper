package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MsgListActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MsgListActivity";

    private List<MsgItem> itemList = null;
    private DataAccess dataAccess = null;
    private MsgItemAdapter mAdapter;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private boolean isBinded;
    private FgService.FgBinder mFgBinder;
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
            mFgBinder.triggerUpdateNotification();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_list);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMsg();
            }
        });

        Button button3 = findViewById(R.id.button_finish);
        button3.setOnClickListener(this);
        Button buttonread = findViewById(R.id.button_read);
        buttonread.setOnClickListener(this);

        String label = getIntent().getStringExtra("label");
        getSupportActionBar().setTitle(label);

        dataAccess = (DataAccess)getIntent().getSerializableExtra("dataAccess");

        itemList = dataAccess.getMsgfromDB(true);
        MsgItem blankItem = new MsgItem();
        blankItem.setInternal_flag(1); //占位，解决最后一行显示不全
        itemList.add(blankItem);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MsgItemAdapter(itemList);
        recyclerView.setAdapter(mAdapter);
        // 最后一行显示不全
        recyclerView.scrollToPosition(itemList.size()-1); // 不全
        //recyclerView.smoothScrollToPosition(itemList.size()-1); //不全
        //layoutManager.scrollToPositionWithOffset(itemList.size()-1,0); //不全

        if ( !isBinded){
            Intent in=new Intent(this, FgService.class);
            bindService(in, fgServiceConn, BIND_AUTO_CREATE);
        }

        getSupportActionBar().setTitle(label + " (" + (itemList.size() -1) + ")");

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_finish:
                finish();
                break;
            case R.id.button_read:
                readMsg();
                break;
            default:
                break;
        }
    }

    private void readMsg(){

        //按当前条件，重读db
        // itemList重新赋值
        itemList.clear();
        itemList = dataAccess.getMsgfromDB(true);
        mAdapter.notifyDataSetChanged();

    }


    private void refreshMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // re-read the itemList
                    Thread.sleep(500);

                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<MsgItem> t = dataAccess.getMsgfromDB(true);
                        //itemList.addAll(t);
                        //itemList.remove(1);
                        //mAdapter.notifyDataSetChanged();
                        //recyclerView.scrollToPosition(itemList.size()-1);
                        recyclerView.scrollToPosition(0);
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        Log.i(TAG,"onPause executed" );
        super.onPause();

        if (isBinded){
            //Log.i(TAG,"onPause(), clear updater and unbindService." );
            //mFgBinder.setGroupUpdater(null);
            // 连续调用unbindService第二次，就会崩溃
            unbindService(fgServiceConn);
            isBinded = false;
            Log.i(TAG, "onPause(), after unbindService, mFgBinder = " + mFgBinder);
            mFgBinder = null;
        }
    }

}
