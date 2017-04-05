package com.rockchip.echo.smartecho.nlu.iflytek;

import com.rockchip.echo.smartecho.SmartEchoComImpl;
import com.rockchip.echo.util.LogUtil;

public class TextUnderstandResult {

    int rc = 0;
    String text = "";
    String history = "";
    String service = "";
    String operation = "";
    String mTtsText;

    public static final String DEFAULT_TEXT_NO_FOUND_ANSWER = "没有为你查到";

    public TextUnderstandResult() {
    }

    public TextUnderstandResult(String text) {
        this.text = text;
    }

    protected String parser(String understandText) {
        return null;
    }

    protected void onStartAction(SmartEchoComImpl smartEchoComImpl) {
        LogUtil.d("TextUnderstandResult - onStartAction");
        LogUtil.d("====== mTtsText: " + mTtsText);
        if(mTtsText!= null && !mTtsText.equals("")) {
            smartEchoComImpl.startTtsOutput(mTtsText);
        }
    }

    protected void onStopAction(SmartEchoComImpl smartEchoComImpl) {
        LogUtil.d("TextUnderstandResult - onStopAction");
    }
}
