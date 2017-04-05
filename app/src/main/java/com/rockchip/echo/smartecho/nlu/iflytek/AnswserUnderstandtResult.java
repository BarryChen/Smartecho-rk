package com.rockchip.echo.smartecho.nlu.iflytek;

import org.json.JSONException;
import org.json.JSONObject;

class AnswserUnderstandtResult extends TextUnderstandResult {

    @Override
    protected String parser(String understandText) {
        JSONObject textobj = null;
        try {
            textobj = new JSONObject(understandText);
            if(textobj == null) {
                return null;
            }
            JSONObject answer = textobj.getJSONObject("answer");
            String answerType = answer.getString("type");
            if(answerType.equals("T")) {
                mTtsText = answer.getString("text");
            }
            return mTtsText;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
