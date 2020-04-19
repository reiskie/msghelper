package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class ActivitySetting extends AppCompatActivity {

    SharedPreferences mPref;
    private TextView mTextVolumn;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPref = MsgApp.getContext().getSharedPreferences("settings", MODE_PRIVATE);

        mTextVolumn = (TextView)findViewById(R.id.volumn_text);
        mSeekBar = (SeekBar)findViewById(R.id.volumn_bar);
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
                if (mPref != null){
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("volumn", seekBar.getProgress());
                    editor.apply();
                }
                UtilSound.setVolumnBySetting(seekBar.getProgress());
                UtilSound.playSound(false);
            }
        });


        int volumnSetting = mPref.getInt("volumn", 50);
        mTextVolumn.setText("音量(" + volumnSetting + ")");
        mSeekBar.setProgress(volumnSetting);
    }
}
