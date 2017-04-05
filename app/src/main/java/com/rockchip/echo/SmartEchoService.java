package com.rockchip.echo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import com.rockchip.echo.smartecho.wakeup.CaeWakeUp;
import com.rockchip.echo.smartecho.SmartEcho;
import com.rockchip.echo.util.LogUtil;


public class SmartEchoService extends Service {

    public static final String SMART_ECHO_ACTION_START = "com.rockchip.echoOnWakeUp.ACTION.START";
    public static final String SMART_ECHO_ACTION_WAKEUP = "com.rockchip.echoOnWakeUp.ACTION.CAE.WAKEUP";

    private SmartEcho mSmartEcho;
    private CaeWakeUp mCaeWakeUp;

    public SmartEchoService() {
        LogUtil.d("SmartEchoService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        LogUtil.d("SmartEchoService - onCreate");
        super.onCreate();
        acquireWakeLock();
        mSmartEcho = SmartEcho.getInstance(getApplicationContext());
        mSmartEcho.start();
        mCaeWakeUp = new CaeWakeUp(mSmartEcho);
        mCaeWakeUp.start();
    }

    @Override
    public void onDestroy() {
        LogUtil.d("SmartEchoService - onDestroy");
        super.onDestroy();
        if (mCaeWakeUp != null) {
            mCaeWakeUp.stop();
        }
        mSmartEcho.stop();
        releaseWakeLock();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            LogUtil.d("SmartEchoService - onStartCommand - " + action);
            if (SMART_ECHO_ACTION_START.equals(action)) {
                mSmartEcho.echoOnStart();
            } else if(SMART_ECHO_ACTION_WAKEUP.equals(action)) {
                // wake manual
                mSmartEcho.onWakeUp(0, 0);
            }
        }
        return START_STICKY;
    }

    private PowerManager.WakeLock mWakeLock;

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "EchoLock");
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
}
