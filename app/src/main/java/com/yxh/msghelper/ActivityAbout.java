package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

public class ActivityAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String mode = getIntent().getStringExtra("mode");

        TextView tv = findViewById(R.id.content);
        switch (mode){
            case "about":
                getSupportActionBar().setTitle("关于");
                tv.setText(R.string.about);
                break;
            case "manual":
                getSupportActionBar().setTitle("说明");
                tv.setText(R.string.manual);
                break;
            default:
                break;
        }

    }
}
