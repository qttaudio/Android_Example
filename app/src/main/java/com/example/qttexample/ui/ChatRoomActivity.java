package com.example.qttexample.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qttexample.R;
import com.example.qttexample.adapter.MicItemAdapter;
import com.example.qttexample.adapter.RoomMsgAdapter;
import com.example.qttexample.utils.Constant;
import com.example.qttexample.utils.FileUtil;
import com.example.qttexample.utils.SpUtil;
import com.example.qttexample.utils.StatusBarUtil;
import com.example.qttexample.utils.TimeUtil;
import com.qttaudio.sdk.channel.AudioMode;
import com.qttaudio.sdk.channel.AudioQuality;
import com.qttaudio.sdk.channel.ChannelEngine;
import com.qttaudio.sdk.channel.ChannelFactory;
import com.qttaudio.sdk.channel.ChannelObserver;
import com.qttaudio.sdk.channel.ChannelRole;
import com.qttaudio.sdk.channel.LogLevel;
import com.qttaudio.sdk.channel.RtcStat;
import com.qttaudio.sdk.channel.VolumeInfo;

public class ChatRoomActivity extends FragmentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ChannelObserver {


    private static final String TAG = "qtt_debug";
    /*上麦列表*/
    private RecyclerView mRecycleRostrum;
    /*房间消息*/
    private RecyclerView mRecycleChannelMsg;
    /*房间名*/
    private AppCompatTextView mTvRoomNumber;
    /*房间人数*/
    private AppCompatTextView mTvPeopleNumber;
    /*上麦*/
    private CheckBox mCbSwitchRole;
    /*静麦*/
    private CheckBox mCbCloseMic;
    /*关闭声音*/
    private CheckBox mCbRemoteMute;
    /*扬声器切换*/
    private CheckBox mCbSpeaker;
    /*耳返*/
    private CheckBox mCbEcho;
    /*音效与音乐*/
    private View mMusicContentView;
    //背景音乐
    private AppCompatTextView mTvBmg;
    //声效：大笑
    private AppCompatTextView mTvLaughter;
    //声效：哭声
    private AppCompatTextView mTvCry;
    /*消息适配器*/
    private RoomMsgAdapter mMsgAdapter;
    /*上麦适配器*/
    private MicItemAdapter mMicItemAdapter;
    /*音效与音乐界面管理*/
    private MusicControlLayout mMusicControl;
    /*房间人数*/
    private int mRoomPeopleCount;
    /*true 主播*/
    private boolean isBroadcaster;
    private long myUid;
    private ChannelEngine mChannelEngine;
    //房间名
    private String mRoomName;
    /*重连次数*/
    private int mReJoinCount = 0;
    //是否离场
    private boolean mIsLeave;
    private boolean isUserHeadSet = false;

    private boolean isAllRemoteMute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char_room);
        StatusBarUtil.darkMode(this);
        setUpView();
        initData();
    }


    private void setUpView() {
        AppCompatImageView ivExit = findViewById(R.id.iv_exit);
        mTvBmg = findViewById(R.id.tv_bmg);
        mTvLaughter = findViewById(R.id.tv_laughter);
        mTvCry = findViewById(R.id.tv_cry);
        mTvRoomNumber = findViewById(R.id.tv_room_number);
        mTvPeopleNumber = findViewById(R.id.tv_people_number);
        mRecycleRostrum = findViewById(R.id.recycle_rostrum);
        mRecycleChannelMsg = findViewById(R.id.recycle_channel_msg);
        mMusicContentView = ((ViewStub) findViewById(R.id.vs_music_content)).inflate();
        mMusicContentView.setVisibility(View.GONE);

        mCbSwitchRole = findViewById(R.id.cb_switch_role);
        mCbCloseMic = findViewById(R.id.cb_close_mic);
        mCbRemoteMute = findViewById(R.id.cb_remote_mute);
        mCbSpeaker = findViewById(R.id.cb_speaker);
        mCbEcho = findViewById(R.id.cb_echo);

        mCbSwitchRole.setOnCheckedChangeListener(this);
        mCbCloseMic.setOnCheckedChangeListener(this);
        mCbRemoteMute.setOnCheckedChangeListener(this);
        mCbSpeaker.setOnCheckedChangeListener(this);
        mCbEcho.setOnCheckedChangeListener(this);
        mTvBmg.setOnClickListener(this);
        mTvLaughter.setOnClickListener(this);
        mTvCry.setOnClickListener(this);
        ivExit.setOnClickListener(this);

        initRecycler();
        setEnable(false);
        setEnableEar(false);
    }

    private void initData() {
        initEngine();
        //蓝牙服务ServiceListener
        //获取手机蓝牙耳机的连接情况需要一段时间才能确认
        mMusicControl = new MusicControlLayout(this, mMusicContentView, mChannelEngine);
    }


    /**
     * 设置耳返控件enable状态
     *
     * @param isEnable
     */
    private void setEnableEar(boolean isEnable) {
        mCbEcho.setEnabled(isEnable);
        mCbEcho.setTextColor(getResources().getColor(isEnable ? R.color.color_ff0091ff : R.color.color_ffb8cefa));
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), isEnable ? R.drawable.selector_cb_bg_style : R.drawable.shape_rectangle_bg_gray_r13, null);
        mCbEcho.setBackground(drawable);
        if (!isEnable && mChannelEngine != null) {
            mChannelEngine.enableInEarMonitoring(false);
            mCbEcho.setChecked(false);
        }
    }

    /**
     * 初始化recycleView
     */
    private void initRecycler() {
        mMicItemAdapter = new MicItemAdapter(this);
        mRecycleRostrum.setLayoutManager(new GridLayoutManager(this, 4));
        mRecycleRostrum.setAdapter(mMicItemAdapter);
        mMsgAdapter = new RoomMsgAdapter();
        mRecycleChannelMsg.setAdapter(mMsgAdapter);
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        try {
            ChannelFactory.SetContext(getBaseContext());
            ChannelFactory.SetAppkey(Constant.APP_KEY);//APPKEY
            ChannelFactory.SetLogLevel(LogLevel.LOG_DEBUG);
            ChannelFactory.SetLogFile(FileUtil.initLogFile(this, 0));//LOG日志路径
            mChannelEngine = ChannelFactory.GetChannelInstance();
            if (null == mChannelEngine) {
                throw new Exception("QTT初始化失败");
            }
            //实现ChannelObserver
            mChannelEngine.setObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initJoinChannel();
    }

    /*
     * 加入房间
     * */
    private void initJoinChannel() {
        Intent intent = getIntent();
        if (intent != null) {
            mRoomName = intent.getStringExtra(Constant.ROOM_NAME);
        }
        //设置音频质量
        setAudioProfile(Constant.AUDIO_PROFILE_SPEECH_STANDARD, Constant.AUDIO_SCENARIO_CHATROOM_GAMING);
        //设置为观众,回调方法：
        //本地端-》onMuteStatusChanged(),uid==0
        //远端-》onMuteStatusChanged()
        mChannelEngine.changeRole(ChannelRole.AUDIENCE);
        //开启扬声器输出
        mChannelEngine.setSpeakerOn(true);
        //开启人声检测，延迟500ms
        //回调方法：onTalking()
        mChannelEngine.setVolumeDectection(500);
        //回调方法:
        //本地端-》onJoinSuccess(...,boolean isReconnect),参数isReconnect：false代表加入房间，true代表断线重连
        //远端-》onOtherJoin()
        mChannelEngine.join(mRoomName, Constant.TOKEN);
    }

    private int setAudioProfile(int profile, int scenario) {

        AudioQuality quality = AudioQuality.AUDIO_QUALITY_MUSIC_STEREO;
        AudioMode mode = AudioMode.AUDIO_MODE_MIX;

        return mChannelEngine.setAudioConfig(quality, mode);
    }

    /**
     * 发生频道消息
     *
     * @param msg
     */
    private void sendChannelMsg(String msg) {
        mMsgAdapter.addMsg(TimeUtil.getCurrentDateStr("HH:mm:ss.SSS") + ", 用户" + msg);
        mRecycleChannelMsg.smoothScrollToPosition(mMsgAdapter.getItemCount());
    }


    /**
     * 设置频道人数
     *
     * @param in
     */
    private void setChannelPeopleCount(boolean in) {
        if (in) {
            mRoomPeopleCount++;
        } else {
            mRoomPeopleCount--;
        }
        mTvPeopleNumber.setText(String.valueOf(mRoomPeopleCount));
    }


    /**
     * 退出频道
     */
    private void leaveChannel() {
        //回调方法:
        //本地端-》onLeave()
        //远端-》onOtherLeave();
        mChannelEngine.leave();
        finish();
    }

    /**
     * 非主播禁用：播放音乐，音效：大笑，哭声，闭麦功能
     *
     * @param b
     */
    private void setEnable(boolean b) {
        mTvBmg.setEnabled(b);
        mTvCry.setEnabled(b);
        mTvLaughter.setEnabled(b);
        mCbCloseMic.setEnabled(b);
        mCbCloseMic.setChecked(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_switch_role://上下麦
                //设置为观众,回调方法：
                //本地端-》onMuteStatusChanged(),uid==0为本地用户
                //远端-》onMuteStatusChanged()
                mChannelEngine.changeRole(isChecked ? ChannelRole.TALKER : ChannelRole.AUDIENCE);
                if (!isChecked) {
                    mMusicControl.stopMusic();
                    mChannelEngine.stopAllEffects();
                }
                mCbSwitchRole.setText(isChecked ? "下麦" : "上麦");
                break;
            case R.id.cb_close_mic://闭麦
                //回调方法：onMuteStatusChanged() uid==0为本地用户
                mChannelEngine.mute(0, isChecked);
                mCbCloseMic.setText(isChecked ? "开麦" : "静麦");
                mMicItemAdapter.enableCloseMic(myUid, isChecked);
                break;
            case R.id.cb_remote_mute://关闭声音-》屏蔽所有人的声音
                isAllRemoteMute = isChecked;
                mChannelEngine.muteAllRemote(isChecked);
                mCbRemoteMute.setText(isChecked ? "开启声音" : "关闭声音");
                break;
            case R.id.cb_speaker://外放
                mChannelEngine.setSpeakerOn(isChecked);
                mCbSpeaker.setText(isChecked ? "关闭外放" : "开启外放");
                break;
            case R.id.cb_echo://开启耳返
                mChannelEngine.enableInEarMonitoring(isChecked);
                mCbEcho.setText(isChecked ? "关闭耳返" : "开启耳返");
                mCbEcho.setTextColor(getResources().getColor(isChecked ? R.color.color_white : R.color.color_ff0091ff));
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_bmg://音乐与音效
                mMusicContentView.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_laughter://大笑
                //回调方法：onEffectFinished 音效完成
                mChannelEngine.playEffect(R.id.tv_laughter, getFilesDir().getAbsolutePath() + "/10190.mp3", 1, true);
                break;
            case R.id.tv_cry://哭声
                //回调方法：onEffectFinished 音效完成
                mChannelEngine.playEffect(R.id.tv_cry, getFilesDir().getAbsolutePath() + "/4789.mp3", 1, true);
                break;
            case R.id.iv_exit://退出房间
                leaveChannel();
                break;
        }
    }

    /**
     * 设置背景音乐样式
     *
     * @param b
     */
    private void setBmgViewStyle(boolean b) {
        mTvBmg.setBackgroundDrawable(getResources().getDrawable(b ? R.drawable.shape_rectangle_bg_blue_r13 : R.drawable.shape_rectangle_bg_white));
        mTvBmg.setTextColor(getResources().getColor(b ? R.color.color_white : R.color.color_ff0091ff));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leaveChannel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChannelFactory.Destroy();
    }


    /**
     * 加入房间成功
     *
     * @param roomName    房间名
     * @param uid         id
     * @param role        角色 主播/观众
     * @param mute        true 静音 false 非静音
     * @param isReconnect true 重新连接  false 加入房间
     */
    @Override
    public void onJoinSuccess(final String roomName, final long uid, final ChannelRole role, final boolean mute, boolean isReconnect) {
        if (mIsLeave) {
            return;
        }
        mRoomName = roomName;
        Log.e(TAG, "onJoinSuccess roomId:" + roomName + "   uid:" + uid + "  role:" + role + "  ");
        if (isReconnect) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChatRoomActivity.this, "重连成功", Toast.LENGTH_SHORT).show();
                    /**
                     * 断线重连后，如果重连前和重连后角色状态一样，不走onSelfRoleChanged
                     * 比如：断线前主播，重连后主播
                     * */
                    if ((isBroadcaster && role.value() != 0) || !isBroadcaster && role.value() != 1) {
                        onRoleStatusChanged(0, role);
                    }
                    mMicItemAdapter.enableCloseMic(uid, mute);
                }
            });
            mReJoinCount = 0;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myUid = uid;
                    mTvRoomNumber.setText(roomName);
                    sendChannelMsg(uid + "(我)进入房间了.");
                    setChannelPeopleCount(true);
                    mMicItemAdapter.setUid(uid);
                    SpUtil.putString(ChatRoomActivity.this, Constant.SP_KEY_ROOM_NAME, roomName);
                }
            });
        }
    }


    /**
     * 远端用户加入房间
     *
     * @param l           uid
     * @param channelRole 主播 0 观众 1
     * @param b           mute
     */
    @Override
    public void onOtherJoin(final long l, final ChannelRole channelRole, final boolean b) {
        Log.d(TAG, "onOtherJoin: uid:" + l + "   role:" + channelRole);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (channelRole.value() == 0) {
                    mMicItemAdapter.joinWheatSeat(l);
                    mMicItemAdapter.enableCloseMic(l, b);
                }
                sendChannelMsg(l + "进入房间.");
                setChannelPeopleCount(true);
            }
        });
    }

    /**
     * 加入房间失败
     *
     * @param i code
     * @param s msg
     */
    @Override
    public void onJoinFail(final int i, final String s) {
        Log.d(TAG, "onJoinFail: code: " + i + " msg: " + s);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatRoomActivity.this, "加入房间失败，code=" + i + "  msg=" + s, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 人声检查--音量回调
     *
     * @param volumeInfos 用户信息以及音量值
     * @param size        volumeInfos长度
     */
    @Override
    public void onTalking(final VolumeInfo[] volumeInfos, final int size) {
        if (size < 1) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < size; i++) {
                    VolumeInfo v = volumeInfos[i];
                    mMicItemAdapter.updateVolume(v.uid == 0 ? myUid : v.uid, isAllRemoteMute ? 0 : v.volume);
                }
                mMicItemAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 静麦回调
     *
     * @param l uid，uid==0 是自己
     * @param b mute
     */
    @Override
    public void onMuteStatusChanged(final long l, final boolean b) {
        Log.d(TAG, "onMuteStatusChanged: " + l + " mute " + b);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (l != 0) {
                    mMicItemAdapter.enableCloseMic(l, b);
                }
            }
        });
    }

    /**
     * 角色切换回调
     *
     * @param l
     * @param channelRole
     */
    @Override
    public void onRoleStatusChanged(final long l, final ChannelRole channelRole) {
        Log.d(TAG, "onRoleStatusChanged: " + l + " role  " + channelRole + "   " + myUid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int value = channelRole.value();
                if (l == 0) {
                    if (value == 0) {
                        sendChannelMsg(myUid + "(我)上麦了.");
                        isBroadcaster = true;
                        mMicItemAdapter.joinWheatSeat(myUid);
                    } else {
                        sendChannelMsg(myUid + "(我)下麦了.");
                        isBroadcaster = false;
                        mMicItemAdapter.removeUid(myUid);
                    }
                    setEnable(isBroadcaster);
                } else {
                    if (value == 0) {
                        sendChannelMsg(l + "上麦了.");
                        mMicItemAdapter.joinWheatSeat(l);
                    } else {
                        sendChannelMsg(l + "下麦了.");
                        mMicItemAdapter.removeUid(l);
                    }
                }

            }
        });
    }

    @Override
    public void onNetworkStats(long l, int i, int i1, RtcStat rtcStat) {

    }

    /**
     * 离开房间
     */
    @Override
    public void onLeave() {
        Log.d(TAG, "onLeave: ");
        mIsLeave = true;
    }

    /**
     * 远端用户退出房间
     *
     * @param l           uid
     * @param channelRole 0 主播 1观众
     */
    @Override
    public void onOtherLeave(final long l, final ChannelRole channelRole) {
        Log.d(TAG, "onOtherMuted: " + l);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //只关心上麦的人离开房间
                if (channelRole.value() == 0) {
                    mMicItemAdapter.removeUid(l);
                }
                sendChannelMsg(l + "退出房间了.");
                setChannelPeopleCount(false);
            }
        });
    }

    /**
     * 连接中断
     */
    @Override
    public void onConnectionBreak() {
        Log.d(TAG, "onConnectionBreak: ");
        if (mIsLeave) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatRoomActivity.this, "网络断开，正在重连...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 连接丢失
     */
    @Override
    public void onConnectionLost() {
        Log.d(TAG, "onConnectionLost: ");
    }

    @Override
    public void onError(final int err, final String s) {
        Log.d(TAG, "onError: " + err);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatRoomActivity.this, "发生错误，code=" + err + "  msg: " + err, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onWarning(int i, String s) {

    }


    /**
     * 音源输出/输入切换回调
     *
     * @param i
     */
    @Override
    public void onAudioRouteChanged(final int i) {
        Log.d(TAG, "onAudioRouteChanged: " + i);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setEnableEar(i == 2 || i == 3);
            }
        });
    }


    /**
     * 音乐播放状态回调
     *
     * @param i 0，1，2，3，4，
     */
    @Override
    public void onSoundStateChanged(int i) {
        Log.d("onSoundStateChanged", "onSoundStateChanged: " + i);
        switch (i) {
            case 0://播放
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMusicControl.getMusicPlayState() == MusicControlLayout.MusicPlayState.START) {
                            mMusicControl.resetMusicProgress(mChannelEngine.getSoundDuration());
                        }
                        mMusicControl.updateMusicProgress();
                        setBmgViewStyle(true);
                    }
                });
                break;
            case 2://停止
            case 4://播放完成
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMusicControl.stopUpdateMusicProgress();
                        mMusicControl.updateMusicProgress(0);
                        setBmgViewStyle(false);
                    }
                });
                break;
            case 1://暂停
            case 3://发生错误
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMusicControl.stopUpdateMusicProgress();
                        setBmgViewStyle(false);
                    }
                });
                break;
        }
    }

    /**
     * 场景音效播放完成回调
     *
     * @param i id
     */
    @Override
    public void onEffectFinished(int i) {
        Log.d(TAG, "onEffectFinished: " + i);
    }

}
