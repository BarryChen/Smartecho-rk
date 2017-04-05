package com.rockchip.echo.smartecho;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.rockchip.echo.util.LogUtil;

import java.io.IOException;

/**
 * Music Player
 */

public class MusicPlayer implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private String mMusicUrl;
    private String mSinger;
    private String mMusicName;

    private MediaPlayer mMediaPlayer;

    public MusicPlayer() {

    }

    public MusicPlayer(String url) {
        mMusicUrl = url;
    }

    public void setMusicUrl(String url) {
        mMusicUrl = url;
    }

    public void play() {
        LogUtil.d("MusicPlayer - play");
        if(mMusicUrl == null) {
            LogUtil.d("MusicPlayer - play - download url = null");
            return;
        }
        LogUtil.d("MusicPlayer - play - url: " + mMusicUrl);
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

    public void pause() {
        LogUtil.d("MusicPlayer - pause");
        mMediaPlayer.pause();
    }

    public void stop() {
        LogUtil.d("MusicPlayer - stop");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
//        LogUtil.d("MusicPlayer - onBufferingUpdate");
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicPlayer - onCompletion");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicPlayer - onPrepared");
        mediaPlayer.start();
    }
}