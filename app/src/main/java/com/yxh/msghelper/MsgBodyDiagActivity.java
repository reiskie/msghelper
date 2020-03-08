package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MsgBodyDiagActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_body_diag);

        //WindowManager.LayoutParams params = this.getWindow().getAttributes();
        //params.x = -10;
        //params.height =  WindowManager.LayoutParams.MATCH_PARENT; //对话框高度总是最大值
        //this.getWindow().setAttributes(params);

        MsgItem item = (MsgItem)getIntent().getSerializableExtra("msg_item");
        TextView textBody = findViewById(R.id.text_body_full);
        textBody.setText(item.getBody());
        textBody.setMovementMethod(ScrollingMovementMethod.getInstance());

        StringBuilder sb = new StringBuilder() ;
        sb.append("日期: " + item.getYear() + item.getMon()+item.getDay());
        sb.append(", 时间: " + item.getTime() + "  ");
        sb.append(", 号码: " + item.getAddress() );
        //sb.append(", 系统=" + item.getSystem());
        //sb.append(", 级别=" + item.getAl_level(true));
        if(getIntent().getStringExtra("mode").equals("detail")){
            sb.append("\n")
                    //.append(", raw_id=" + item.getRaw_id())
                    //.append(", thread_id=" + item.getThread_id())
                    .append(", category=" + item.getMsg_category())
                    //.append(", source=" + item.getMsg_srouce())
                    .append(", is_read=" + item.isIs_read())
                    .append(", is_cleared=" + item.isIs_cleared());
        }
        TextView textHead = findViewById(R.id.text_header);
        textHead.setText(sb.toString());
    }
}
