package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MsgBodyDiagActivity extends AppCompatActivity {
    private static final String TAG = "MsgBodyDiagActivity";
    private PopupWindow mPopupWindow;
    private String mCopiedText;
    private int lastX;
    private int lastY;
    //private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_body_diag);

        Log.i(TAG, "onCreate() executed.");

        //WindowManager.LayoutParams params = this.getWindow().getAttributes();
        //params.x = -10;
        //params.height =  WindowManager.LayoutParams.MATCH_PARENT; //对话框高度总是最大值
        //this.getWindow().setAttributes(params);

        initPopupWindow();

        MsgItem item = (MsgItem)getIntent().getSerializableExtra("msg_list_item");
        TextView textBody1 = findViewById(R.id.text_body_full);
        TextView textHead1 = findViewById(R.id.text_header);
        LinearLayout view1 = findViewById(R.id.linear_1);
        fillText(textBody1, textHead1, view1, item);

        MsgItem item2 = DataAccess.findRelatedMsg(item);
        if (item2 != null){
            TextView textBody2 = findViewById(R.id.text_body_full2);
            TextView textHead2 = findViewById(R.id.text_header2);
            LinearLayout view2 = findViewById(R.id.linear_2);
            view2.setVisibility(View.VISIBLE);
            fillText(textBody2, textHead2, view2, item2);

            if (!item2.isIs_read()){
                item2.setIs_read(true);
                item2.save();
            }
        }
    }

    private void fillText(TextView textBody, TextView textHead, View view, MsgItem item){

        textBody.setText(item.getBody());

        textBody.setMovementMethod(ScrollingMovementMethod.getInstance());
        textBody.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Log.i(TAG, "onLongClick() executed.");
                //mPopupWindow.showAsDropDown(view); // the popup shown under the textBody, not what I want
                //mPopupWindow.showAtLocation(mBtn, Gravity.CENTER, 20, 20);  // from internet, this's confused that mBtn is here
                //mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                mPopupWindow.showAtLocation(v,Gravity.LEFT|Gravity.TOP, lastX, lastY);
                mCopiedText = ((TextView) v).getText().toString();
                return false;
            }
        });
        textBody.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.i(TAG, "onTouch(): event="+ event.getAction());
                //Log.i(TAG, "onTouch(): event.x="+ event.getX() + ", event.y=" + event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int)event.getX();
                        lastY = (int)event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                //PopupMenu s = new PopupMenu(MsgBodyDiagActivity.this, v);
                //s.show(); // 不能控制显示位置
                return false;
            }
        });


        StringBuilder sb = new StringBuilder() ;
        sb.append("号码: " + item.getAddress() );
        sb.append("\n");

        sb.append("分类: " + item.getMsg_category(true));
        if (item.getAl_level() > 0 ){
            sb.append("  级别: " + item.getAl_level(true) );
        }
        if (item.isIs_cleared()){
            if(item.getSim_perc() == 100){
                sb.append("  已关联");
            }else{
                sb.append("  " + item.getSim_perc(true));
            }
            view.setBackgroundColor(0xFFE8F5E9);
        }else{
            view.setBackgroundColor(0xFFFFFFFF);
        }
        sb.append("\n");

        sb.append("日期: " + item.getYear() + "-"+ item.getMon()+"-"+item.getDay());
        sb.append("  时间: " + item.getTime() + "");
        //sb.append(", 系统=" + item.getSystem());
        //if(getIntent().getStringExtra("mode").equals("detail")){
        //    ;
        //}
        textHead.setText(sb.toString());

    }

    private void initPopupWindow() {
        Log.i(TAG, "initPopupWindow() executed.");

        View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);
        //View popupView = View.inflate(MsgBodyDiagActivity.this, R.layout.popup_window, null); // this also work
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        //mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        Button btnCopy = (Button) popupView.findViewById(R.id.btn_copy);
        //mBtn = (Button) popupView.findViewById(R.id.btn_copy);
        //mBtn=(Button) findViewById(R.id.btn_copy); // this cause crash
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mCopiedText));

                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() executed.");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop() executed.");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() executed.");

    }


}
