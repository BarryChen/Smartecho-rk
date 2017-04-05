package com.rockchip.echo.smartecho.actions;


import android.os.Bundle;

import com.rockchip.echo.smartecho.nlu.NluResult;

public abstract class Action {

    protected static NluResult.NluIntent mMatchIntent;

    public Action(NluResult.NluIntent intent) {
        mMatchIntent = intent;
    }

    public abstract void start();
    public abstract void stop();
    public abstract void pause();
    public abstract void resume();

    public abstract void prepare(Bundle data);

    public NluResult.NluIntent getMatchIntent() {
        return mMatchIntent;
    }

    public boolean isMatch(NluResult.NluIntent intent) {
        return mMatchIntent.equals(intent);
    }
}
