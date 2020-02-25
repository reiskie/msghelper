package com.yxh.msghelper;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DataAccess {
    private static final String TAG = "DataAccess";


    //String predicate;
    //String[] predicate_args;

    // for detail
    String order_col;
    int limit;
    int offset;
    // List<SMS> result_set;

    // for aggregation
    String aggregate_col;
    // Map<String, Integer> result_count;



    public int getLastRawIdFromDB() {
        int last_raw_id = 0;
        SMS lastSms = LitePal.findLast(SMS.class);
        if (lastSms != null ){
            last_raw_id = lastSms.getRaw_id();
        }
        Log.i(TAG, "getLastRawIdFromDB: last_raw_id in database: "+last_raw_id);
        return last_raw_id;
    }


    public void getInboxSMSAndSaveToDB() {

        int last_raw_id = getLastRawIdFromDB();

        String sel="_id > ? and address in (?,?,?,?,?)";
        String[] selArgs = new String[] {
                Integer.toString(last_raw_id),
                //"10016", "1065510198","106559999", "10010", "13810105361"};
                "xxx", "xxx","xxx", "+8613810745542", "+8613810105361"};

        Uri inSMSUri = Uri.parse("content://sms/inbox") ;
        int i=0;
        Cursor c = MsgApp.getContext().getContentResolver()
                .query(inSMSUri, null, sel, selArgs,"date asc");
        if(c != null) {
            Log.i(TAG, "readMsgFromInboxToDB: sms count by query inbox is " + c.getCount());
            while (c.moveToNext()) {
                SMS sms = new SMS();
                sms.setRaw_id(c.getInt(c.getColumnIndex("_id")));
                sms.setThread_id(c.getInt(c.getColumnIndex("thread_id")));
                sms.setAddress(c.getString(c.getColumnIndex("address")));
                sms.setDate(c.getLong(c.getColumnIndex("date")));
                sms.setBody(c.getString(c.getColumnIndex("body")));
                sms.save();
                i++;
                // for test
                int id = c.getInt(c.getColumnIndex("_id"));
                String addr = c.getString(c.getColumnIndex("address"));
                Log.i(TAG, "inserted _id=" + id + ", address=" + addr);
                //if (i >= 2) break;
            }
            c.close();
        }
    }

    private long getStartTimeOfDay(){
//        Calendar calendar=Calendar.getInstance();
//        calendar.setTime(new java.util.Date());
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        return calendar.getTime().getTime();

        long current = System.currentTimeMillis();
        long zero = current-(current+ TimeZone.getDefault().getRawOffset())%(1000*3600*24);
        return zero;
    }

    // use LitePal
    // E.g.@github
    // List<Song> songs = LitePal.where("name like ? and duration < ?", "song%", "200").order("duration").find(Song.class);
    // 注意: where方法的参数是(String... conditions), 不支持用String[]数组表示条件中用到的多个值.
    public List<SMS> getMsgfromDB(String order_col, int limit, int offset, String... conditions ){

        List<SMS> result = null;

        //String dateArgs = Long.toString(getStartTimeOfDay());
        String dateArgs = new String("1580916827092");
        result = LitePal.select("*")
                .where("date > ?", dateArgs)
                .order("id")
                .find(SMS.class);

        Log.i(TAG, "getMsgfromDB: ls.size() = " + result.size());

        return result;
    }

    // no groupby in LitePal
    // use SQLiteDatabase.query(), it support selectionArgs[]
    public Map<String, Integer>  aggregateMsgfromDB(String aggregate_col){

        Map<String, Integer> result = null;

        return result;
    }

        public void clearMSGFromDB() {
        LitePal.deleteAll(SMS.class);
    }

}
