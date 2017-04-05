package com.rockchip.echo.smartecho.nlu;

import android.os.Bundle;

import java.io.Serializable;

public class NluResult implements Serializable {

    public final static String INTENT_DATA_TEXT = "text";
    public final static String INTENT_DATA_URL = "url";

    public enum NluIntent {
        SPEAK_TEXT,
        SPEAK_ASK_TEXT,
        PLAY_MUSIC,
    }

    protected NluIntent mIntent;
    protected Bundle mBundle;

    public void setIntent(NluIntent intent) {
        mIntent = intent;
    }

    public void setData(Bundle data) {
        mBundle = data;
    }

    public NluIntent getIntent() {
        return mIntent;
    }

    public Bundle getData() {
        return mBundle;
    }

}
