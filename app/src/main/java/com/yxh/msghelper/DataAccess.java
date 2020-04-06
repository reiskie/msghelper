package com.yxh.msghelper;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.litepal.LitePal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DataAccess implements Serializable {
    private static final String TAG = "DataAccess";
    int alertLevel;
    String system;
    // time 最近多久/当日 > xxxxxx
    //      指定范围  [xxx, yyy]
    long upperTime;
    long lowerTime;
    int category;

    public DataAccess(int alertLevel, String system, int category, long upperTime, long lowerTime) {
        this.alertLevel = alertLevel;
        this.system = system;
        this.upperTime = upperTime;
        this.lowerTime = lowerTime;
        this.category = category; // category value depends on data in db
    }

    public DataAccess(){
        // set level to all
        // set system to all
        this.lowerTime = getDefaultLowerTime();
        // set category to 1-aleart
    }

    public DataAccess(DataAccess da){
        this.alertLevel = da.alertLevel;
        this.system = da.system;
        this.upperTime = da.upperTime;
        this.lowerTime = da.lowerTime;
        this.category = da.category;
    }


    private String getPredicateString(){

        String str_cond = new String(" 1=1 ");

        if (alertLevel != 0){
            str_cond += " and al_level = " + alertLevel + "";
        }
        if (system != null){
            str_cond += " and system = '" + system + "'";
        }
        if (category != 0){
            str_cond += " and msg_category = " + category + "";
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

    public int getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(int alertLevel) {
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

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public static void clearMSGFromDB() {
        LitePal.deleteAll(MsgItem.class);
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

    private static long getEarliestTimeForReadingInbox(){
        //now is 24 hours. it should depend on configuration
        // if user change this config to a earlier time,
        //   we should check whether this new time is earlier than the first one in db.
        //   if so, might want to clear table in db and reload all.
        long t1 = System.currentTimeMillis() - (1000L*3600*24*7);
        //long t1 = 100; // for test
        return t1;
    }

    private static long getDefaultLowerTime(){
        long t1 = System.currentTimeMillis() - (1000*3600*24);
        //long t1 = 100; // for test
        return t1;
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

    private static int getFirstRawIdFromDB() {
        int first_raw_id = 0;
        MsgItem lastMsgItem = LitePal.findFirst(MsgItem.class);
        if (lastMsgItem != null ){
            first_raw_id = lastMsgItem.getRaw_id();
        }
        Log.i(TAG, "getFirstRawIdFromDB: first_raw_id in database: "+first_raw_id);
        return first_raw_id;
    }

    // return 是否有新数据
    public static int copySMSFromInboxToDB() {

        StringBuilder sb = new StringBuilder("address in (?,?,?,?,?) ");
        //String[] arr = {"xxx", "1065510198","10010", "+8613810745542", "+8613810105361"};
        //String[] arr = {"xxx", "xxxx","10086", "+8613810745542", "+8613810105361"};
        //String[] arr = {"xxx", "xxx","xxx", "+8613810745542", "+8613810105361"};
        //String[] arr = {"10016", "1065510198","106559999", "10010", "13810105361"};
        String[] arr = {"106980095568911", "106980095568","+8613810105361", "xxx", "xxx"};

        List<String> argsList = new ArrayList<String>(Arrays.asList(arr));

        int last_raw_id = getLastRawIdFromDB();
        if (last_raw_id == 0){ // empty table
            sb.append(" and date > ? ");
            argsList.add(Long.toString(DataAccess.getEarliestTimeForReadingInbox()));
        }else{
            sb.append(" and _id > ?  ");
            argsList.add(Integer.toString(last_raw_id));
        }

        String selString = sb.toString();
        String[] selArgs = argsList.toArray(new String[argsList.size()]);
        Log.i(TAG, "copySMSFromInboxToDB: sel: "+selString);
        Log.i(TAG, "copySMSFromInboxToDB: selArgs: "+selArgs);

        Uri inSMSUri = Uri.parse("content://sms/inbox") ;
        int i=0;
        Cursor c = MsgApp.getContext().getContentResolver()
                .query(inSMSUri, null, selString, selArgs,"date asc");
        if(c != null) {
            Log.i(TAG, "copySMSFromInboxToDB: sms count by query inbox is " + c.getCount());
            while (c.moveToNext()) {
                MsgItem msgItem = new MsgItem();
                msgItem.setRaw_id(c.getInt(c.getColumnIndex("_id")));
                msgItem.setThread_id(c.getInt(c.getColumnIndex("thread_id")));
                msgItem.setAddress(c.getString(c.getColumnIndex("address")));
                msgItem.setDate(c.getLong(c.getColumnIndex("date")));
                msgItem.setBody(c.getString(c.getColumnIndex("body")));
                msgItem.extractInfo();
                if (msgItem.getMsg_category() == 1 && msgItem.getAl_level() == 4){ // 清除告警
                    // 如果 copySMSFromInboxToDB 是被同步调用，这里会慢
                    tryRelateMsg(msgItem);
                }
                msgItem.save();
                i++;
                // for test
                int id = c.getInt(c.getColumnIndex("_id"));
                String addr = c.getString(c.getColumnIndex("address"));
                if ( i % 1000 == 0){
                    Log.i(TAG, "copySMSFromInboxToDB [" + i + "]: inserted _id=" + id + ", address=" + addr);
                }
                //if (i >= 2) break;
            }
            c.close();

            Log.i(TAG, "copySMSFromInboxToDB: inserted count of rows: " + i );
        }

        return i;
    }

    public  static void tryRelateMsg(MsgItem clearItem){
        Log.i(TAG, "tryRelateMsg() execute.");

        List<MsgItem> result = null;
        int max_sim = 0;
        int max_sim_raw_id = 0;
        int last_min_raw_id = Integer.MAX_VALUE;
        long earliestTimeForRelate = clearItem.getDate() - (1000L*3600*24);
        while (true){
            result = LitePal
                    .where("date > " + earliestTimeForRelate
                            + " and raw_id < " + last_min_raw_id
                            + " and msg_category = 1 "     // 告警
                            + " and al_level in (1,2,3) "  // 主/次/警
                            + " and is_cleared = 0 "
                            + " and rel_raw_id = 0 "
                    )
                    .order("id desc")
                    .limit(20)
                    .find(MsgItem.class);

            Log.i(TAG, "tryRelateMsg: result.size() = " + result.size());

            if  (result.size() > 0){
                for (MsgItem item : result){
                    int sim = MsgItem.compareMsgStr(item.getBody(), clearItem.getBody());

                    if (sim == 100) {
                        item.setIs_cleared(true);
                        item.setRel_raw_id(clearItem.getRaw_id());
                        item.setSim_perc(sim);
                        item.save();
                        clearItem.setRel_raw_id(item.getRaw_id());
                        clearItem.setSim_perc(sim);
                        break;
                    }else{
                        last_min_raw_id = item.getRaw_id();
                        if (sim > max_sim){
                            max_sim = sim;
                            max_sim_raw_id = item.getRaw_id();
                        }
                    }
                }
                if (clearItem.getRel_raw_id() > 0 || result.size() < 20){
                    break;
                }
            }else{
                break;
            }
        }

        if (clearItem.getRel_raw_id() == 0 &&  max_sim >= 80){
            MsgItem item = LitePal
                    .where("date > " + earliestTimeForRelate
                            + " and raw_id = " + max_sim_raw_id
                            + " and msg_category = 1 "     // 告警
                            + " and al_level in (1,2,3) "  // 主/次/警
                            + " and is_cleared = 0 "
                            + " and rel_raw_id = 0 "
                    )
                    .findFirst(MsgItem.class);

            item.setIs_cleared(true);
            item.setRel_raw_id(clearItem.getRaw_id());
            item.setSim_perc(max_sim);
            item.save();
            clearItem.setRel_raw_id(item.getRaw_id());
            clearItem.setSim_perc(max_sim);
        }
    }

    public static MsgItem findRelatedMsg(MsgItem item){

        MsgItem item1 = null;

        long boundary = item.getDate() - (1000*3600*24);

        item1 = LitePal
                .where("date > " + boundary
                        + " and msg_category = 1 "     // 告警
                        + " and rel_raw_id =  " + item.getRaw_id()
                )
                .findFirst(MsgItem.class);

        return item1;
    }

    // use LitePal
    // E.g.@github
    // List<Song> songs = LitePal.where("name like ? and duration < ?", "song%", "200").order("duration").find(Song.class);
    // 注意: where方法的参数是(String... conditions), 不支持用String[]数组表示条件中用到的多个值.
    public List<MsgItem> getMsgfromDB(boolean setRead ){
        List<MsgItem> result = null;

        result = LitePal
                .where(this.getPredicateString())
                .order("id")
                .find(MsgItem.class);

        Log.i(TAG, "getMsgfromDB: result.size() = " + result.size());
        Log.i(TAG, "getMsgfromDB: result = " + result);

        if  (setRead && result.size()>0){
            for (MsgItem item : result){
                if (!item.isIs_read()){
                    item.setIs_read(true);
                    item.save();
                }
            }
        }

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

    public Map<String, Integer>  aggregateMsgfromDB(String aggregate_col, String condition){
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        Log.i(TAG,"aggregateMsgfromDB(), aggregate_cod=" + aggregate_col + ", cond=" + condition);

        String sqlstr = new String("select ");
        sqlstr = sqlstr + aggregate_col + ", count(1) from msgitem where "
                + this.getPredicateString() + " and " + condition
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
