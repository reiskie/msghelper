package com.yxh.msghelper;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ActivitySetting extends AppCompatActivity {

    private static final String TAG = "ActivitySetting";
    SharedPreferences mPref;
    private TextView mTextVolumn;
    private SeekBar mSeekBar;
    private TextView mTextRing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPref = MsgApp.getContext().getSharedPreferences("settings", MODE_PRIVATE);

        mTextVolumn = (TextView) findViewById(R.id.volumn_text);
        mSeekBar = (SeekBar) findViewById(R.id.volumn_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //SeekBar 滑动时的回调函数，其中 fromUser 为 true 时是手动调节
                mTextVolumn.setText("音量(" + progress + ")");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //SeekBar 开始滑动的的回调函数
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //SeekBar 停止滑动的回调函数
                if (mPref != null) {
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("volumn", seekBar.getProgress());
                    editor.apply();
                }
                UtilSound.setVolumnBySetting(seekBar.getProgress());
                UtilSound.play(false);
            }
        });


        int volumnSetting = mPref.getInt("volumn", 50);
        mTextVolumn.setText("音量(" + volumnSetting + ")");
        mSeekBar.setProgress(volumnSetting);

        mTextRing = (TextView) findViewById(R.id.ring_text);
        mTextRing.setText(UtilSound.getRingTitle());
        mTextRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置提示音");
                Uri uri = UtilSound.getRingUri();
                if (uri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
                }
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if  (requestCode == 1 && resultCode == RESULT_OK){
            Uri uri= data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (mPref != null) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString("ringuri", uri.toString());
                editor.apply();
            }
            UtilSound.setRingUri(uri);
            mTextRing.setText(UtilSound.getRingTitle());

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
