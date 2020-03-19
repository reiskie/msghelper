package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MsgBodyDiagActivity extends AppCompatActivity {
    private PopupWindow mPopupWindow;
    private String mCopiedText;
    //private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_body_diag);

        //WindowManager.LayoutParams params = this.getWindow().getAttributes();
        //params.x = -10;
        //params.height =  WindowManager.LayoutParams.MATCH_PARENT; //对话框高度总是最大值
        //this.getWindow().setAttributes(params);

        initPopupWindow();

        MsgItem item = (MsgItem)getIntent().getSerializableExtra("msg_list_item");
        TextView textBody = findViewById(R.id.text_body_full);
        textBody.setText(item.getBody());
        textBody.setMovementMethod(ScrollingMovementMethod.getInstance());
        textBody.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                //mPopupWindow.showAsDropDown(view); // the popup shown under the textBody, not what I want
                //mPopupWindow.showAtLocation(mBtn, Gravity.CENTER, 20, 20);  // from internet, this's confused that mBtn is here
                mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                mCopiedText = ((TextView) view).getText().toString();
            }
        });

        StringBuilder sb = new StringBuilder() ;
        sb.append("号码: " + item.getAddress() + "\n");
        sb.append("分类: " + item.getMsg_category(true));
        sb.append(" 级别: " + item.getAl_level(true) + "\n");
        sb.append("日期: " + item.getYear() + "-"+ item.getMon()+"-"+item.getDay());
        sb.append(" 时间: " + item.getTime() + "");
        //sb.append(", 系统=" + item.getSystem());
        if(getIntent().getStringExtra("mode").equals("detail")){
            sb.append("")
                    //.append(", raw_id=" + item.getRaw_id())
                    //.append(", thread_id=" + item.getThread_id())
                    //.append(", source=" + item.getMsg_srouce())
                    //.append(", is_read=" + item.isIs_read())
                    //.append(", is_cleared=" + item.isIs_cleared())
            ;
        }
        TextView textHead = findViewById(R.id.text_header);
        textHead.setText(sb.toString());
    }

    private void initPopupWindow() {

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

}
