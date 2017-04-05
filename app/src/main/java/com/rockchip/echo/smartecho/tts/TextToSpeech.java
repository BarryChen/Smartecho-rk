package com.rockchip.echo.smartecho.tts;


import android.content.Context;

public class TextToSpeech {

    protected TextToSpeechListener mTextToSpeechListener;

    public TextToSpeech() {

    }

    public TextToSpeech(TextToSpeechListener ttsListener) {
        mTextToSpeechListener = ttsListener;
    }

    public int startTts(String text) {
        return 0;
    }

    public int stopTts() {
        return 0;
    }

    public static TextToSpeech getInstance(Context context, TextToSpeechListener listener) {
        return new IFlyTekTextToSpeech(context, listener);
    }

    public boolean isSpeaking() {
        return false;
    }

    public void setListener(TextToSpeechListener ttsListener) {
        mTextToSpeechListener = ttsListener;
    }

    public interface TextToSpeechListener {
        void onSpeakStart();
        void onSpeakComplete();
    }
}
