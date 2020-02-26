package com.yxh.msghelper;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.text.SimpleDateFormat;

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
    private int msg_category; // 0-未知，1-告警，2-工单
    private int msg_srouce; // 0-未知，1-patrol, 2-zabbix, 3-alphaOps，4-itoms，5-自动化，iPaas
    private boolean is_cleared; // 告警是否清除
    private int al_level; // 0-未知，1-主要，2-次要，3-警告
    private String system; // 系统
    @Column(ignore = true)
    private String day;
    @Column(ignore = true)
    private String mon;
    @Column(ignore = true)
    private String year;
    @Column(ignore = true)
    private String time;

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
    }

    public void initDate() {
        this.day = new SimpleDateFormat("dd").format(this.date);
        this.mon = new SimpleDateFormat("MM").format(this.date);
        this.year = new SimpleDateFormat("yyyy").format(this.date);
        this.time = new SimpleDateFormat("HH:mm:SS").format(this.date);
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

    public int getMsg_category() {
        return msg_category;
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
}
