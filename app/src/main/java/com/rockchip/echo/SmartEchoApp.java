package com.rockchip.echo;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.rockchip.echo.smartecho.SmartEchoConfig;
import com.rockchip.echo.util.LogUtil;

import android.app.Application;

public class SmartEchoApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		SmartEchoConfig.loadConfig();
		SpeechUtility.createUtility(SmartEchoApp.this,
				SpeechConstant.APPID + "=" + SmartEchoConfig.appId);
		LogUtil.d("============ use appid = " + SmartEchoConfig.appId + " =============");
	}
}