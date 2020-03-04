package com.yxh.msghelper;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

public class SMSContentObserver extends ContentObserver {
    private static final String TAG = "SMSContentObserver";

    private Context mContext  ;
    private Cursor cursor = null;
    private Handler mHandler;

    public SMSContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context ;
        mHandler = handler ;
        Log.i(TAG,"SMSContentObserver():ThreadID = " + Thread.currentThread().getId());

    }
    //  Added in API level 1
    @Override
    public void onChange(boolean selfChange){
        super.onChange(selfChange);
        Log.i(TAG, "onChange of old version: selfChange="+selfChange);

        //if use below delegate call then supper.onChange can only be called in onChange(boolean)
        //this.onChange(selfChange, null);
    }

    //  Added in API level 16  Android 4.1
    //  it seems that the supper.onChange in this function will trigger onChange(boolean).
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.i(TAG, "onChange: selfChange="+selfChange+", Uri="+uri.toString());
        Log.i(TAG,"onChange:ThreadID = " + Thread.currentThread().getId());

        if (uri == null) {
            return;
        }

        switch (uri.toString()){
            case "content://sms/raw": //虽然收到了短信.但是短信并没有写入到收件箱里
                return;
            case "content://sms/inbox-insert":
                Log.i(TAG, "onChange: deal the msg here!!!");
                break;
             default:
                 //content://sms/52 (number is sms id which will increase)
                 //content://sms/53
                 return;
        }

        Uri inboxUri = Uri.parse("content://sms/inbox") ;
        int i=0;
        Cursor c = mContext.getContentResolver()
                .query(inboxUri, null, null, null,"date desc");
        if(c != null){

            Log.i(TAG, "the number of inbox is "+c.getCount()) ;

            StringBuilder sb = new StringBuilder() ;

            while(c.moveToNext()){
                Log.i(TAG, c.getString(c.getColumnIndex("body")));

                sb.append(++i)
                        .append(": 发件人手机号码: "+c.getInt(c.getColumnIndex("address")))
                        .append(" 信息内容: "+c.getString(c.getColumnIndex("body")))
                        .append("\n\n");
                break; // for test: only read one row
            }
            c.close();

            Message message = mHandler.obtainMessage(1);
            message.obj = sb.toString();
            mHandler.sendMessage(message);
        }
    }





}