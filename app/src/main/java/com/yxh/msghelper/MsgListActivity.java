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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MsgListActivity extends AppCompatActivity implements View.OnClickListener{

    private List<MsgItem> itemList = new ArrayList<>();
    private MsgItemAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_list);
        Button button3 = (Button) findViewById(R.id.button_finish);
        button3.setOnClickListener(this);
        Button buttonread = (Button) findViewById(R.id.button_read);
        buttonread.setOnClickListener(this);

        initMsgItems();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MsgItemAdapter(itemList);
        recyclerView.setAdapter(mAdapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},1);
        }else{
            ;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ;
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

        //查询发件箱里的内容
        Uri inSMSUri = Uri.parse("content://sms/inbox") ;
        int i=0;
        Cursor c = this.getContentResolver().query(inSMSUri, null, null, null,"date desc");
        if(c != null) {

            Log.i("xxx", "the number of inbox is " + c.getCount());
            itemList.clear();

            //循环遍历
            while (c.moveToNext()) {
                Log.i("xxx", c.getString(c.getColumnIndex("body")));
                MsgItem m1 = new MsgItem(
                        c.getLong(c.getColumnIndex("date")),
                        c.getString(c.getColumnIndex("address")),
                        c.getString(c.getColumnIndex("body"))
                );
                m1.set_id(c.getInt(c.getColumnIndex("_id")));
                m1.setThread_id(c.getInt(c.getColumnIndex("thread_id")));
                m1.setType(c.getInt(c.getColumnIndex("type")));
                m1.setStatus(c.getInt(c.getColumnIndex("status")));
                m1.setDate_sent(c.getLong(c.getColumnIndex("date_sent")));
                m1.setCreator(c.getString(c.getColumnIndex("creator")));
                m1.setSubject(c.getString(c.getColumnIndex("subject")));
                //m1.setMtu(c.getInt(c.getColumnIndex("mtu")));
                m1.setProtocol(c.getInt(c.getColumnIndex("protocol")));
                m1.setRead((0==c.getInt(c.getColumnIndex("read")))?false:true);

                itemList.add(m1);
                if (itemList.size() > 10) break;
            }
            c.close();
            mAdapter.notifyDataSetChanged();

        }
    }

    private void initMsgItems() {
        for(int i = 0; i < 1; i++) {
            MsgItem m1 = new MsgItem(11221, "10086", "火车");
            itemList.add(m1);
            MsgItem m2 = new MsgItem(355453, "10010", "飞机");
            itemList.add(m2);
            MsgItem m3 = new MsgItem(31233, "10000", "汽车");
            itemList.add(m3);
        }
    }

}
