package com.yxh.msghelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.fonts.FontStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MsgItemAdapter extends RecyclerView.Adapter<MsgItemAdapter.ViewHolder> {

    private List<MsgItem> mMsgItemList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        View linearHead;
        View linearDigest;
        TextView itemDate;
        TextView itemTime;
        TextView itemTag;
        TextView itemBody;

        public ViewHolder(View view) {
            super(view);
            itemView    = view;
            linearHead  = view.findViewById(R.id.linear_head);
            linearDigest= view.findViewById(R.id.linear_digest);
            itemBody    = view.findViewById(R.id.msg_body);
            itemDate    = view.findViewById(R.id.msg_date);
            itemTime    = view.findViewById(R.id.msg_time);
            itemTag     = view.findViewById(R.id.msg_tag);
        }
    }

    public MsgItemAdapter(List<MsgItem> itemList) {
            mMsgItemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_list_item, parent,false);

        final ViewHolder holder = new ViewHolder(view);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                MsgItem item = mMsgItemList.get(position);
                //Toast.makeText(view.getContext(), "你点击了View" + item.getAddress(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                MsgItem item = mMsgItemList.get(position);

                if (item.getInternal_flag() != 1) {
                    Context context = MsgApp.getContext();
                    Intent in1 = new Intent(context, MsgBodyDiagActivity.class);
                    in1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    in1.putExtra("msg_list_item", item);
                    //in1.putExtra("mode", "abstract");
                    in1.putExtra("mode", "detail");
                    context.startActivity(in1);
                    //Toast.makeText(view.getContext(), "你点击了TextView(body)"+ item.getBody(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MsgItem item = mMsgItemList.get(position);

        if (item.getInternal_flag() != 1){

            holder.itemBody.setText(item.getBody());
            holder.itemDate.setText(item.getMon()+"月"+item.getDay()+"日");
            holder.itemTime.setText(item.getTime());

            if(item.getMsg_category() == 1 && item.getRel_raw_id() > 0){
                holder.itemTag.setText(item.getSim_perc(true) );

                //列表很长时显示有bug，没设置颜色的行，也会显示颜色
                //0405 解决，需要在else里面设置默认颜色，可能系统会复用list item的组件
                holder.linearHead.setBackgroundColor(MsgApp.getClearedMsgColor());
                holder.linearDigest.setBackgroundColor(MsgApp.getClearedMsgColor());
                //holder.itemDate.setBackgroundColor(0xFFE8F5E9);
                //holder.itemTime.setBackgroundColor(0xFFE8F5E9);
                //holder.itemTag.setBackgroundColor(0xFFE8F5E9);
                //holder.itemBody.setBackgroundColor(0xFFE8F5E9);
            }else{
                holder.itemTag.setText("");

                holder.linearHead.setBackgroundColor(MsgApp.getBackgroundColor());
                holder.linearDigest.setBackgroundColor(MsgApp.getBackgroundColor());
                //holder.itemBody.setBackgroundColor(0xFFFFFFFF);
            }
        }else{
            holder.itemBody.setText("");
            holder.itemDate.setText("");
            holder.itemTime.setText("");
            holder.itemTag.setText("");

            holder.linearHead.setBackgroundColor(MsgApp.getBackgroundColor());
            holder.linearDigest.setBackgroundColor(MsgApp.getBackgroundColor());
            //holder.itemBody.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMsgItemList.size();
    }

}
