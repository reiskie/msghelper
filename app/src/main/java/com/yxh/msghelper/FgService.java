package com.yxh.msghelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import me.leolin.shortcutbadger.ShortcutBadger;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FgService extends Service {
    private static final String TAG = "FgService";
    private DataAccess mDataAccess;
    private MsgGroupActivity.ActivityGroupUpdater groupUpdater;
    private SMSContentObserver smsContentObserver;
    private FgBinder mBinder = this.new FgBinder();
    private NotificationValuesHolder lastHolder;
    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean  handleMessage(Message msg) {
            Log.i(TAG,"Handler:handleMessage():ThreadID = " + Thread.currentThread().getId());
            switch (msg.what) {
                case 1:
                    //String outbox = (String) msg.obj;
                    //Toast.makeText(FgService.this, "FgService:handleMessage: msg="+outbox, Toast.LENGTH_LONG).show();
                    int num = ((Integer)msg.obj).intValue();

                    if (groupUpdater != null) {
                        groupUpdater.update();
                    }
                    updateNotification();
                    break;
                default:
                    break;
            }

            return false;
        }
    });

    class FgBinder extends Binder {

        public void setGroupUpdater(MsgGroupActivity.ActivityGroupUpdater updater){
            groupUpdater = updater;
        }
        public void triggerReadSmsAsync(){
            FgService.this.triggerReadSmsAsync();
        }
        public void triggerUpdateNotification(){FgService.this.updateNotification();}
        public List<MsgGroupItem> getMsgGroupResult(String groupKey, boolean onlyForBadge) {
            return FgService.this.getMsgGroupResult(groupKey, onlyForBadge);
        }

        public int getStatus(){
            int status=0; // 0-空闲，1-忙碌（读短信，写入自己的库）
            return status;
        }
    }

    public FgService() {
        Log.i(TAG,"FgService():ThreadID = " + Thread.currentThread().getId());
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate:ThreadID = " + Thread.currentThread().getId());
        super.onCreate();
        mDataAccess = new DataAccess();
        startForeground(1, getNotification("信息监控中..."));
        registerContentObservers();
        UtilSound.initSound();
    }

    private void registerContentObservers(){
        smsContentObserver = new SMSContentObserver(this, mHandler);
        Uri smsUri = Uri.parse("content://sms");
        getContentResolver().registerContentObserver(smsUri,
                true, smsContentObserver);

    }

    public List<MsgGroupItem> getMsgGroupResult(String groupKey, boolean onlyForBadge){
        List<MsgGroupItem> list = new ArrayList<MsgGroupItem>();
        Map<String, Integer> groupMap = null;
        Map<String, Integer> badgeMap = null;

        if (onlyForBadge){
            badgeMap = mDataAccess.aggregateMsgfromDB(groupKey, "is_read = 0");
            groupMap = badgeMap;
        }else{
            groupMap = mDataAccess.aggregateMsgfromDB(groupKey);
            badgeMap = mDataAccess.aggregateMsgfromDB(groupKey, "is_read = 0");
        }

        for (String key : groupMap.keySet()) {
            MsgGroupItem item = new MsgGroupItem(key);
            item.setCount(groupMap.get(key));
            if(badgeMap.containsKey(key)){
                item.setCountBadge(badgeMap.get(key));
            }// if not in badgeMap, default value of item.countBadge is 0, that's right.
            list.add(item);
        }
        return list;
    }

    public void updateNotification(){

        List<MsgGroupItem> badgeCategory = getMsgGroupResult("msg_category", true);
        List<MsgGroupItem> badgeLevel    = getMsgGroupResult("al_level", true);

        NotificationValuesHolder holder = new NotificationValuesHolder();

        for (MsgGroupItem item:badgeCategory){
            if ("-1".equals(item.getKey())) {
                holder.setBadgeOther(item.getCountBadge());
            }else if ("2".equals(item.getKey())){
                holder.setBadgeWorksheet(item.getCountBadge());
            }else{
            }
        }
        for (MsgGroupItem item:badgeLevel){
            if ("1".equals(item.getKey())){
                holder.setBadgeMajor(item.getCountBadge());
            }else if ("2".equals(item.getKey())){
                holder.setBadgeMinor(item.getCountBadge());
            }else if ("3".equals(item.getKey())){
                holder.setBadgeTrivial(item.getCountBadge());
            }else if ("4".equals(item.getKey())){
                holder.setBadgeClear(item.getCountBadge());
            }else{
            }
        }

        // 有变化时才发通知
        if (lastHolder == null || !holder.equals(lastHolder)){
            String str = holder.getString();
            int badgeCount = holder.getWholeNumber();
            Log.i(TAG, "notification str=" + str + ", badgeCount=" +badgeCount);
            Notification notification = getNotification(str);
            ShortcutBadger.applyCount(this, badgeCount);
            //ShortcutBadger.applyNotification(this, notification, 100);
            getNotiManager().notify(1, notification);
            //getNotiManager().notify(2, getNotification(str));
            //test: 每个ID一条通知
        }
        if (lastHolder == null && holder.getBadgeMajor() > 0
                || lastHolder != null && holder.isMajorIncreased(lastHolder)){
            UtilSound.play(true);
        }
        lastHolder = holder;
    }

    private NotificationManager getNotiManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String str){

        String CHNL_ID="yxh.chnl_id.1";

        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            //只在Android O之上需要channel
            NotificationChannel channel =
                    new NotificationChannel(CHNL_ID,"UnReadMsg", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null,null);
            //channel.enableLights(true);
            //channel.setLightColor(Color.RED);
            //channel.setShowBadge(true); // not work
            getNotiManager().createNotificationChannel(channel);
        }

        Intent intent1 = new Intent(this, MsgGroupActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent1,0);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this, CHNL_ID);
        builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setSound(null);
        //builder.setContentTitle("未读");
        builder.setContentTitle(str);
        //builder.setContentText(str);
        //builder.setStyle(new NotificationCompat.BigTextStyle().bigText(str));
        //builder.setNumber(10); // not work
        return builder.build();
    }

    public void triggerReadSmsAsync(){
        smsContentObserver.triggerReadSmsAsync();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);
        Log.i(TAG,"onStratCommand: executed. res = " + res +
                ", threadId = " + Thread.currentThread().getId());
        // 0 - START_STICKY_COMPATIBILITY
        // 1 - START_STICKY
        return res;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy: executed.");
        super.onDestroy();
        if (smsContentObserver != null) {
            getContentResolver().unregisterContentObserver(smsContentObserver);
            smsContentObserver = null;
        }

        UtilSound.cleanup();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i(TAG, "onBind: executed");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG,"onRebind: executed.");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Boolean res = super.onUnbind(intent);
        Log.i(TAG,"onUnbind: executed.super.res="+res);
        return true;
        // 返回ture/false影响下一次:
        // -- bindService()会不会触发onBind(), onRebind()
        // -- unbindService()会不会触发onUnBind()
        // 前提是两次中间service没有被销毁、重建
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG,"onTaskRemoved: executed.");


        //https://android.stackexchange.com/questions/32697/what-is-the-offical-name-of-the-third-on-screen-button
        //Overview: Opens a list of thumbnail images of apps and Chrome tabs you’ve worked with recently.
        //          To open an app, touch it. To remove a thumbnail from the list, swipe it left or right.
        //          recent-apps-list
        // 三个键： home/back/task(menu, overview)

        // https://stackoverflow.com/questions/30525784/android-keep-service-running-when-app-is-killed
        // onTaskRemoved will be called when activity is swiped after 任务键/菜单键 is touched.
        // 在onTaskRemoved 发送广播，注册一个广播接收器，在里面重新启动这个服务。
        // Intent intent = new Intent("com.android.ServiceStopped");
        // sendBroadcast(intent);

        /* 行为不同
                                  荣耀(7.0)              mate10(8.1)               mate20pro(9)           mate20pro 打开应用自动启动3个选项过一会
        按返回退出活动             服务通知还有           服务通知还有              服务通知还有            服务通知还有
        按任务键然后kill活动       服务通知消失           服务通知还有              服务通知消失            服务通知还有
                                  进入onTaskRemoved      进入onTaskRemoved         没进onTaskRemoved      进入onTaskRemoved
        开发选项服务列表中         没有本服务             有本服务                  没有本服务              有本服务
        在服务列表中停止           --                     服务通知消失              --                     通知服务消失
                                                         进入onDestroy                                    没进入onDestroy
        */
    }



    class NotificationValuesHolder{
        int badgeOther ;
        int badgeWorksheet ;
        int badgeMajor ;
        int badgeMinor ;
        int badgeTrivial ;
        int badgeClear ;

        int getWholeNumber(){
            return badgeMajor + badgeMinor + badgeTrivial + badgeClear + badgeWorksheet + badgeOther;
        }

        public String getString(){
            String str = new String("主: "+ badgeMajor + "  次: " + badgeMinor
                    + "  警: " + badgeTrivial + " 清: " + badgeClear+ " 工: " + badgeWorksheet
                    + "  其: " + badgeOther);
            return str;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            //return super.equals(obj);
            NotificationValuesHolder ob=(NotificationValuesHolder) obj;
            return (
                    this.badgeWorksheet == ob.getBadgeWorksheet()
                    && this.badgeOther == ob.getBadgeOther()
                    && this.badgeMajor == ob.getBadgeMajor()
                    && this.badgeMinor == ob.getBadgeMinor()
                    && this.badgeTrivial == ob.getBadgeTrivial()
                    && this.badgeClear == ob.getBadgeClear()
                   );
        }

        public boolean isMajorIncreased(@Nullable Object obj) {
            //return super.equals(obj);
            NotificationValuesHolder ob=(NotificationValuesHolder) obj;
            return (
                    this.badgeMajor > ob.getBadgeMajor()
            );
        }

        public void setBadgeOther(int badgeOther) {
            this.badgeOther = badgeOther;
        }

        public void setBadgeWorksheet(int badgeWorksheet) {
            this.badgeWorksheet = badgeWorksheet;
        }

        public void setBadgeMajor(int badgeMajor) {
            this.badgeMajor = badgeMajor;
        }

        public void setBadgeMinor(int badgeMinor) {
            this.badgeMinor = badgeMinor;
        }

        public void setBadgeTrivial(int badgeTrivial) {
            this.badgeTrivial = badgeTrivial;
        }

        public void setBadgeClear(int badgeClear) {
            this.badgeClear = badgeClear;
        }

        public int getBadgeOther() {
            return badgeOther;
        }

        public int getBadgeWorksheet() {
            return badgeWorksheet;
        }

        public int getBadgeMajor() {
            return badgeMajor;
        }

        public int getBadgeMinor() {
            return badgeMinor;
        }

        public int getBadgeTrivial() {
            return badgeTrivial;
        }

        public int getBadgeClear() {
            return badgeClear;
        }
    }

}
