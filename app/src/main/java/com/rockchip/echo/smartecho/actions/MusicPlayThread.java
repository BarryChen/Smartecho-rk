package com.rockchip.echo.smartecho.actions;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.rockchip.echo.util.LogUtil;

import java.io.IOException;


public class MusicPlayThread implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    private int mLastPlayPos;
    private String mMusicUrl;
    private String mSinger;
    private String mMusicName;

    public MusicPlayThread() {
        initMusicPlayerThread();
    }

    public void setMusicUrl(String url) {
        mMusicUrl = url;
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public void start() {
        LogUtil.d("MusicPlayThread - start");
        if (mPlayMusicHandler != null) {
            Message msg = mPlayMusicHandler.obtainMessage(MUSIC_HANDLER_MSG_PLAY_MUSIC);
            msg.sendToTarget();
        }
    }

    public void stop() {
        LogUtil.d("MusicPlayThread - stop");
        if (mPlayMusicHandler != null) {
            Message msg = mPlayMusicHandler.obtainMessage(MUSIC_HANDLER_MSG_STOP_MUSIC);
            msg.sendToTarget();
        }
    }

    public void pause() {
        LogUtil.d("MusicPlayThread - pause");
        if (mPlayMusicHandler != null) {
            Message msg = mPlayMusicHandler.obtainMessage(MUSIC_HANDLER_MSG_PAUSE_MUSIC);
            msg.sendToTarget();
        }
    }

    public void resume() {
        LogUtil.d("MusicPlayThread - resume");
        if (mPlayMusicHandler != null) {
            Message msg = mPlayMusicHandler.obtainMessage(MUSIC_HANDLER_MSG_RESUME_MUSIC);
            msg.sendToTarget();
        }
    }

    private static final int MUSIC_HANDLER_MSG_PLAY_MUSIC = 1;
    private static final int MUSIC_HANDLER_MSG_STOP_MUSIC = 2;
    private static final int MUSIC_HANDLER_MSG_PAUSE_MUSIC = 3;
    private static final int MUSIC_HANDLER_MSG_RESUME_MUSIC = 4;

    private HandlerThread mPlayMusicHandlerThread;
    private Handler mPlayMusicHandler;
    private Handler.Callback mPlayMusicHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MUSIC_HANDLER_MSG_PLAY_MUSIC:
                    doPlayMusic();
                    break;
                case MUSIC_HANDLER_MSG_STOP_MUSIC:
                    doStopMusic();
                    break;
                case MUSIC_HANDLER_MSG_PAUSE_MUSIC:
                    doPauseMusic();
                    break;
                case MUSIC_HANDLER_MSG_RESUME_MUSIC:
                    doResumeMusic();
                    break;
                default:break;
            }
            return true;
        }
    };

    private void initMusicPlayerThread() {
        mPlayMusicHandlerThread = new HandlerThread("MusicHandlerThread");
        mPlayMusicHandlerThread.start();
        mPlayMusicHandler = new Handler(mPlayMusicHandlerThread.getLooper(),
                mPlayMusicHandlerCallback);
    }

    private void doPlayMusic() {
        LogUtil.d("MusicPlayThread - doPlayMusic");
        if(mMusicUrl == null) {
            LogUtil.d("MusicPlayThread - start - download url = null");
            return;
        }
        LogUtil.d("MusicPlayThread - start - url: " + mMusicUrl);
        if(mMediaPlayer == null) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusicUrl); // 设置数据源
            mMediaPlayer.prepare(); // prepare自动播放
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doStopMusic() {
        LogUtil.d("MusicPlayThread - doStopMusic");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mLastPlayPos = 0;
        }
    }

    private void doPauseMusic() {
        LogUtil.d("MusicPlayThread - doPauseMusic");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mLastPlayPos = mMediaPlayer.getCurrentPosition();
        }
    }

    private void doResumeMusic() {
        LogUtil.d("MusicPlayThread - doResumeMusic");
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mLastPlayPos);
            mMediaPlayer.start();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
//        LogUtil.d("MusicPlayThread - onBufferingUpdate");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicPlayThread - onCompletion");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicPlayThread - onPrepared");
        LogUtil.d("MusicPlayThread - start play music");
        mediaPlayer.start();
    }
}
