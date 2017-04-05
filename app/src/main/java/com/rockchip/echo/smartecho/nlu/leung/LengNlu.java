package com.rockchip.echo.smartecho.nlu.leung;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.lingju.common.util.BaseCrawler;
import com.lingju.context.entity.AudioEntity;
import com.lingju.context.entity.Command;
import com.lingju.context.entity.base.IChatResult;
import com.rockchip.echo.smartecho.nlu.Nlu;
import com.rockchip.echo.smartecho.nlu.NluListener;
import com.rockchip.echo.smartecho.nlu.NluResult;
import com.rockchip.echo.util.LogUtil;


public class LengNlu extends Nlu {

    private LeungNluImpl mLeungNlu;

    public LengNlu(Context context) {
        super(context);
        initLeungNlu();
    }

    public LengNlu(Context context, NluListener listener) {
        super(context, listener);
        initLeungNlu();
    }

    @Override
    public void startProcess(String text) {
        boolean res = mLeungNlu.process(text);
        LogUtil.d("robot.process>>" + text + ", result=" + Boolean.toString(res));
    }

    private LeungNluResultListener mLeungNluResultListener = new LeungNluResultListener() {
        @Override
        public void onResult(IChatResult result) {
            LogUtil.d("onResult>>text="+result.getText()+",cmd:" + result.cmd().toJsonString()
                    +"-result:"+result.cmd().getResult());
            if(result.getStatus()!=IChatResult.Status.SUCCESS){
                LogUtil.d("应答出错："+result.getStatus().toString());
                return;
            }

            switch(result.cmd().getType()) {
                case Command.Default.VALUE: {//通用指令
                    switch(result.cmd().getSubType()){
                        case Command.Default.DEFAULT://该指令无任何作用，可忽略
                            callListenerOfSpeakText(result.getText(), false);
                            break;
                        case Command.Default.ASK://反问指令，朗读完回复文本后主动启动识别侦听
                            callListenerOfSpeakText(result.getText(), true);
                            break;
                        case Command.Default.SYNTHESIZE_COMPLETELY://提升回复文本朗读的权重，即使被打断朗读，该指令也建议重新朗读
                            callListenerOfSpeakText(result.getText(), false);
                            break;
                        case Command.Default.DEFAULT_SYNTHESIZE_COMPLETELY_AND_ASK://前二者的和
                            callListenerOfSpeakText(result.getText(), true);
                            break;
                        default:break;
                    }
                    break;
                }
                case Command.AudioPlay.VALUE: {//音频播放指令
                    //二级指令为一系列的播放控制，请参照指令文档自行解析执行
                    AudioEntity music = result.getMusicList().get(0);
                    String musicId = music.getMusicId();
                    playMusicAsyc(musicId);
                    break;
                }
                case Command.AudioAction.VALUE: {// 音频实体操作指令
                    switch(result.cmd().getSubType()) {
                        case Command.AudioAction.FAVORITE_SPECIFIED:{
                            //收藏当前播放的歌曲列表
                            //...
                            break;
                        }
                        //其他操作指令不一一枚举，请参照指令文档自行增加
                        default:break;
                    }
                    break;
                }
           /* case Command.AudioExtract.VALUE:{
                //第三方音频资源搜索播放之音频实体提取指令，请参照指令文档解析执行
                break;
            }*/
                default:break;
            }

//            history.add(new Msg(result.getText(), Msg.OUTPUT_TYPE));
        }
    };

    private void callListenerOfSpeakText(String text, boolean isAsk) {
        if (mNluLinstener == null) {
            return;
        }
        NluResult res = new NluResult();
        if (isAsk) {
            res.setIntent(NluResult.NluIntent.SPEAK_ASK_TEXT);
        } else {
            res.setIntent(NluResult.NluIntent.SPEAK_TEXT);
        }
        Bundle data = new Bundle();
        data.putString(NluResult.INTENT_DATA_TEXT, text);
        res.setData(data);
        mNluLinstener.onResult(res);
    }

    private void callListenerOfPlayMusic(String url) {
        if (mNluLinstener == null) {
            return;
        }
        NluResult res = new NluResult();
        res.setIntent(NluResult.NluIntent.PLAY_MUSIC);
        Bundle data = new Bundle();
        data.putString(NluResult.INTENT_DATA_URL, url);
        res.setData(data);
        mNluLinstener.onResult(res);
    }

    private void playMusicAsyc(String musicId) {
        Bundle b = new Bundle();
        b.putString("musicId", musicId);
        Message msg = mLeungNluHandler.obtainMessage(LEUNG_NLU_HANDLER_MSG_GET_MUSIC_URL);
        msg.setData(b);
        msg.sendToTarget();
    }

    private void initLeungNlu() {
        mLeungNlu = new LeungNluImpl(mContext, mLeungNluResultListener);
        mLeungNluHandlerThread = new HandlerThread("LeungNluHandlerThread");
        mLeungNluHandlerThread.start();
        mLeungNluHandler = new Handler(mLeungNluHandlerThread.getLooper(), mLeungNluHandlerCallback);
    }

    private HandlerThread mLeungNluHandlerThread;
    private Handler mLeungNluHandler;
    private Handler.Callback mLeungNluHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
        switch (message.what) {
            case LEUNG_NLU_HANDLER_MSG_GET_MUSIC_URL:
                Bundle b = message.getData();
                if (b == null) {
                    break;
                }
                String musicId = b.getString("musicId");
                String musicUrl = new BaseCrawler().getMusicUri(musicId);
                LogUtil.d("get music url:" + musicUrl);
                callListenerOfPlayMusic(musicUrl);
                break;
            default:break;
        }
        return true;
        }
    };

    private static final int LEUNG_NLU_HANDLER_MSG_GET_MUSIC_URL = 0;
}
