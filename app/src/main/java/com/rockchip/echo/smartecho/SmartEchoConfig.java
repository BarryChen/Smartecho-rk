package com.rockchip.echo.smartecho;

import android.os.Environment;

import com.rockchip.echo.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


public class SmartEchoConfig {

    private static final String CONFIG_FILE_PATH1 = "/system/etc/smartecho.properties";
    private static final String CONFIG_FILE_PATH2 = "/sdcard/smartecho.properties";

    public static class IatConfig {

        public static String engineType = "cloud";
        public static String audioSource = "-1";
        public static String resultType = "json";
        public static String language = "zh_cn";
        public static String accent = "mandarin";
        public static String vadBos = "5000";
        public static String vadEos = "1500";
        public static String asrPtt = "0";
        public static String audioFormat = "wav";
        public static String asrAudioPath = Environment.getExternalStorageDirectory()+"/msc/iat.wav";
        public static String notifyRecordData = "0";
        public static String domain = "fariat";
    }

    public static class TtsConfig {

        public static String engineType = "cloud";
        public static String voiceName = "vinn";
        public static String speed = "50";
        public static String pitch = "50";
        public static String volume = "100";
        public static String streamType = "3";
        public static String keyRequestFocus = "true";
        public static String audioFormat = "wav";
        public static String ttsAudioPath = Environment.getExternalStorageDirectory()+"/msc/tts.wav";
    }

    public static String appId = "580adc58";

    public static String startEchoText = "我来了";

    public static String wakeupEchoText = "在呢|我在|在";

    public static String echoimpl = "common";
    public static String nluimpl = "leung";

//    private static String[] startEchoTextArray;
    private static String[] wakeupEchoTextArray;

    public static String[] getWakeupEchoTextArray() {
        if (wakeupEchoTextArray == null) {
            wakeupEchoTextArray = wakeupEchoText.split("\\|");
        }
        return wakeupEchoTextArray;
    }

//    public static String[] getStartEchoTextArray() {
//        if (startEchoTextArray == null) {
//            startEchoTextArray = startEchoText.split("|");
//        }
//        return startEchoTextArray;
//    }

    public static boolean loadConfig() {
        String useConfigFilePath;
        if (isFileExist(CONFIG_FILE_PATH1)) {
            useConfigFilePath = CONFIG_FILE_PATH1;
        } else if(isFileExist(CONFIG_FILE_PATH2)) {
            useConfigFilePath = CONFIG_FILE_PATH2;
        } else {
            return false;
        }
        LogUtil.d("SmartEchoConfig - loadConfig");
        Properties props = new Properties();
        try {
            InputStream in = new FileInputStream(useConfigFilePath);
            props.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        LogUtil.d("SmartEchoConfig - loadConfig from: " + useConfigFilePath);
        loadProps(props);
        LogUtil.d(changeIsoToUtf8(props.toString()));
        return true;
    }

    private static void loadProps(Properties props) {
        LogUtil.d("SmartEchoConfig - loadProps");
        IatConfig.engineType = props.getProperty(PROP_KEY_IAT_ENGINE_TYPE, IatConfig.engineType);
        IatConfig.audioSource = props.getProperty(PROP_KEY_IAT_AUDIO_SOURCE, IatConfig.audioSource);
        IatConfig.resultType = props.getProperty(PROP_KEY_IAT_RESULT_TYPE, IatConfig.resultType);
        IatConfig.language = props.getProperty(PROP_KEY_IAT_LANGUAGE, IatConfig.language);
        IatConfig.accent = props.getProperty(PROP_KEY_IAT_ACCENT, IatConfig.accent);
        IatConfig.vadBos = props.getProperty(PROP_KEY_IAT_VAD_BOS, IatConfig.vadBos);
        IatConfig.vadEos = props.getProperty(PROP_KEY_IAT_VAD_EOS, IatConfig.vadEos);
        IatConfig.asrPtt = props.getProperty(PROP_KEY_IAT_ASR_PTT, IatConfig.asrPtt);
        IatConfig.audioFormat = props.getProperty(PROP_KEY_IAT_AUDIO_FORMAT, IatConfig.audioFormat);
        IatConfig.asrAudioPath = props.getProperty(PROP_KEY_IAT_ASR_AUDIO_PATH, IatConfig.asrAudioPath);
        IatConfig.notifyRecordData = props.getProperty(PROP_KEY_IAT_NOTIFY_RECORD_DATA, IatConfig.notifyRecordData);
        IatConfig.domain = props.getProperty(PROP_KEY_IAT_DOMAIN, IatConfig.domain);

        TtsConfig.engineType = props.getProperty(PROP_KEY_TTS_ENGINE_TYPE, TtsConfig.engineType);
        TtsConfig.voiceName = props.getProperty(PROP_KEY_TTS_VOICE_NAME, TtsConfig.voiceName);
        TtsConfig.speed = props.getProperty(PROP_KEY_TTS_SPEED, TtsConfig.speed);
        TtsConfig.pitch = props.getProperty(PROP_KEY_TTS_PITCH, TtsConfig.pitch);
        TtsConfig.volume = props.getProperty(PROP_KEY_TTS_VOLUME, TtsConfig.volume);
        TtsConfig.streamType = props.getProperty(PROP_KEY_TTS_STREAM_TYPE, TtsConfig.streamType);
        TtsConfig.keyRequestFocus = props.getProperty(PROP_KEY_TTS_KEY_REQUEST_FOCUS, TtsConfig.keyRequestFocus);
        TtsConfig.audioFormat = props.getProperty(PROP_KEY_TTS_AUDIO_FORMAT, TtsConfig.audioFormat);
        TtsConfig.ttsAudioPath = props.getProperty(PROP_KEY_TTS_AUDIO_PATH, TtsConfig.ttsAudioPath);

        String startEchoTextStr = changeIsoToUtf8(props.getProperty(PROP_KEY_START_ECHO_TEXT));
        if (startEchoTextStr != null) {
            startEchoText = startEchoTextStr;
        }
        String wakeupEchoTextStr = changeIsoToUtf8(props.getProperty(PROP_KEY_WAKEUP_ECHO_TEXT, wakeupEchoText));
        if (wakeupEchoTextStr != null) {
            wakeupEchoText = wakeupEchoTextStr;
        }

        appId = props.getProperty(PROP_KEY_APPID, appId);
        echoimpl = props.getProperty(PROP_KEY_ECHO_IMPL, echoimpl);
        nluimpl = props.getProperty(PROP_KEY_NLU_IMPL, nluimpl);
    }

    private static String changeIsoToUtf8(String s) {
        String retStr = null;
        if (s == null) {
            return null;
        }
        try {
            retStr = new String(s.getBytes("iso-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retStr;
    }

    public static boolean isFileExist(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public final static String PROP_KEY_IAT_ENGINE_TYPE = "IAT_ENGINE_TYPE";
    public final static String PROP_KEY_IAT_AUDIO_SOURCE = "IAT_AUDIO_SOURCE";
    public final static String PROP_KEY_IAT_RESULT_TYPE = "IAT_RESULT_TYPE";
    public final static String PROP_KEY_IAT_LANGUAGE = "IAT_LANGUAGE";
    public final static String PROP_KEY_IAT_ACCENT = "IAT_ACCENT";
    public final static String PROP_KEY_IAT_VAD_BOS = "IAT_VAD_BOS";
    public final static String PROP_KEY_IAT_VAD_EOS = "IAT_VAD_EOS";
    public final static String PROP_KEY_IAT_ASR_PTT = "IAT_ASR_PTT";
    public final static String PROP_KEY_IAT_AUDIO_FORMAT = "IAT_AUDIO_FORMAT";
    public final static String PROP_KEY_IAT_ASR_AUDIO_PATH = "IAT_ASR_AUDIO_PATH";
    public final static String PROP_KEY_IAT_NOTIFY_RECORD_DATA = "IAT_NOTIFY_RECORD_DATA";
    public final static String PROP_KEY_IAT_DOMAIN = "IAT_DOMAIN";

    public final static String PROP_KEY_TTS_ENGINE_TYPE = "TTS_ENGINE_TYPE";
    public final static String PROP_KEY_TTS_VOICE_NAME = "TTS_VOICE_NAME";
    public final static String PROP_KEY_TTS_SPEED = "TTS_SPEED";
    public final static String PROP_KEY_TTS_PITCH = "TTS_PITCH";
    public final static String PROP_KEY_TTS_VOLUME = "TTS_VOLUME";
    public final static String PROP_KEY_TTS_STREAM_TYPE = "TTS_STREAM_TYPE";
    public final static String PROP_KEY_TTS_KEY_REQUEST_FOCUS = "TTS_KEY_REQUEST_FOCUS";
    public final static String PROP_KEY_TTS_AUDIO_FORMAT = "TTS_AUDIO_FORMAT";
    public final static String PROP_KEY_TTS_AUDIO_PATH = "TTS_AUDIO_PATH";

    public final static String PROP_KEY_START_ECHO_TEXT = "START_ECHO_TEXT";
    public final static String PROP_KEY_WAKEUP_ECHO_TEXT = "WAKEUP_ECHO_TEXT";
    public final static String PROP_KEY_APPID = "APPID";
    public final static String PROP_KEY_ECHO_IMPL = "ECHO_IMPL";
    public final static String PROP_KEY_NLU_IMPL = "NLU_IMPL";
}
