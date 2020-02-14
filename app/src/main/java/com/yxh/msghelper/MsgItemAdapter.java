package com.yxh.msghelper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MsgItemAdapter extends RecyclerView.Adapter<MsgItemAdapter.ViewHolder> {

    private List<MsgItem> mMsgItemList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView itemAddress;
        TextView itemDate;
        TextView itemTag;
        TextView itemBody;

        public ViewHolder(View view) {
            super(view);
            itemView    = view;
            itemAddress = view.findViewById(R.id.msg_address);
            itemDate    = view.findViewById(R.id.msg_date);
            itemTag     = view.findViewById(R.id.msg_tag);
            itemBody    = view.findViewById(R.id.msg_body);
        }
    }

    public MsgItemAdapter(List<MsgItem> itemList) {
            mMsgItemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_item, parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                MsgItem item = mMsgItemList.get(position);
                Toast.makeText(view.getContext(), "你点击了View"+ item.getAddress(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                MsgItem item = mMsgItemList.get(position);

                Context context = MsgApp.getContext();
                Intent in1=new Intent(context, MsgBodyDiagActivity.class);
                in1.putExtra("msg_item", item);
                //in1.putExtra("mode", "abstract");
                in1.putExtra("mode", "detail");
                context.startActivity(in1);
                //Toast.makeText(view.getContext(), "你点击了TextView(body)"+ item.getBody(), Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MsgItem item = mMsgItemList.get(position);
        holder.itemDate.setText(item.getHourmin());
        holder.itemAddress.setText(item.getAddress());
        holder.itemTag.setText("TAG");
        holder.itemBody.setText(item.getBody());
    }

    @Override
    public int getItemCount() {
        return mMsgItemList.size();
    }

}
