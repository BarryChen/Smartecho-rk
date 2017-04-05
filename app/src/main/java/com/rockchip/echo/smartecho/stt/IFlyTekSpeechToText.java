package com.rockchip.echo.smartecho.stt;


import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.rockchip.echo.smartecho.SmartEchoConfig;
import com.rockchip.echo.smartecho.audio.PcmRecorder;
import com.rockchip.echo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class IFlyTekSpeechToText extends SpeechToText {

    private Context mContext;

    private SpeechRecognizer mIat;

    private PcmRecorder mRecorder;

    private byte[] mBuffer;
    private int mBufLen;

    private PcmRecorder.PcmListener mPcmListener = new PcmRecorder.PcmListener() {

        @Override
        public void onPcmRate(long bytePerMs) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPcmData(byte[] data, int dataLen) {
//			write2File(data);
            if (mIsOnRecognition) {
                // 写入16K采样率音频，开始听写
                mIat.writeAudio(data, 0, dataLen);
                System.arraycopy(data, 0, mBuffer, mBufLen, dataLen);
                mBufLen += dataLen;
            }
        }
    };

    public IFlyTekSpeechToText(Context context) {
        super();
        mContext = context;
        init();
    }

    public IFlyTekSpeechToText(Context context, SpeechToTextListener listener) {
        super(listener);
        mContext = context;
        init();
    }

    public void init() {
        LogUtil.d("IFlyTekSpeechToText - init");
        mRecorder = new PcmRecorder();
        mRecorder.startRecording(mPcmListener);
        mBuffer = new byte[1024*1024];
        mIat = SpeechRecognizer.createRecognizer(mContext, null);
        setIatParam();
    }

    @Override
    public void start() {
        super.start();
        LogUtil.d("IFlyTekSpeechToText - startIat");
        // start listening user
        if(mIat != null && !mIat.isListening()) {
            mIat.startListening(mIatListener);
        }
        Arrays.fill(mBuffer, (byte) 0);
        mBufLen = 0;
    }

    @Override
    public void stop() {
        super.stop();
        LogUtil.d("IFlyTekSpeechToText - stopIat");
        if(mIat != null && mIat.isListening()) {
            mIat.stopListening();
        }
    }

    public byte[] getBuffer() {
        LogUtil.d("IFlyTekSpeechToText - getBuffer mBufLen: " + mBufLen);
        byte[] copy = new byte[mBufLen];
        System.arraycopy(mBuffer, 0, copy, 0, mBufLen);
        return copy;

    }

    // 听写监听器
    private RecognizerListener mIatListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int arg0, byte[] arg1) {
//            LogUtil.d("====== RecognizerListener - onVolumeChanged");
        }

        @Override
        public void onResult(RecognizerResult result, boolean isLast) {
            LogUtil.d("====== RecognizerListener - onResult");
            String res = processIatResult(result);
            stop();
            if (mSpeechToTextListener != null && res != null) {
                mSpeechToTextListener.onResult(res, isLast);
            }
        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            LogUtil.d("====== RecognizerListener - onEvent");
        }

        @Override
        public void onError(SpeechError arg0) {
            LogUtil.d("====== RecognizerListener - onError: " + arg0.getErrorCode() + " " + arg0.getErrorDescription());
            if (mSpeechToTextListener != null) {
                mSpeechToTextListener.onError(arg0.getErrorCode(), arg0.getErrorDescription());
            }
        }

        @Override
        public void onEndOfSpeech() {
            LogUtil.d("====== RecognizerListener - onEndOfSpeech");
        }

        @Override
        public void onBeginOfSpeech() {
            LogUtil.d("====== RecognizerListener - onBeginOfSpeech");
        }
    };

    private void setIatParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SmartEchoConfig.IatConfig.engineType);
        //
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, SmartEchoConfig.IatConfig.audioSource);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, SmartEchoConfig.IatConfig.resultType);
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, SmartEchoConfig.IatConfig.language);
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, SmartEchoConfig.IatConfig.accent);
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, SmartEchoConfig.IatConfig.vadBos);
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, SmartEchoConfig.IatConfig.vadEos);
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, SmartEchoConfig.IatConfig.asrPtt);
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, SmartEchoConfig.IatConfig.audioFormat);

        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, SmartEchoConfig.IatConfig.asrAudioPath);
        mIat.setParameter(SpeechConstant.NOTIFY_RECORD_DATA, SmartEchoConfig.IatConfig.notifyRecordData);

        mIat.setParameter("domain", SmartEchoConfig.IatConfig.domain);
    }

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private String processIatResult(RecognizerResult results) {
        LogUtil.d("======== RecognizerResult: " + results.getResultString());
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        String resultStr = resultBuffer.toString();

        LogUtil.d("======== result: " + resultStr);

        if(" ".equals(resultStr) || "。".equals(resultStr)) {
            LogUtil.d("====== skip useless result: " + resultStr);
            return null;
        }

        return text;
    }
}
