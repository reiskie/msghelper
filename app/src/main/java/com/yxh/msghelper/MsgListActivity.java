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

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class MsgListActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MsgListActivity";

    private List<MsgItem> itemList = null;
    private DataAccess dataAccess = null;
    private MsgItemAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_list);
        Button button3 = findViewById(R.id.button_finish);
        button3.setOnClickListener(this);
        Button buttonread = findViewById(R.id.button_read);
        buttonread.setOnClickListener(this);

        dataAccess =
                new DataAccess(null, null, null,0,0) ;
        itemList = dataAccess.getMsgfromDB(true);

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
        }
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


}
