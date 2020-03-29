package com.yxh.msghelper;

import android.util.Log;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
数据库中字段的顺序，按字母排序的:
CREATE TABLE sms
(id integer primary key autoincrement,
address text,
al_level integer,
body text,
date integer,
is_cleared integer,
is_read integer,
msg_category integer,
msg_srouce integer,
raw_id integer,
system text,
thread_id integer);
 */

public class MsgItem extends LitePalSupport implements Serializable {
    private static final String TAG = "MsgItem";

    // fields from sms-inbox
    private int raw_id; // for _id
    private int thread_id;
    //private int type; // 类型 1-inbox 2-sent
    private String address;
    private long date;
    //private boolean read; // 系统收件箱中  0未读， 1已读
    //private int status;  //TP-Status value for the message, or -1 if no status has been received
    private String body;

    // below are this app's specific fields.
    private boolean is_read;
    private int msg_category; // -1-未知，1-告警，2-工单
    private int msg_srouce; // -1-未知，1-patrol, 2-zabbix, 3-alphaOps，4-itoms，5-自动化，iPaas

    // category为 "告警" 时的属性
    private boolean is_cleared; // 告警是否清除
    private int al_level; // 告警级别: -1-未知，1-主要，2-次要，3-警告，4-清除
    //private String al_type; // 告警种类: 状态类，性能类，容量类，错误提示类，应用日志监控
    private String system; // 系统，仅对告警提取system
    //private String sys_level; // 系统级别，告警内容里有
    private int rel_raw_id; // 关联信息ID，例如清除短信的ID

    @Column(ignore = true)
    private String day;
    @Column(ignore = true)
    private String mon;
    @Column(ignore = true)
    private String year;
    @Column(ignore = true)
    private String time;
    @Column(ignore = true)
    private int internal_flag; // 1 - 用于列表最后一行占位，解决recyclerView最后一行显示不全

    public int getInternal_flag() {
        return internal_flag;
    }

    public void setInternal_flag(int internal_flag) {
        this.internal_flag = internal_flag;
    }

    public void extractInfo() {

        if (body == null){
            return;
        }

        if (msg_category == 0){
            // 识别告警更重要，贪婪
            if(body.contains("告警")) {
                msg_category = 1;
            }
            // 在线提问服务请求工单[QS_ONLINE-...
            // XBANK事件工单[XBANK_EVENT...
            else if (body.contains("工单")){
                msg_category = 2;
            }else{
                msg_category = -1;
            }
        }

        if (msg_category == 1 && al_level == 0){
            if (body.contains("清除告警")){
                al_level = 4;
            }else if(body.contains("警告告警")){
                al_level = 3;
            }else if(body.contains("次要告警")){
                al_level = 2;
            }else if(body.contains("主要告警")){
                al_level = 1;
            }else{
                al_level = -1;
                //没有级别的告警，展示时容易遗漏, 这里简单处理，改为非告警
                msg_category = -1;
            }
        }


    }

    public void formatDate() {
        if (day == null)
        {
            this.day = new SimpleDateFormat("dd").format(this.date);
            this.mon = new SimpleDateFormat("MM").format(this.date);
            this.year = new SimpleDateFormat("yyyy").format(this.date);
            this.time = new SimpleDateFormat("HH:mm:ss").format(this.date);
        }
    }

    public int getRaw_id() {
        return raw_id;
    }

    public void setRaw_id(int raw_id) {
        this.raw_id = raw_id;
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        if (date>0){
            formatDate();
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public int getMsg_category() { return msg_category;  }
    public String getMsg_category(boolean flag) {
        String category;
        switch(msg_category){
            case 1:
                category = "告警";
                break;
            case 2:
                category = "工单";
                break;
            default:
                category  = "未知";
                break;
        }
        return category;

    }

    public void setMsg_category(int msg_category) {
        this.msg_category = msg_category;
    }

    public int getMsg_srouce() {
        return msg_srouce;
    }

    public void setMsg_srouce(int msg_srouce) {
        this.msg_srouce = msg_srouce;
    }

    public boolean isIs_cleared() {
        return is_cleared;
    }

    public void setIs_cleared(boolean is_cleared) {
        this.is_cleared = is_cleared;
    }

    public int getAl_level() {
        return al_level;
    }

    public String getAl_level(boolean flag) {
        String sLevel;
        switch(al_level){
            case 1:
                sLevel = "主要";
                break;
            case 2:
                sLevel = "次要";
                break;
            case 3:
                sLevel = "警告";
                break;
            default:
                sLevel = "未知";
                break;
        }
        return sLevel;
    }

    public void setAl_level(int al_level) {
        this.al_level = al_level;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDay() {
        return day;
    }

    public String getMon() {
        return mon;
    }

    public String getYear() {
        return year;
    }

    public String getTime() {
        return time;
    }

    public int getRel_raw_id() {return rel_raw_id;}

    public void setRel_raw_id(int rel_raw_id) {this.rel_raw_id = rel_raw_id; }


    public static boolean compareMsgStr(String strAlert, String strClear) {
        boolean result = false;
        Log.i(TAG, "compareMsgStr() execute.");

        try {

            if (strAlert.contains("AlphaOps")) {
                checkAlphaOps(strAlert, strClear);
                return false;
            }

            ArrayList listAlert = getThings(strAlert);
            Log.i(TAG,"compareMsgStr: ============[CLEAR]=============");
            ArrayList listClear = getThings(strClear);

            if (listAlert == null || listClear == null ||
                    "".equals(listAlert.get(0)) || "".equals(listAlert.get(0)) ||
                    "".equals(listClear.get(2)) || "".equals(listClear.get(2)) ) {
                return false;
            }

            if (listAlert.get(0).equals(listClear.get(0)) &&
                    listAlert.get(2).equals(listClear.get(2)) ) {
                Log.i(TAG,"compareMsgStr: $$$***### 比对成功!");
                result = true;
            }

        }catch(Exception e) {
            Log.i(TAG,"compareMsgStr: catch exception");
            e.printStackTrace();
        }

        return result;
    }

    // return a list as [beginStr, sysName, content, IP]
    private static ArrayList getThings(String body) {
        Log.i(TAG, "getThings() execute.");

        ArrayList list = new ArrayList<String>();

        // 级][错误提示类]集成开放平台:生产系统:BS-448:告
        // [A级][状态类][Linux]PE系统:生产系统:MPEDB04A(40.
        // [A级][Linux] 生产系统:PE系统_MPEDB04A_监控代理程
        // [A级][硬件类][Linux]PE系统:生产系统:MPEDB0
        // A级][硬件类][Linux] PE系统:生产系统:MPEDB
        // [B级][性能类][NT]外汇清算:生产系统:MFCLSVR1(40.3
        // [A级][错误提示类]NPS系统:生产系统:MNPSAPP1:告警
        // [A级][错误提示类]NPS系统:生产系统:MNPSAPP1:告警内
        // 状态类][应用日志监控]PE系统:生产系统:MPEAP
        // 状态类][应用日志监控]PE系统:生产系统:MPEAP
        // [AlphaOps 告警]【次要告警】【NPS系统-cardStatus

        // get beginStr
        //Log.i(TAG,"getThings: ###### find beginStr #####");
        int pos1 = body.indexOf("生产系统:");
        if (pos1 == -1) {
            return null;
        }
        String beginStr = body.substring(0, pos1);
        Log.i(TAG,"getThings: beginStr=" + beginStr);
        //让"主要|次要|警告" == "清除"
        beginStr=beginStr.replaceFirst("(]主要告警|]次要告警|]警告告警|]清除告警)", "]告警");
        Log.i(TAG,"getThings: beginStr=" + beginStr);
        list.add(beginStr);

        // get sysName
        //Log.i(TAG,"getThings: ###### find sysName #####");
        String sysName = "";
        int pos0 = body.lastIndexOf(']', pos1);
        //Log.i(TAG,"getThings: pos0=" + pos0 + ", pos1=" + pos1);
        if (pos1-1 >= pos0+1) {
            sysName = body.substring(pos0 + 1, pos1-1);
        }else if (pos1 >= pos0+1) {
            sysName = body.substring(pos0 + 1, pos1);
        }
        Log.i(TAG,"getThings: sysName=" + sysName);
        list.add(sysName);

        // get content
        //Log.i(TAG,"getThings: ###### find content #####");
        int contentBgn = pos1 + 5; // bypass 生产系统:
        String content = body.substring(contentBgn);

        String pattern="(发生时间|当前值|异常，已发生报错|成功解除)";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(content);
        if (m.find()) {
            //Log.i(TAG,"m.groupCount(): "+m.groupCount());
            Log.i(TAG,"getThings: Found Boundary: "+ m.group(0));
            //Log.i(TAG,"start()="+m.start() + ", end()="+m.end());
            content = content.substring(0, m.start());
        }else {
            Log.i(TAG,"getThings: Boundary not found.");
        }
        Log.i(TAG,"getThings: content=" + content);
        list.add(content);

        // get IP
        //Log.i(TAG,"getThings: ###### find ip addr #####");
        String IP="";
        String REGEX_IP0 = "((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))";
        p=Pattern.compile(REGEX_IP0);
        m=p.matcher(content);
        if (m.find()) {
            //Log.i(TAG,"m.groupCount(): "+m.groupCount());
            Log.i(TAG,"getThings: Found IP: "+ m.group(0));
            //Log.i(TAG,"start()="+m.start() + ", end()="+m.end());
            IP = m.group(0);

        }else {
            Log.i(TAG,"getThings: IP not found.");
        }
        list.add(IP);

        return list;
    }


    private static void checkAlphaOps(String strAlert, String strClear) {

        Log.i(TAG,"checkAlphaOps: AlphaOps is found.");

    }
}
