package com.rockchip.echo.smartecho.actions;


import android.os.Bundle;

import com.rockchip.echo.smartecho.nlu.NluResult;

public class TextSpeakAction extends Action {

    public TextSpeakAction() {
        super(NluResult.NluIntent.SPEAK_TEXT);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void prepare(Bundle data) {

    }
}
