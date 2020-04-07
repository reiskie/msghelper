package com.yxh.msghelper;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class MsgApp extends Application {
    private static Context context;
    private static int defaultBackgroundColor = 0xFFFFFFFF;
    private static int clearedMsgColor = 0xFFE8F5E9;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        //defaultBackgroundColor = context.getResources().getColor(R.color.background_material_light);
        clearedMsgColor = context.getResources().getColor(R.color.colorClearedMsg);
        LitePal.initialize(context);
    }

    public static Context getContext(){
        return context;
    }

    public static int getBackgroundColor(){return defaultBackgroundColor;}

    public static int getClearedMsgColor(){return clearedMsgColor;}

}
