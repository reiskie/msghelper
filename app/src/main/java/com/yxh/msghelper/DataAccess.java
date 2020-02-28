package com.yxh.msghelper;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.litepal.LitePal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DataAccess implements Serializable {
    private static final String TAG = "DataAccess";
    String alertLevel;
    String system;
    // time 最近多久/当日 > xxxxxx
    //      指定范围  [xxx, yyy]
    long upperTime;
    long lowerTime;
    String category;

    public DataAccess(String alertLevel, String system, String category, long upperTime, long lowerTime) {
        this.alertLevel = alertLevel;
        this.system = system;
        this.upperTime = upperTime;
        this.lowerTime = lowerTime;
        this.category = category; // category value depends on data in db
    }

    public DataAccess(){
        // set level to all
        // set system to all
        //this.lowerTime = getStartTimeOfDay();
        // set category to 1-aleart
    }

    private String getPredicateString(){

        String str_cond = new String(" 1=1 ");

        if (alertLevel != null){
            str_cond += " and al_level = '" + alertLevel + "'";
        }
        if (system != null){
            str_cond += " and system = '" + system + "'";
        }
        if (category != null){
            str_cond += " and msg_category = '" + category + "'";
        }
        if (lowerTime > 0){
            str_cond += " and date >= " + lowerTime;
        }
        if (upperTime > 0){
            str_cond += " and date <= " + upperTime;
        }

        Log.i(TAG,"getConditionString(), str_cond=" + str_cond);

        return str_cond;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public long getUpperTime() {
        return upperTime;
    }

    public void setUpperTime(long upperTime) {
        this.upperTime = upperTime;
    }

    public long getLowerTime() {
        return lowerTime;
    }

    public void setLowerTime(long lowerTime) {
        this.lowerTime = lowerTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public static void clearMSGFromDB() {
        LitePal.deleteAll(MsgItem.class);
    }

    private static int getLastRawIdFromDB() {
        int last_raw_id = 0;
        MsgItem lastMsgItem = LitePal.findLast(MsgItem.class);
        if (lastMsgItem != null ){
            last_raw_id = lastMsgItem.getRaw_id();
        }
        Log.i(TAG, "getLastRawIdFromDB: last_raw_id in database: "+last_raw_id);
        return last_raw_id;
    }

    // return 是否有新数据
    public static boolean copySMSFromInboxToDB() {

        int last_raw_id = getLastRawIdFromDB();

        String sel="_id > ? and address in (?,?,?,?,?)";
        String[] selArgs = new String[] {
                Integer.toString(last_raw_id),
                //"10016", "1065510198","106559999", "10010", "13810105361"};
                "xxx", "xxx","10010", "+8613810745542", "+8613810105361"};

        Uri inSMSUri = Uri.parse("content://sms/inbox") ;
        int i=0;
        Cursor c = MsgApp.getContext().getContentResolver()
                .query(inSMSUri, null, sel, selArgs,"date asc");
        if(c != null) {
            Log.i(TAG, "copySMSFromInboxToDB: sms count by query inbox is " + c.getCount());
            while (c.moveToNext()) {
                MsgItem msgItem = new MsgItem();
                msgItem.setRaw_id(c.getInt(c.getColumnIndex("_id")));
                msgItem.setThread_id(c.getInt(c.getColumnIndex("thread_id")));
                msgItem.setAddress(c.getString(c.getColumnIndex("address")));
                msgItem.setDate(c.getLong(c.getColumnIndex("date")));
                msgItem.setBody(c.getString(c.getColumnIndex("body")));
                msgItem.save();
                i++;
                // for test
                int id = c.getInt(c.getColumnIndex("_id"));
                String addr = c.getString(c.getColumnIndex("address"));
                Log.i(TAG, "inserted _id=" + id + ", address=" + addr);
                //if (i >= 2) break;
            }
            c.close();

            Log.i(TAG, "inserted count of rows: " + i );
        }

        return true;
    }

    private static long getStartTimeOfDay(){
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
    public List<MsgItem> getMsgfromDB(boolean flag ){
        List<MsgItem> result = null;

        result = LitePal
                .where(this.getPredicateString())
                .order("id")
                .find(MsgItem.class);

        Log.i(TAG, "getMsgfromDB: result.size() = " + result.size());
        Log.i(TAG, "getMsgfromDB: result = " + result);

        return result;
    }

    // 读取今天的所有信息
    public List<MsgItem> getMsgfromDB( ){
        List<MsgItem> result = null;

        //String dateArgs = Long.toString(getStartTimeOfDay());
        String dateArgs = new String("1580916827092");
        result = LitePal
                //.select("*")  // * 可以不写select
                .where("date > ?", dateArgs)
                .order("id")
                .find(MsgItem.class);

        Log.i(TAG, "getMsgfromDB: result.size() = " + result.size());
        Log.i(TAG, "getMsgfromDB: result = " + result);

        return result;
    }

    // no groupby in LitePal
    // SQLiteDatabase.query() support groupBy and selectionArgs[], but still need to use cursor
    public Map<String, Integer>  aggregateMsgfromDB(String aggregate_col){
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        Log.i(TAG,"aggregateMsgfromDB(), aggregate_cod=" + aggregate_col);

        String sqlstr = new String("select ");
        sqlstr = sqlstr + aggregate_col + ", count(1) from msgitem where " + this.getPredicateString()
                + " group by " + aggregate_col + " order by " + aggregate_col;
        Log.i(TAG,"aggregateMsgfromDB(), sqlstr=" + sqlstr);

        Cursor c = LitePal.findBySQL(sqlstr);
        if (c != null){
            if(c.moveToFirst()) {
                do {
                    String col = c.getString(0);
                    int cnt = c.getInt(1);
                    result.put(col, cnt);
                } while (c.moveToNext());
            }
            c.close();
        }

        Log.i(TAG,"aggregateMsgfromDB(), result=" + result);

        return result;
    }



}
