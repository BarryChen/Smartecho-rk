package com.rockchip.echo.smartecho.tts;


import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.rockchip.echo.R;
import com.rockchip.echo.smartecho.SmartEchoConfig;
import com.rockchip.echo.util.LogUtil;

public class IFlyTekTextToSpeech extends TextToSpeech {

    private Context mContext;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    public IFlyTekTextToSpeech(Context context) {
        mContext = context;
        init();
    }

    public IFlyTekTextToSpeech(Context context, TextToSpeechListener ttsListener) {
        super(ttsListener);
        mContext = context;
        init();
    }

    public void init() {
        LogUtil.d("IFlyTekTextToSpeech - init");
        mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
    }

    @Override
    public int startTts(String text) {
        if (mTts == null) {
            LogUtil.d("IFlyTekTextToSpeech - startTts - error mTts=null");
            return -1;
        }
        super.startTts(text);
        LogUtil.d("IFlyTekTextToSpeech - startTts - text: " + text);
        // 设置参数
        setTtsParam();
        int code = mTts.startSpeaking(text, mSynthesizerListener);
        if (code != ErrorCode.SUCCESS) {
            LogUtil.d("IFlyTekTextToSpeech - startTtsAndIat - tts error: " + code);
        }
        return code;
    }

    @Override
    public int stopTts() {
        if (mTts == null) {
            LogUtil.d("IFlyTekTextToSpeech - stopTts - error mTts=null");
            return -1;
        }
        super.stopTts();
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
        return 0;
    }

    @Override
    public boolean isSpeaking() {
        if (mTts != null) {
            return mTts.isSpeaking();
        } else {
            return false;
        }
    }

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                LogUtil.d("IFlyTekTextToSpeech - InitListener - tts init error code=" + code);
            } else {
                LogUtil.d("IIFlyTekTextToSpeech - InitListener init success code=" + code);
            }
        }
    };

    private SynthesizerListener mSynthesizerListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            LogUtil.d("IFlyTekTextToSpeech - SynthesizerListener - onSpeakBegin");
            if (mTextToSpeechListener != null) {
                mTextToSpeechListener.onSpeakStart();
            }
        }

        @Override
        public void onSpeakPaused() {
            LogUtil.d("IFlyTekTextToSpeech - SynthesizerListener - onSpeakPaused");
        }

        @Override
        public void onSpeakResumed() {
            LogUtil.d("IFlyTekTextToSpeech - SynthesizerListener - onSpeakResumed");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            LogUtil.d(String.format(mContext.getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
//            LogUtil.d(String.format(mContext.getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                LogUtil.d("IFlyTekTextToSpeech - SynthesizerListener - onCompleted");
            } else {
                LogUtil.d(error.getPlainDescription(true));
            }
            if (mTextToSpeechListener != null) {
                mTextToSpeechListener.onSpeakComplete();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void setTtsParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SmartEchoConfig.TtsConfig.engineType);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, SmartEchoConfig.TtsConfig.voiceName);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, SmartEchoConfig.TtsConfig.speed);
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, SmartEchoConfig.TtsConfig.pitch);
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, SmartEchoConfig.TtsConfig.volume);

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, SmartEchoConfig.TtsConfig.streamType);
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, SmartEchoConfig.TtsConfig.keyRequestFocus);

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, SmartEchoConfig.TtsConfig.audioFormat);
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, SmartEchoConfig.TtsConfig.ttsAudioPath);
    }
}
