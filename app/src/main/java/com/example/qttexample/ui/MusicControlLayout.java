package com.example.qttexample.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.qttexample.R;
import com.example.qttexample.utils.TimeUtil;
import com.qttaudio.sdk.channel.ChannelEngine;

public class MusicControlLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    /*音乐时长*/
    private AppCompatTextView mTvMusicTime;
    /*播放音乐进度*/
    private AppCompatTextView mTvMusicProgress;
    private SeekBar mSbMusicProgress;
    /*人声音量*/
    private AppCompatTextView mTvVocal;
    private SeekBar mSbVocal;
    /*音调*/
    private AppCompatTextView mTvTone;
    private SeekBar mSbTone;
    /*音乐音量*/
    private AppCompatTextView mTvMusicVolume;
    private SeekBar mSbMusicVolume;
    /*播放-暂停-恢复音乐*/
    private AppCompatImageView mIvPlayMusic;
    private View view;
    private ChannelEngine mChannelEngine;
    private Context mContext;
    /*音乐声音*/
    private int musicVolume = 100;
    /*是否播放音乐*/
    private MusicPlayState musicPlayState=MusicPlayState.STOP;
    private boolean isStartTrack;
    /*当前播放进度*/
    private int currentProgress;
    /*音乐进度任务*/
    private Runnable updateMusicRunnable;
    private Handler handler = new Handler(Looper.getMainLooper());


    public MusicControlLayout(Context context, @NonNull View view, ChannelEngine mChannelEngine) {
        this.mContext = context;
        this.view = view;
        this.mChannelEngine = mChannelEngine;
        initView();
    }


    private void initView() {
        AppCompatImageView ivClose = view.findViewById(R.id.iv_close);
        AppCompatImageView ivStopMusic = view.findViewById(R.id.iv_stop_music);
        mTvMusicProgress = view.findViewById(R.id.tv_music_progress);
        mSbMusicProgress = view.findViewById(R.id.sb_music_progress);
        mTvMusicTime = view.findViewById(R.id.tv_music_time);
        mIvPlayMusic = view.findViewById(R.id.iv_play_music);
        mTvVocal = view.findViewById(R.id.tv_vocal);
        mTvTone = view.findViewById(R.id.tv_tone);
        mTvMusicVolume = view.findViewById(R.id.tv_music_volume);
        mSbVocal = view.findViewById(R.id.sb_vocal);
        mSbMusicVolume = view.findViewById(R.id.sb_music_volume);
        mSbTone = view.findViewById(R.id.sb_tone);

        mSbVocal.setOnSeekBarChangeListener(this);
        mSbMusicVolume.setOnSeekBarChangeListener(this);
        mSbTone.setOnSeekBarChangeListener(this);
        mSbMusicProgress.setOnSeekBarChangeListener(this);
        mSbMusicProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return musicPlayState==MusicPlayState.STOP;
            }
        });

        ivClose.setOnClickListener(this);
        ivStopMusic.setOnClickListener(this);
        mIvPlayMusic.setOnClickListener(this);
    }

    /**
     * 重置音乐进度
     * @param max
     */
    public void resetMusicProgress(int max) {
        if (view != null && (musicPlayState == MusicPlayState.START)) {
            mTvMusicProgress.setText("00:00");
            mSbMusicProgress.setMax(max);
            mSbMusicProgress.setProgress(0);
            mTvMusicTime.setText(TimeUtil.msecToTime(max));
        }
    }

    /**
     * 更新进度
     * @param progress 0-100%
     */
    public void updateMusicProgress(int progress) {
        if (!isStartTrack && view != null) {
            mTvMusicProgress.setText(TimeUtil.msecToTime(progress));
            mSbMusicProgress.setProgress(progress);
        }
    }

    /**
     * 伴奏进度任务
     */
    public void updateMusicProgress() {
        if (updateMusicRunnable == null) {
            updateMusicRunnable = new Runnable() {
                @Override
                public void run() {
                    if (musicPlayState == MusicPlayState.START || musicPlayState == MusicPlayState.RESUME) {
                        updateMusicProgress(mChannelEngine.getSoundPosition());
                        handler.postDelayed(updateMusicRunnable, 1000);
                    } else {
                        handler.removeCallbacks(updateMusicRunnable);
                    }
                }
            };
        }
        handler.postDelayed(updateMusicRunnable, 1000);
    }

    /**
     * 清空音乐进度更新任务
     */
    public void stopUpdateMusicProgress() {
        handler.removeCallbacksAndMessages(null);
        mIvPlayMusic.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.play));
        if(musicPlayState!=MusicPlayState.PAUSE){
            musicPlayState = MusicPlayState.STOP;
        }
    }

    /**
     * 停止音乐
     */
    public void stopMusic() {
        if (musicPlayState != MusicPlayState.STOP) {
            mChannelEngine.stopSound();
            musicPlayState = MusicPlayState.STOP;
            mIvPlayMusic.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.play));
        }
    }

    public MusicPlayState getMusicPlayState() {
        return musicPlayState;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close://关闭
                view.setVisibility(View.GONE);
                break;
            case R.id.iv_play_music://播放-暂停-恢复音乐
                    //播放音乐
                    //回调方法：
                    //onAudioRouteChanged(int code)
                    //code 0:播放回调
                    //code 1:暂停回调
                    //code 2:停止回调
                    //code 3:发生错误回调
                    //code 4:播放完成回调
                if (musicPlayState == MusicPlayState.START || musicPlayState == MusicPlayState.RESUME) {
                    musicPlayState = MusicPlayState.PAUSE;
                    mChannelEngine.pauseSound();
                    mIvPlayMusic.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.play));
                } else if (musicPlayState == MusicPlayState.PAUSE) {
                    musicPlayState = MusicPlayState.RESUME;
                    mChannelEngine.resumeSound();
                    mIvPlayMusic.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.pause));
                } else {
                    musicPlayState = MusicPlayState.START;
                    String fileName = mContext.getFilesDir().getAbsolutePath() + "/夜的钢琴曲.mp3";
                    mChannelEngine.playSound(fileName, 1, true);
                    mChannelEngine.setSoundPublishVolume(musicVolume);//设置远端音乐音量
                    mChannelEngine.setSoundPlayoutVolume(musicVolume);//设置本地端音乐音量
                    mIvPlayMusic.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.pause));
                }
                break;
            case R.id.iv_stop_music://停止播放
                stopMusic();
                musicPlayState = MusicPlayState.STOP;
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isStartTrack && seekBar.getId() == R.id.sb_music_progress) {
            currentProgress = progress;
            mTvMusicProgress.setText(TimeUtil.msecToTime(progress));
            return;
        }
        /*音调*/
        int pitch = 0;
        switch (seekBar.getId()) {
            case R.id.sb_vocal:        //人声音量调节
                /*人声音量*/
                mSbVocal.setProgress(progress);
                mTvVocal.setText("" + progress);
                mChannelEngine.adjustMicVolume(progress);
                break;
            case R.id.sb_music_volume:        //音乐音量调节
                musicVolume = progress;
                mSbMusicVolume.setProgress(progress);
                mTvMusicVolume.setText("" + musicVolume);
                mChannelEngine.setSoundVolume(musicVolume);
                break;
            case R.id.sb_tone:        //音调调节
                //-12 - 0 - 12
                if (progress == 12) {
                    pitch = 0;
                } else {
                    pitch = progress - 12;
                }
                mSbTone.setProgress(progress);
                mTvTone.setText("" + pitch);
                mChannelEngine.setSoundPitch(pitch);
                break;
            case R.id.sb_music_progress:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isStartTrack = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isStartTrack = false;
        if (seekBar.getId() == R.id.sb_music_progress) {
            updateMusicProgress(currentProgress);
            mChannelEngine.setSoundPosition(currentProgress);
        }
    }

    public enum MusicPlayState {
        START, RESUME, PAUSE, STOP
    }
}
