package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ServiceConnection fgServiceConn = new ServiceConnection(){

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

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
        setContentView(R.layout.activity_main);

        Button button1 = (Button) findViewById(R.id.button_1);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this,"hello",Toast.LENGTH_LONG).show();
            }
        });

        Button button2 = (Button) findViewById(R.id.button_2);
        button2.setOnClickListener(this);

        Button button3 = (Button) findViewById(R.id.btn_service);
        button3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_2:
                Intent in1=new Intent(MainActivity.this, MsgGroupActivity.class);
                startActivity(in1);
                Log.i("main_act","in case button_2");
                break;

            case R.id.btn_service:
                Intent in2=new Intent(MainActivity.this, FgService.class);
                startService(in2);
                bindService(in2, fgServiceConn, BIND_AUTO_CREATE);
                Log.i("main_act","start and bind service.");
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

        Intent in2=new Intent(MainActivity.this, FgService.class);
        unbindService(fgServiceConn);
        stopService(in2);
        Log.i("main_onDestroy","stop and unbind service.");
    }
}
