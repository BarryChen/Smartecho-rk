package com.rockchip.echo.smartecho.nlu.iflytek;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.rockchip.echo.smartecho.nlu.Nlu;
import com.rockchip.echo.smartecho.nlu.NluListener;
import com.rockchip.echo.smartecho.nlu.NluResult;
import com.rockchip.echo.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IflytekNlu extends Nlu {

    // 语义理解对象（文本到语义）
    private TextUnderstander mTextUnderstander;

    public IflytekNlu(Context context) {
        super(context);
        initTextUnderstand();
    }

    public IflytekNlu(Context context, NluListener listener) {
        super(context, listener);
        initTextUnderstand();
    }

    @Override
    public void startProcess(String text) {
        int ret = mTextUnderstander.understandText(text, mTextUnderstanderListener);
        if(ret != 0) {
            LogUtil.d("text understand error: " + ret);
        }
    }

    /**
     * 初始化监听器（文本到语义）。
     */
    private InitListener mTextUdrInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            LogUtil.d("textUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                LogUtil.d("====== mTextUdrInitListener - onInit - init error, code: " + code);
            }
        }
    };

    TextUnderstand textUnderstand;

    private TextUnderstanderListener mTextUnderstanderListener = new TextUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            LogUtil.d("========= TextUnderstanderListener - onResult =========");
            if (null != result) {
                // 显示
                String understandText = result.getResultString();
                if (!TextUtils.isEmpty(understandText)) {
//                    String showtext = mResultEdit.getText().toString();
//                    showtext += "\r\n";
//                    showtext += UnderstandText;
//                    mResultEdit.setText(showtext);
                    LogUtil.d(understandText);
                    parserResult(understandText);
                }
            } else {
                LogUtil.d("understander result : null");
            }
            LogUtil.d("========================================================");
        }

        @Override
        public void onError(SpeechError error) {
            // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
            LogUtil.d("TextUnderstanderListener - onError Code："    + error.getErrorCode());
        }
    };

    private void initTextUnderstand() {
        mTextUnderstander = TextUnderstander.createTextUnderstander(mContext, mTextUdrInitListener);
    }

    protected void parserResult(String understandText) {
        String ttsText = null;
        try {
            JSONObject textJsonObj = new JSONObject(understandText);
            int rc = textJsonObj.getInt("rc");
            if(rc == 0) {
                String service = textJsonObj.getString("service");
                String operation = textJsonObj.getString("operation");
                LogUtil.d("TextUnderstand - service: " + service + " operation: " + operation);
                if("QUERY".equals(operation) && "weather".equals(service)) {
//                    mTextUnderstandResult = new WeatherUnderstandResult();
                    parserWeather(understandText);
                } else if("ANSWER".equals(operation)) {
//                    mTextUnderstandResult = new AnswserUnderstandtResult();
                    parserAnswser(understandText);
                } else if("PLAY".equals(operation) && "music".equals(service)) {
//                    mTextUnderstandResult = new MusicUnderstandResult();
                    parserMusic(understandText);
                }
//                if (mTextUnderstandResult != null) {
//                    mTextUnderstandResult.rc = rc;
//                    mTextUnderstandResult.service = service;
//                    mTextUnderstandResult.operation = operation;
//                    ttsText = mTextUnderstandResult.parser(understandText);
//                    LogUtil.d("TextUnderstand - parser tts text: " + ttsText);
//                }
            } else if(rc == 4) {
                LogUtil.d("TextUnderstand - rc=4, can't understand text");
                String text = textJsonObj.getString("text");
                if(text.equals("。")) {
                    return;
                }
                ttsText = TextUnderstandResult.DEFAULT_TEXT_NO_FOUND_ANSWER;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (mTextUnderstandResult != null) {
//            mTextUnderstandResult.mTtsText = ttsText;
//        }
    }

    public static final String DEFAULT_TEXT_NO_FOUND_ANSWER = "没有为你查到";

    protected String parserWeather(String understandText) {
        JSONObject textobj = null;
        String ttsText = null;
        try {
            textobj = new JSONObject(understandText);
            if(textobj == null) {
                return null;
            }
            JSONObject data = textobj.getJSONObject("data");
            JSONArray result = data.getJSONArray("result");
            JSONObject todayResult = result.getJSONObject(0);
            JSONObject semantic = textobj.getJSONObject("semantic");
            JSONObject slots = semantic.getJSONObject("slots");
            JSONObject location = slots.getJSONObject("location");
            JSONObject datetime = slots.getJSONObject("datetime");
            String weather = todayResult.getString("weather");
            String tempRange = todayResult.getString("tempRange");
            String wind = todayResult.getString("wind");
            String humidity = todayResult.getString("humidity");
            String airQuality = todayResult.getString("airQuality");
            String city = todayResult.getString("city");
            String date = parserDatetime(datetime);
            ttsText = city + date + "天气" + weather + " " + tempRange + " 空气质量" + airQuality;
            if(ttsText == null || "".equals(ttsText)) {
                ttsText = DEFAULT_TEXT_NO_FOUND_ANSWER;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mNluLinstener != null) {
            NluResult res = new NluResult();
            res.setIntent(NluResult.NluIntent.SPEAK_TEXT);
            Bundle data = new Bundle();
            data.putString(NluResult.INTENT_DATA_TEXT, ttsText);
            res.setData(data);
            mNluLinstener.onResult(res);
        }
        return ttsText;
    }

    private String parserDatetime(JSONObject datetime) throws JSONException {
        String date = datetime.getString("date");
        String dateOrig = null;
        if(datetime.has("dateOrig")) {
            dateOrig = datetime.getString("dateOrig");
        }
        if("CURRENT_DAY".equals(date)) {
            return "今天";
        }
        if(dateOrig != null) {
            return dateOrig;
        }
        return "";
    }

    protected String parserMusic(String understandText) {
        JSONObject textobj = null;
        String ttsText = null;
        String singer = null;
        String musicName = null;
        String downloadUrl = null;
        try {
            textobj = new JSONObject(understandText);
            if(textobj == null) {
                return null;
            }
            JSONObject data = textobj.getJSONObject("data");
            JSONArray result = data.getJSONArray("result");
            JSONObject firstResult = result.getJSONObject(0);
            singer = firstResult.getString("singer");
            musicName = firstResult.getString("name");
            String url = firstResult.getString("downloadUrl");
            if (url != null) {
                downloadUrl = url.replace("\\/", "/");
            }
            ttsText = "为你播放" + musicName;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mNluLinstener != null) {
            NluResult res = new NluResult();
            res.setIntent(NluResult.NluIntent.PLAY_MUSIC);
            Bundle data = new Bundle();
            data.putString(NluResult.INTENT_DATA_URL, downloadUrl);
            res.setData(data);
            mNluLinstener.onResult(res);
        }
        return ttsText;
    }

    protected String parserAnswser(String understandText) {
        JSONObject textobj = null;
        String ttsText = null;
        try {
            textobj = new JSONObject(understandText);
            if(textobj == null) {
                return null;
            }
            JSONObject answer = textobj.getJSONObject("answer");
            String answerType = answer.getString("type");
            if(answerType.equals("T")) {
                ttsText = answer.getString("text");
            }
            return ttsText;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mNluLinstener != null) {
            NluResult res = new NluResult();
            res.setIntent(NluResult.NluIntent.SPEAK_TEXT);
            Bundle data = new Bundle();
            data.putString(NluResult.INTENT_DATA_TEXT, ttsText);
            res.setData(data);
            mNluLinstener.onResult(res);
        }
        return null;
    }

}
