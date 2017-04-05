package com.rockchip.echo.smartecho.nlu;


import android.content.Context;

import com.rockchip.echo.smartecho.SmartEchoConfig;
import com.rockchip.echo.smartecho.nlu.aiapi.ApiaiNlu;
import com.rockchip.echo.smartecho.nlu.iflytek.IflytekNlu;
import com.rockchip.echo.smartecho.nlu.leung.LengNlu;

public abstract class Nlu {

    protected NluListener mNluLinstener;
    protected Context mContext;

    public Nlu(Context context) {
        mContext = context;
    }

    public Nlu(Context context, NluListener listener) {
        mNluLinstener = listener;
        mContext = context;
    }

    public abstract void startProcess(String text);

    public void setNluLinstener(NluListener linstener) {
        mNluLinstener = linstener;
    }

    public static Nlu getInstance(Context context, NluListener listener) {
        if ("iflytek".equals(SmartEchoConfig.nluimpl)) {
            return new IflytekNlu(context, listener);
        } else if ("leung".equals(SmartEchoConfig.nluimpl)){
            return new LengNlu(context, listener);
        } else {
            return new LengNlu(context, listener);
        }
    }

}
