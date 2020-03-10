package com.yxh.msghelper;

public class MsgGroupItem {

    String key;
    int count;
    int countBadge;

    public MsgGroupItem(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCountBadge() {
        return countBadge;
    }

    public void setCountBadge(int countBadge) {
        this.countBadge = countBadge;
    }
    
}
