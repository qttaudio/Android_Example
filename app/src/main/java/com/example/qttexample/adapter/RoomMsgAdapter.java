package com.example.qttexample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qttexample.R;

import java.util.ArrayList;
import java.util.List;

public class RoomMsgAdapter extends RecyclerView.Adapter<RoomMsgAdapter.MessageViewHolder> {

    private List<String> datas;

    public RoomMsgAdapter() {
        datas=new ArrayList<>();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_msg,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.tvMessage.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void addMsg(String msg){
        datas.add(msg);
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        public AppCompatTextView tvMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage= (AppCompatTextView) itemView;
        }
    }
}
