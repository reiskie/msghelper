package com.yxh.msghelper;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class MsgApp extends Application {
    private static Context context;
    private static int defaultBackgroundColor = 0xFFFFFFFF;
    private static int clearedMsgColor = 0xFFE8F5E9;

    // FLAG_USER indicate the user who the current build is working for.
    public static final int FLAG_USER_SELF = 1;
    public static final int FLAG_USER_INTERNAL = 2;
    public static final int FLAG_USER_ALL = 3;
    public static int getFlagUser(){return FLAG_USER_INTERNAL;}

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
