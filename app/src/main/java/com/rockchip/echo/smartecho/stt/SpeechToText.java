package com.rockchip.echo.smartecho.stt;

import android.content.Context;


public class SpeechToText {

    protected SpeechToTextListener mSpeechToTextListener;
    protected boolean mIsOnRecognition;

    public static SpeechToText getInstance(Context context, SpeechToTextListener listener) {
        return new IFlyTekSpeechToText(context, listener);
    }


    public SpeechToText(SpeechToTextListener listener) {
        mSpeechToTextListener = listener;
    }

    public SpeechToText() {
    }

    public void start() {
        mIsOnRecognition = true;
        if (mSpeechToTextListener != null) {
            mSpeechToTextListener.onStart();
        }
    }

    public void stop() {
        mIsOnRecognition = false;
        if (mSpeechToTextListener != null) {
            mSpeechToTextListener.onStop();
        }
    }

    public interface SpeechToTextListener {
        void onStart();
        void onStop();
        void onResult(String res, boolean isLast);
        void onError(int code, String errorDesc);
    }
}
