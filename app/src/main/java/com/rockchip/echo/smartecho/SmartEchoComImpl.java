package com.rockchip.echo.smartecho;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.iflytek.cloud.Setting;
import com.rockchip.echo.smartecho.actions.Action;
import com.rockchip.echo.smartecho.actions.MusicPlayAction;
import com.rockchip.echo.smartecho.nlu.Nlu;
import com.rockchip.echo.smartecho.nlu.NluError;
import com.rockchip.echo.smartecho.nlu.NluListener;
import com.rockchip.echo.smartecho.nlu.NluResult;
import com.rockchip.echo.smartecho.stt.SpeechToText;
import com.rockchip.echo.smartecho.tts.TextToSpeech;
import com.rockchip.echo.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * SmartEcho Common Implement
 */

public class SmartEchoComImpl extends SmartEcho {

    private Context mContext;

    private boolean mStartRecognize = false;
    private boolean mIsOnTts = false;
    private boolean mIsNeedStartIatAfterTts = false;

    SmartEchoComImpl(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        LogUtil.d("SmartEchoComImpl - init");
        SmartEchoConfig.loadConfig();
        initTts();
        initIat();
        initNlu();
        Setting.setLogLevel(Setting.LOG_LEVEL.low);
    }

    @Override
    public void start() {
        LogUtil.d("SmartEchoComImpl - start");
    }

    @Override
    public void stop() {
        LogUtil.d("SmartEchoComImpl - stop");
        stopIat();
    }

    @Override
    public void onWakeUp(int angle, int chanel) {
        LogUtil.d("SmartEchoComImpl - onWakeUp");
        startTtsAndIat(getEchoText());
        stopNluProcess();
    }

    @Override
    public void echoOnStart() {
        startTtsOutput(SmartEchoConfig.startEchoText);
    }

    private int mEchoIndex = 0;
    private String getEchoText() {
        mEchoIndex++;
        String[] wakeupEchoTextArray = SmartEchoConfig.getWakeupEchoTextArray();
        if(mEchoIndex >= wakeupEchoTextArray.length) {
            mEchoIndex = 0;
        }
        return wakeupEchoTextArray[mEchoIndex];
    }

    /**
     * ==================================================================================
     *                               Text To Speech
     * ==================================================================================
     */
    TextToSpeech mTts;

    private void initTts() {
        TextToSpeech.TextToSpeechListener listener = new TextToSpeech.TextToSpeechListener() {
            @Override
            public void onSpeakStart() {
                mStartRecognize = false;
            }

            @Override
            public void onSpeakComplete() {
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        if (mIsNeedStartIatAfterTts) {
                            LogUtil.d("tts - onCompleted - need start iat after tts");
                            mIsNeedStartIatAfterTts = false;
                            startIat();
                        }
                    }
                }, 200);
            }
        };
        mTts = TextToSpeech.getInstance(mContext, listener);
    }

    public void startTtsOutput(String text) {
        if (mTts == null) {
            LogUtil.d("SmartEchoComImpl - startTtsOutput - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = false;
        mTts.startTts(text);
    }

    public void startTtsAndIat(String text) {
        if (mTts == null) {
            LogUtil.d("SmartEchoComImpl - startTtsOutput - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = true;
        mTts.startTts(text);
    }

    public void stopTts() {
        if (mTts == null) {
            LogUtil.d("SmartEchoComImpl - stopTts - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = false;
        mTts.stopTts();
    }

    /**
     * ==================================================================================
     *                               Speech To Text
     * ==================================================================================
     */
    private SpeechToText mStt;

    private void initIat() {
        SpeechToText.SpeechToTextListener listener = new SpeechToText.SpeechToTextListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onResult(String res, boolean isLast) {
                startNluProcess(res);
                stopIat();
            }

            @Override
            public void onError(int code, String errorDesc) {
                stopIat();
            }
        };

        mStt = SpeechToText.getInstance(mContext, listener);
    }

    public void startIat() {
        if (mTts != null) {
            mTts.stopTts();
        }
        if (mStt != null) {
            mStt.start();
        }
        showLedOnListener(true);
    }

    public void stopIat() {
        if (mStt != null) {
            mStt.stop();
        }
        showLedOnListener(false);
    }


    /**
     * ==================================================================================
     *                               Nature Language Understanding
     * ==================================================================================
     */

    private Nlu mNlu;

    private NluListener mNluListener = new NluListener() {
        @Override
        public void onResult(NluResult result) {
            LogUtil.d("NluListener - onResult");
            switch (result.getIntent()) {
                case SPEAK_TEXT: {
                    Bundle data = result.getData();
                    String text = data.getString(NluResult.INTENT_DATA_TEXT);
                    startTtsOutput(text);
                }
                break;
                case SPEAK_ASK_TEXT: {
                    Bundle data = result.getData();
                    String text = data.getString(NluResult.INTENT_DATA_TEXT);
                    startTtsAndIat(text);
                }
                case PLAY_MUSIC: {
                    Bundle data = result.getData();
                    String url = data.getString(NluResult.INTENT_DATA_URL);
                    playMusic(url);
                }
                break;
            }
        }

        @Override
        public void onError(NluError error) {
            LogUtil.d("NluListener - onError");
        }
    };

    public void initNlu() {
        mNlu = Nlu.getInstance(mContext, mNluListener);
    }

    public void startNluProcess(String text) {
        // check text
        if(text == null || text.isEmpty() || text.equals("。") || text.equals(" ") || text.equals("？")) {
            return;
        }
        mNlu.startProcess(text);
    }

    public void stopNluProcess() {
        stopMusic();
    }

    /**
     * ==================================================================================
     *                               control led
     * ==================================================================================
     */
    private Timer mLedTimer;
    private boolean isShowLedGreen = true;
    private TimerTask mLedTimerTask;

    public void showLedOnListener(boolean isShow) {
        LogUtil.d("SmartEchoComImpl - showCompalLedOnListener: " + isShow);
        if (isShow) {
            if (mLedTimer == null ) {
                mLedTimer = new Timer();
                mLedTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isShowLedGreen) {
                            LedController.setGreenLedState(1);
                        } else {
                            LedController.setGreenLedState(0);
                        }
                        isShowLedGreen = !isShowLedGreen;
                    }
                };
                mLedTimer.schedule(mLedTimerTask, 500, 1000);
            }
        } else {
            if (mLedTimerTask != null) {
                mLedTimerTask.cancel();
                mLedTimerTask = null;
            }
            if (mLedTimer != null) {
                mLedTimer.cancel();
                mLedTimer = null;
            }
            LedController.setGreenLedState(1);
        }
        showCompalLedOnListener(isShow);
    }

    private Timer mCompalLedTimer;
    private boolean isShowLedGroupA = true;
    private TimerTask mCompalLedTimerTask;

    public void showCompalLedOnListener(boolean isShow) {
        LogUtil.d("SmartEchoComImpl - showCompalLedOnListener: " + isShow);
        if (isShow) {
            if (mCompalLedTimer == null ) {
                mCompalLedTimer = new Timer();
                mCompalLedTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        CompalLedController.controlLedOnListerner(isShowLedGroupA);
                        isShowLedGroupA = !isShowLedGroupA;
                    }
                };
                mCompalLedTimer.schedule(mCompalLedTimerTask, 500, 1000);
            }
        } else {
            if (mCompalLedTimerTask != null) {
                mCompalLedTimerTask.cancel();
                mCompalLedTimerTask = null;
            }
            if (mCompalLedTimer != null) {
                mCompalLedTimer.cancel();
                mCompalLedTimer = null;
            }
            CompalLedController.setAllLedOff();
        }
    }

    /**
     * ==================================================================================
     *                               play music
     * ==================================================================================
     */

    private Action mMusicPlayAction;

    private void playMusic(String url) {
        LogUtil.d("SmartEchoComImpl - playMusic - url: " + url);
        if (mMusicPlayAction == null) {
            mMusicPlayAction = new MusicPlayAction();
        }
        Bundle data = new Bundle();
        data.putString(MusicPlayAction.DATA_URL, url);
        mMusicPlayAction.prepare(data);
        mMusicPlayAction.start();
    }

    private void stopMusic() {
        LogUtil.d("SmartEchoComImpl - stopMusic");
        if (mMusicPlayAction == null) {
            return;
        }
        mMusicPlayAction.stop();
    }
}
