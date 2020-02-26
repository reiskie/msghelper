package com.yxh.msghelper;

import java.io.Serializable;
import java.text.SimpleDateFormat;

// refer to class Telephony.Sms.Inbox
// to read message from content://sms/inbox
public class SMSRaw implements Serializable {
    //
    private int _id;
    private int thread_id;
    private int type; // 类型 1-inbox 2-sent
    private String address;
    private long date;
    private long date_sent;
    private boolean read; // 0未读， 1已读
    private boolean seen; // 和通知有关
    private int status;  //TP-Status value for the message, or -1 if no status has been received
    private String subject;
    private String body;
    private int Person;  // reference to item in {@code content://contacts/people}
    private int protocol; //the protocol identifier code.
    private String service_center;
    private int mtu; //The MTU size of the mobile interface to which the APN connected
    private int error_code;
    private String creator; //it is usually the package name of the app which sends the message.
    //
    private String year;
    private String monthday;
    private String hourmin;

    public SMSRaw(long date, String address, String body){
        this.date = date;
        this.address=address;
        this.body = body;
        this.year = new SimpleDateFormat("yyyy").format(date);
        this.monthday =  new SimpleDateFormat("MM-dd").format(date);
        this.hourmin = new SimpleDateFormat("HH:mm").format(date);
    }

    public long getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public String getAddress() {
        return address;
    }

    public String getHourmin() { return hourmin;}

    public int get_id() {
        return _id;
    }

    public int getThread_id() {
        return thread_id;
    }

    public int getType() {
        return type;
    }

    public long getDate_sent() {
        return date_sent;
    }

    public boolean isRead() {
        return read;
    }

    public int getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public int getPerson() {
        return Person;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getMtu() {
        return mtu;
    }

    public String getCreator() {
        return creator;
    }

    public String getYear() {
        return year;
    }

    public String getMonthday() {
        return monthday;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPerson(int person) {
        Person = person;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDate_sent(long date_sent) {
        this.date_sent = date_sent;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
