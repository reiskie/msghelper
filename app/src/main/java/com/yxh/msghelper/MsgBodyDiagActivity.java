package com.yxh.msghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.WindowManager;
import android.widget.Button;
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

        MsgItem msgItem = (MsgItem)getIntent().getSerializableExtra("msg_item");
        TextView textBody = findViewById(R.id.text_body_full);
        textBody.setText(msgItem.getBody());
        textBody.setMovementMethod(ScrollingMovementMethod.getInstance());

        StringBuilder sb = new StringBuilder() ;
        sb.append("日期: " + msgItem.getMonthday() + "  ");
        sb.append("时间: " + msgItem.getHourmin() + "  ");
        sb.append("Address: " + msgItem.getAddress() );
        if(getIntent().getStringExtra("mode").equals("detail")){
            sb.append("\n")
                    .append("_id=" + msgItem.get_id())
                    .append(", thread_id=" + msgItem.getThread_id())
                    .append(", type=" + msgItem.getType())
                    .append(", status=" + msgItem.getStatus())
                    .append(", isRead=" + msgItem.isRead())
                    .append(", subject=" + msgItem.getSubject())
                    .append(", person=" + msgItem.getPerson())
                    .append(", creator=" + msgItem.getCreator())
                    .append(", date_sent=" +
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msgItem.getDate_sent()))
                    .append(", mtu=" + msgItem.getMtu())
                    .append(", protocol=" + msgItem.getProtocol());
        }
        TextView textHead = findViewById(R.id.text_header);
        textHead.setText(sb.toString());
    }
}
