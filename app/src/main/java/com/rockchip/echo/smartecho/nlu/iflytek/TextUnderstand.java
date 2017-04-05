package com.rockchip.echo.smartecho.nlu.iflytek;

import com.rockchip.echo.smartecho.SmartEchoComImpl;
import com.rockchip.echo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class TextUnderstand {

    private TextUnderstandResult mTextUnderstandResult;

    public TextUnderstand() {
    }

    public TextUnderstand(String text) {
        setUnderstandText(text);
    }

    public String getTtsText() {
        if (mTextUnderstandResult != null) {
            return mTextUnderstandResult.mTtsText;
        }
        return null;
    }

    public void setUnderstandText(String text) {
        parserFirst(text);
    }

    protected void parserFirst(String understandText) {
        String ttsText = null;
        try {
            JSONObject textJsonObj = new JSONObject(understandText);
            int rc = textJsonObj.getInt("rc");
            if(rc == 0) {
                String service = textJsonObj.getString("service");
                String operation = textJsonObj.getString("operation");
                LogUtil.d("TextUnderstand - service: " + service + " operation: " + operation);
                if("QUERY".equals(operation) && "weather".equals(service)) {
                    mTextUnderstandResult = new WeatherUnderstandResult();
                } else if("ANSWER".equals(operation)) {
                    mTextUnderstandResult = new AnswserUnderstandtResult();
                } else if("PLAY".equals(operation) && "music".equals(service)) {
                    mTextUnderstandResult = new MusicUnderstandResult();
                }
                if (mTextUnderstandResult != null) {
                    mTextUnderstandResult.rc = rc;
                    mTextUnderstandResult.service = service;
                    mTextUnderstandResult.operation = operation;
                    ttsText = mTextUnderstandResult.parser(understandText);
                    LogUtil.d("TextUnderstand - parser tts text: " + ttsText);
                }
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
        if (mTextUnderstandResult != null) {
            mTextUnderstandResult.mTtsText = ttsText;
        }
    }

    public void startAction(SmartEchoComImpl smartecho) {
        LogUtil.d("TextUnderstand - startAction");
        if (mTextUnderstandResult != null) {
            mTextUnderstandResult.onStartAction(smartecho);
        }
    }

    public void stopAction(SmartEchoComImpl smartecho) {
        LogUtil.d("TextUnderstand - stopAction");
        if (mTextUnderstandResult != null) {
            mTextUnderstandResult.onStopAction(smartecho);
            mTextUnderstandResult = null;
        }
    }
}
