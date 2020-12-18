package com.example.qttexample.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qttexample.R;
import com.example.qttexample.bean.OnWheat;
import com.example.qttexample.utils.WaveView;

import java.util.ArrayList;
import java.util.List;

public class MicItemAdapter extends RecyclerView.Adapter<MicItemAdapter.MicViewHolder> {

    private List<OnWheat> datas = new ArrayList<>();
    private Context mContext;
    private long uid;
    private int[] headIcons = {R.mipmap.mic_head_icon1, R.mipmap.mic_head_icon2, R.mipmap.mic_head_icon3,
            R.mipmap.mic_head_icon4, R.mipmap.mic_head_icon5, R.mipmap.mic_head_icon6, R.mipmap.mic_head_icon7, R.mipmap.mic_head_icon8};

    public MicItemAdapter(Context context) {
        this.mContext = context;
        for (int i = 0; i < 8; i++) {
            datas.add(initBean());
        }
    }

    private OnWheat initBean() {
        OnWheat bean = new OnWheat();
        bean.uid = -1;
        bean.isMute = false;
        bean.volumeValue = 0;
        bean.headerImage = null;
        return bean;
    }

    @NonNull
    @Override
    public MicItemAdapter.MicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_safa_people, parent, false);
        return new MicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MicItemAdapter.MicViewHolder holder, final int position) {
        final OnWheat bean = datas.get(position);
        //闭麦
        holder.ivCloseMic.setVisibility(bean.isMute ? View.VISIBLE : View.GONE);
        holder.waveView.setTag(position);
        //设置音量
        if (bean.volumeValue <= 30) {
            holder.tvTrumpetValue.setText("0");
            holder.tvTrumpetValue.setVisibility(View.INVISIBLE);
            holder.waveView.stop();
        } else {
            holder.tvTrumpetValue.setText(bean.volumeValue + "");
            holder.tvTrumpetValue.setVisibility(View.VISIBLE);
            int tag = (int) holder.waveView.getTag();
            if (tag == position) {
                holder.waveView.start();
            }
        }
        if (bean.isMute) {
            holder.waveView.stop();
        }
        //昵称+头像
        if (bean.uid != -1) {
            holder.tvNickname.setText(bean.uid == uid ? uid + "(我)" : bean.uid + "");
            Drawable headDrawable = ResourcesCompat.getDrawable(mContext.getResources(), headIcons[position], null);
            holder.ivHeader.setImageDrawable(headDrawable);
            holder.ivHeader.setVisibility(View.VISIBLE);
        } else {
            holder.ivHeader.setVisibility(View.INVISIBLE);
            holder.tvNickname.setText("麦位");
        }
    }


    /**
     * 加入房间后上麦
     *
     * @param uid
     */
    public void joinWheatSeat(long uid) {
        for (OnWheat bean : datas) {
            if (uid == bean.uid) {
                return;
            }
            if (bean.uid == -1) {
                bean.uid = uid;
                notifyDataSetChanged();
                return;
            }
        }
    }

    /**
     * 是否启用闭麦
     *
     * @param isMute
     */
    public void enableCloseMic(long uid, boolean isMute) {
        for (OnWheat bean : datas) {
            if (bean.uid == uid) {
                bean.isMute = isMute;
                bean.volumeValue = 0;
                notifyDataSetChanged();
                return;
            }
        }
    }

    /*
     * 更新音量
     * */
    public void updateVolume(long uid, int value) {
        int uidIndex = getUidIndex(uid);
        if (uidIndex >= 0) {
            OnWheat bean = datas.get(uidIndex);
            if (bean.isMute) {
                return;
            }
            bean.volumeValue = value;
        }
    }

    private int getUidIndex(long uid) {
        for (int i = 0; i < datas.size(); i++) {
            OnWheat bean = datas.get(i);
            if (bean.uid == uid) {
                return i;
            }
        }
        return -1;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void removeUid(long uid) {
        for (OnWheat bean : datas) {
            if (bean.uid == uid) {
                int index = datas.indexOf(bean);
                datas.set(index, initBean());
                notifyDataSetChanged();
                return;
            }
        }
    }


    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class MicViewHolder extends RecyclerView.ViewHolder {

        public View itemView;
        /*闭麦图标*/
        private AppCompatImageView ivCloseMic;
        /*头像*/
        private AppCompatImageView ivHeader;
        /*昵称*/
        private AppCompatTextView tvNickname;
        /*音量值*/
        private AppCompatTextView tvTrumpetValue;
        private WaveView waveView;

        public MicViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivCloseMic = itemView.findViewById(R.id.iv_close_mic);
            ivHeader = itemView.findViewById(R.id.iv_header);
            tvNickname = itemView.findViewById(R.id.tv_name);
            tvTrumpetValue = itemView.findViewById(R.id.tv_trumpet_value);
            waveView = itemView.findViewById(R.id.wv_speaker);
        }
    }
}
