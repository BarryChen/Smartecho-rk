package com.rockchip.echo.smartecho.wakeup;


public class CaeWakeUp {

    private CaeWakeUpFileObserver mCaeWakeUpFileObserver;

    public CaeWakeUp(CaeWakeupListener listener) {
        mCaeWakeUpFileObserver = new CaeWakeUpFileObserver(listener);
    }

    public void start() {
        mCaeWakeUpFileObserver.startWatching();
    }

    public void stop() {
        mCaeWakeUpFileObserver.stopWatching();
    }

}
