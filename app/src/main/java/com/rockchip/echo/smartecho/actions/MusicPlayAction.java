package com.rockchip.echo.smartecho.actions;

import android.os.Bundle;

import com.rockchip.echo.smartecho.nlu.NluResult;
import com.rockchip.echo.util.LogUtil;


/**
 * Music Play Action
 */

public class MusicPlayAction extends Action {

    public final static String DATA_URL = "url";

    private MusicPlayThread mMusicPlayThread;

    public MusicPlayAction() {
        super(NluResult.NluIntent.PLAY_MUSIC);
        mMusicPlayThread = new MusicPlayThread();
    }

    @Override
    public void prepare(Bundle data) {
        if (data == null) {
            return;
        }
        String url = data.getString(DATA_URL);
        mMusicPlayThread.setMusicUrl(url);
    }

    @Override
    public void start() {
        LogUtil.d("MusicPlayAction - start");
        mMusicPlayThread.start();
    }

    @Override
    public void stop() {
        LogUtil.d("MusicPlayAction - stop");
        mMusicPlayThread.stop();
    }

    @Override
    public void pause() {
        LogUtil.d("MusicPlayAction - pause");
        mMusicPlayThread.pause();
    }

    @Override
    public void resume() {
        LogUtil.d("MusicPlayAction - resume");
        mMusicPlayThread.resume();
    }
}
