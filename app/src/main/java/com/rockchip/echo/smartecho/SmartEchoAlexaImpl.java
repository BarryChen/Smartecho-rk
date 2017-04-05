package com.rockchip.echo.smartecho;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.view.KeyEvent;

import com.rockchip.echo.smartecho.audio.PcmRecorder;
import com.rockchip.echo.smartecho.stt.IFlyTekSpeechToText;
import com.rockchip.echo.smartecho.stt.SpeechToText;
import com.rockchip.echo.smartecho.tts.TextToSpeech;
import com.rockchip.echo.util.LogUtil;
import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.audioplayer.AlexaAudioPlayer;
import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.callbacks.AuthorizationCallback;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayContentItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayRemoteItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.*;
import com.willblaschko.android.alexa.interfaces.speaker.AvsAdjustVolumeItem;
import com.willblaschko.android.alexa.interfaces.speaker.AvsSetMuteItem;
import com.willblaschko.android.alexa.interfaces.speaker.AvsSetVolumeItem;
import com.willblaschko.android.alexa.interfaces.speechrecognizer.AvsExpectSpeechItem;
import com.willblaschko.android.alexa.interfaces.speechsynthesizer.AvsSpeakItem;
import com.willblaschko.android.alexa.requestbody.DataRequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okio.BufferedSink;

/**
 * SmartEcho Implement Use Amazon Alexa
 */

class SmartEchoAlexaImpl extends SmartEcho {

    private final static String PRODUCT_ID = "SmartEcho_Alexa";

    private Context mContext;
    private boolean mHasLogin = false;
    private boolean mIsOnListener = false;

    SmartEchoAlexaImpl(Context context) {
        mContext = context;
        initAlexaAndroid();
    }

    @Override
    public void start() {
        LogUtil.d("SmartEchoAlexaImpl - start");
        checkLogin();
//        startWaitWakeup();
        initTts();
        initIat();
    }

    @Override
    public void stop() {
        LogUtil.d("SmartEchoAlexaImpl - stop");
        stopListening();
    }

    @Override
    public void onWakeUp(int angle, int chanel) {
        LogUtil.d("SmartEchoAlexaImpl - onWakeUp");
        stopAvsPlaying();
        if (mHasLogin) {
            startTtsAndIat(getEchoText());
        }
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

    private void checkLogin() {
        LogUtil.d("SmartEchoAlexaImpl - checkLogin");
        //Run an async check on whether we're logged in or not
        alexaManager.checkLoggedIn(mCheckLoginAsyncCallback);
    }

    private AsyncCallback<Boolean, Throwable> mCheckLoginAsyncCallback = new AsyncCallback<Boolean, Throwable>() {
        @Override
        public void start() {
            LogUtil.d("SmartEchoAlexaImpl - mCheckLoginAsyncCallback - start");
        }

        @Override
        public void success(Boolean result) {
            LogUtil.d("SmartEchoAlexaImpl - mCheckLoginAsyncCallback - success - result:" + result);
            if(result) {
                mHasLogin = true;
            } else {
                alexaManager.logIn(mAuthorizationCallback);
            }
        }

        @Override
        public void failure(Throwable error) {
            LogUtil.d("SmartEchoAlexaImpl - mCheckLoginAsyncCallback - failure");
            error.printStackTrace();
        }

        @Override
        public void complete() {
            LogUtil.d("SmartEchoAlexaImpl - mCheckLoginAsyncCallback - failure");
        }
    };

    private AuthorizationCallback mAuthorizationCallback = new AuthorizationCallback() {
        @Override
        public void onCancel() {
            LogUtil.d("SmartEchoAlexaImpl - mAuthorizationCallback - failure");
        }

        @Override
        public void onSuccess() {
            LogUtil.d("SmartEchoAlexaImpl - mAuthorizationCallback - failure");
            mHasLogin = true;
        }

        @Override
        public void onError(Exception error) {
            LogUtil.d("SmartEchoAlexaImpl - mAuthorizationCallback - failure");
            error.printStackTrace();
        }
    };


    private AlexaManager alexaManager;
    private AlexaAudioPlayer audioPlayer;
    private List<AvsItem> avsQueue = new ArrayList<>();

    private void initAlexaAndroid(){
        LogUtil.d("SmartEchoAlexaImpl - initAlexaAndroid");
        //get our AlexaManager instance for convenience
        alexaManager = AlexaManager.getInstance(mContext, PRODUCT_ID);

        //instantiate our audio player
        audioPlayer = AlexaAudioPlayer.getInstance(mContext);

        //Callback to be able to remove the current item and check queue once we've finished playing an item
        audioPlayer.addCallback(alexaAudioPlayerCallback);
    }

    //Our callback that deals with removing played items in our media player and then checking to see if more items exist
    private AlexaAudioPlayer.Callback alexaAudioPlayerCallback = new AlexaAudioPlayer.Callback() {
        @Override
        public void playerPrepared(AvsItem pendingItem) {
            LogUtil.d("SmartEchoAlexaImpl - playerPrepared");
        }

        @Override
        public void playerProgress(AvsItem currentItem, long offsetInMilliseconds, float percent) {
//            LogUtil.d("SmartEchoAlexaImpl - playerProgress");
        }

        @Override
        public void itemComplete(AvsItem completedItem) {
            LogUtil.d("SmartEchoAlexaImpl - itemComplete");
            avsQueue.remove(completedItem);
            checkQueue();
        }

        @Override
        public boolean playerError(AvsItem item, int what, int extra) {
            LogUtil.d("SmartEchoAlexaImpl - playerError");
            return false;
        }

        @Override
        public void dataError(AvsItem item, Exception e) {
            LogUtil.d("SmartEchoAlexaImpl - dataError");
        }

    };

    //async callback for commands sent to Alexa Voice
    private AsyncCallback<AvsResponse, Exception> requestCallback = new AsyncCallback<AvsResponse, Exception>() {
        @Override
        public void start() {
            LogUtil.d("SmartEchoAlexaImpl - requestCallback - start");
            //your on start code
        }

        @Override
        public void success(AvsResponse result) {
            LogUtil.d("SmartEchoAlexaImpl - requestCallback - success");
            stopListening();
            handleResponse(result);
        }

        @Override
        public void failure(Exception error) {
            LogUtil.d("SmartEchoAlexaImpl - requestCallback - failure");
            LogUtil.d(error.toString());
            //your on error code
            stopListening();
        }

        @Override
        public void complete() {
            LogUtil.d("SmartEchoAlexaImpl - requestCallback - complete");
            //your on complete code
            stopListening();
        }
    };

    /**
     * Handle the response sent back from Alexa's parsing of the Intent, these can be any of the AvsItem types (play, speak, stop, clear, listen)
     * @param response a List<AvsItem> returned from the mAlexaManager.sendTextRequest() call in sendVoiceToAlexa()
     */
    private void handleResponse(AvsResponse response){
        LogUtil.d("SmartEchoAlexaImpl - handleResponse");
        if(response != null){
            //if we have a clear queue item in the list, we need to clear the current queue before proceeding
            //iterate backwards to avoid changing our array positions and getting all the nasty errors that come
            //from doing that
            for(int i = response.size() - 1; i >= 0; i--){
                if(response.get(i) instanceof AvsReplaceAllItem || response.get(i) instanceof AvsReplaceEnqueuedItem){
                    //clear our queue
                    avsQueue.clear();
                    //remove item
                    response.remove(i);
                }
            }
            avsQueue.addAll(response);
        }
        checkQueue();
    }


    /**
     * Check our current queue of items, and if we have more to parse (once we've reached a play or listen callback) then proceed to the
     * next item in our list.
     *
     * We're handling the AvsReplaceAllItem in handleResponse() because it needs to clear everything currently in the queue, before
     * the new items are added to the list, it should have no function here.
     */
    private void checkQueue() {
        LogUtil.d("SmartEchoAlexaImpl - checkQueue");
        //if we're out of things, hang up the phone and move on
        if (avsQueue.size() == 0) {
            return;
        }

        LogUtil.d("SmartEchoAlexaImpl - checkQueue - queue size: " + avsQueue.size());
        for (AvsItem item : avsQueue) {
            LogUtil.d("class: " + item.getClass().toString() + " token: " + item.getToken());
        }

        AvsItem current = avsQueue.get(0);

        if (current instanceof AvsPlayRemoteItem) {
            //play a URL
            if (audioPlayer.isPlaying()) {
                audioPlayer.stop();
                avsQueue.remove(current);
                checkQueue();
                return;
            }
            audioPlayer.playItem((AvsPlayRemoteItem) current);
        } else if (current instanceof AvsPlayContentItem) {
            //play a URL
            if (audioPlayer.isPlaying()) {
                audioPlayer.stop();
                avsQueue.remove(current);
                checkQueue();
                return;
            }
            audioPlayer.playItem((AvsPlayContentItem) current);
        } else if (current instanceof AvsSpeakItem) {
            //play a sound file
            if (audioPlayer.isPlaying()) {
                audioPlayer.stop();
                avsQueue.remove(current);
                checkQueue();
                return;
            }
            audioPlayer.playItem((AvsSpeakItem) current);
        } else if (current instanceof AvsStopItem) {
            //stop our play
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceAllItem) {
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceEnqueuedItem) {
            avsQueue.remove(current);
        } else if (current instanceof AvsExpectSpeechItem) {
            //listen for user input
            audioPlayer.stop();
            startListening();
        } else if (current instanceof AvsSetVolumeItem) {
            setVolume(((AvsSetVolumeItem) current).getVolume());
            avsQueue.remove(current);
        } else if(current instanceof AvsAdjustVolumeItem){
            adjustVolume(((AvsAdjustVolumeItem) current).getAdjustment());
            avsQueue.remove(current);
        } else if(current instanceof AvsSetMuteItem){
            setMute(((AvsSetMuteItem) current).isMute());
            avsQueue.remove(current);
        }else if(current instanceof AvsMediaPlayCommandItem){
            //fake a hardware "play" press
            sendMediaButton(mContext, KeyEvent.KEYCODE_MEDIA_PLAY);
        }else if(current instanceof AvsMediaPauseCommandItem){
            //fake a hardware "pause" press
            sendMediaButton(mContext, KeyEvent.KEYCODE_MEDIA_PAUSE);
        }else if(current instanceof AvsMediaNextCommandItem){
            //fake a hardware "next" press
            sendMediaButton(mContext, KeyEvent.KEYCODE_MEDIA_NEXT);
        }else if(current instanceof AvsMediaPreviousCommandItem){
            //fake a hardware "previous" press
            sendMediaButton(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        }
    }

    private void stopAvsPlaying() {
        if (audioPlayer.isPlaying()) {
            LogUtil.d("SmartEchoAlexaImpl - stopAvsPlaying()");
            audioPlayer.stop();
            if (avsQueue.size() > 0) {
                avsQueue.clear();
            }
        }
    }

    //adjust our device volume
    private void adjustVolume(long adjust){
        setVolume(adjust, true);
    }

    //set our device volume
    private void setVolume(long volume){
        setVolume(volume, false);
    }

    //set our device volume, handles both adjust and set volume to avoid repeating code
    private void setVolume(final long volume, final boolean adjust){
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        long vol= am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(adjust){
            vol += volume * max / 100;
        }else{
            vol = volume * max / 100;
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) vol, AudioManager.FLAG_VIBRATE);
        //confirm volume change
        alexaManager.sendVolumeChangedEvent(volume, vol == 0, requestCallback);
    }

    //set device to mute
    private void setMute(final boolean isMute){
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
        //confirm device mute
        alexaManager.sendMutedEvent(isMute, requestCallback);
    }

    private static void sendMediaButton(Context context, int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }

    private final static int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int AUDIO_RATE = 16000;
    private RawAudioRecorder recorder;

    private void startListening() {
        LogUtil.d("SmartEchoAlexaImpl - startListening");
        mIsOnListener = true;
        startIat();
//        stopWaitWakeup();
//        if(recorder == null){
//            recorder = new RawAudioRecorder(AUDIO_RATE);
//        }
//        recorder.start();
//        alexaManager.sendAudioRequest(requestBody, requestCallback);
    }

    private void stopListening() {
        LogUtil.d("SmartEchoAlexaImpl - stopListening");
//        mIsOnListener = false;
//        alexaManager.cancelAudioRequest();
        stopIat();
    }

    private void stopRecording() {
        LogUtil.d("SmartEchoAlexaImpl - stopRecording");
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        startWaitWakeup();
    }

    private PcmRecorder mPcmRecorder;
    private PcmRecorder.PcmListener mPcmListener = new PcmRecorder.PcmListener() {

        @Override
        public void onPcmRate(long bytePerMs) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPcmData(byte[] data, int dataLen) {
//			write2File(data);
        }
    };

    private void startWaitWakeup() {
        LogUtil.d("SmartEchoAlexaImpl - startWaitWakeup");
        if (mPcmRecorder == null) {
            mPcmRecorder = new PcmRecorder();
        }
        mPcmRecorder.startRecording(mPcmListener);
        LogUtil.d("SmartEchoAlexaImpl - startWaitWakeup - 1");
    }

    private void stopWaitWakeup() {
        LogUtil.d("SmartEchoAlexaImpl - stopWaitWakeup");
        if (mPcmRecorder != null) {
            mPcmRecorder.stopRecording();
        }
    }

    //our streaming data requestBody
    private DataRequestBody requestBody = new DataRequestBody() {
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo");
            //while our recorder is not null and it is still recording, keep writing to POST data
            while (recorder != null && !recorder.isPausing() && mIsOnListener) {
                LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo - keep");
                if(sink != null && recorder != null && mIsOnListener) {
                    LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo - 1");
                    byte[] buf = recorder.consumeRecording();
                    LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo - 2 - " + buf.length);
                    sink.write(buf);
                    LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo - 3");
                }
                //sleep and do it all over again
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - writeTo" +
                        " mIsOnListener：" + mIsOnListener);
            }
            LogUtil.d("SmartEchoAlexaImpl - DataRequestBody - stop");
            stopRecording();
        }
    };

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
                stopIat();
                if (isLast) {
//                alexaManager.sendTextRequest(res, requestCallback);
                    byte[] buf = ((IFlyTekSpeechToText) mStt).getBuffer();
                    alexaManager.sendAudioRequest(buf, requestCallback);
                }
            }

            @Override
            public void onError(int code, String errorDesc) {
                stopIat();
            }
        };

        mStt = SpeechToText.getInstance(mContext, listener);
    }

    public void startIat() {
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
     *                               Text To Speech
     * ==================================================================================
     */
    TextToSpeech mTts;
    private boolean mIsNeedStartIatAfterTts = false;

    private void initTts() {
        TextToSpeech.TextToSpeechListener listener = new TextToSpeech.TextToSpeechListener() {
            @Override
            public void onSpeakStart() {

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
            LogUtil.d("SmartEchoAlexaImpl - startTtsOutput - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = false;
        mTts.startTts(text);
    }

    public void startTtsAndIat(String text) {
        if (mTts == null) {
            LogUtil.d("SmartEchoAlexaImpl - startTtsOutput - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = true;
        mTts.startTts(text);
    }

    public void stopTts() {
        if (mTts == null) {
            LogUtil.d("SmartEchoAlexaImpl - stopTts - mTts=null");
            return;
        }
        mIsNeedStartIatAfterTts = false;
        mTts.stopTts();
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
        LogUtil.d("SmartEchoAlexaImpl - showLedOnListener: " + isShow);
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
        LogUtil.d("SmartEchoAlexaImpl - showCompalLedOnListener: " + isShow);
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

}
