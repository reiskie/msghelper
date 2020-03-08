package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

        dataAccess = (DataAccess)getIntent().getSerializableExtra("dataAccess");

        itemList = dataAccess.getMsgfromDB(true);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MsgItemAdapter(itemList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.scrollToPosition(itemList.size()-1);

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

}
