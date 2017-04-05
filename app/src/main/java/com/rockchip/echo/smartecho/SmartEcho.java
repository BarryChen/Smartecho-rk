package com.rockchip.echo.smartecho;

import android.content.Context;

import com.rockchip.echo.smartecho.nlu.aiapi.Config;
import com.rockchip.echo.smartecho.wakeup.CaeWakeupListener;

/**
 * SmartEcho
 */

public abstract class SmartEcho implements CaeWakeupListener {

    public abstract void start();
    public abstract void stop();
    public void echoOnStart() {}
    public void echoOnWake() {}

    public static SmartEcho getInstance(Context context) {
        if (SmartEchoConfig.echoimpl.equals("alexa")) {
            return new SmartEchoAlexaImpl(context);
        } else {
            return new SmartEchoComImpl(context);
        }
    }
}
