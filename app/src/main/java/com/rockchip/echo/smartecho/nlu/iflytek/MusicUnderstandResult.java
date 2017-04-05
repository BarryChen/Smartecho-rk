package com.rockchip.echo.smartecho.nlu.iflytek;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.rockchip.echo.smartecho.SmartEchoComImpl;
import com.rockchip.echo.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class MusicUnderstandResult extends TextUnderstandResult implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private String mDownloadUrl;
    private String mSinger;
    private String mMusicName;

    private MediaPlayer mMediaPlayer;

    @Override
    protected String parser(String understandText) {
        JSONObject textobj = null;
        String ttsText = null;
        try {
            textobj = new JSONObject(understandText);
            if(textobj == null) {
                return null;
            }
            JSONObject data = textobj.getJSONObject("data");
            JSONArray result = data.getJSONArray("result");
            JSONObject firstResult = result.getJSONObject(0);
            mSinger = firstResult.getString("singer");
            mMusicName = firstResult.getString("name");
            String url = firstResult.getString("downloadUrl");
            if (url != null) {
                mDownloadUrl = url.replace("\\/", "/");
            }
            ttsText = "为你播放" + mMusicName;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ttsText;
    }


    public void playMusic() {
        LogUtil.d("MusicUnderstandResult - play");
        if(mDownloadUrl == null) {
            LogUtil.d("MusicUnderstandResult - play - download url = null");
            return;
        }
        LogUtil.d("MusicUnderstandResult - play - url: " + mDownloadUrl);
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
            mMediaPlayer.setDataSource(mDownloadUrl); // 设置数据源
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

    public void pauseMusic() {
        LogUtil.d("MusicUnderstandResult - pause");
        mMediaPlayer.pause();
    }

    public void stopMusic() {
        LogUtil.d("MusicUnderstandResult - stop");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onStartAction(SmartEchoComImpl smartEchoComImpl) {
        LogUtil.d("MusicUnderstandResult - onStartAction");
        playMusic();
    }

    @Override
    public void onStopAction(SmartEchoComImpl smartEchoComImpl) {
        LogUtil.d("MusicUnderstandResult - onStopAction");
        stopMusic();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
//        LogUtil.d("MusicUnderstandResult - onBufferingUpdate");
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicUnderstandResult - onPrepared");
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.d("MusicUnderstandResult - onCompletion");
    }
}
