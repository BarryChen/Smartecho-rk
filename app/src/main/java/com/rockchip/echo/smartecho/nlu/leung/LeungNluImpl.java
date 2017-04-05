package com.rockchip.echo.smartecho.nlu.leung;

import android.content.Context;

import com.lingju.common.adapter.LocationAdapter;
import com.lingju.common.adapter.MusicContext;
import com.lingju.common.adapter.NetworkAdapter;
import com.lingju.common.adapter.PropertiesAccessAdapter;
import com.lingju.common.callback.ResponseCallBack;
import com.lingju.context.entity.AudioEntity;
import com.lingju.context.entity.Command;
import com.lingju.context.entity.base.IChatResult;
import com.lingju.robot.AndroidChatRobotBuilder;
import com.rockchip.echo.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

/**
 * Leung Nature Language Understanding
 */
public class LeungNluImpl implements ResponseCallBack {

    private Context mContext;
    private final List<Msg> history=new ArrayList<Msg>();
    private LeungNluResultListener mLeungNluResultListener;

    public LeungNluImpl(Context context, LeungNluResultListener listener) {
        mContext = context;
        mLeungNluResultListener = listener;
        init();
    }

    public void init() {
        AndroidChatRobotBuilder.create(mContext, "5726052bf94ebfc2a551e661b9ee036c")
                .setPropertiesAccessAdapter(propertiesAccessAdapter)
                .setMusicContext(musicContext)
                .setLocationAdapter(locationAdapter)
                .setNetworkAdapter(networkAdapter)
                .build();
    }

    private PropertiesAccessAdapter propertiesAccessAdapter = new PropertiesAccessAdapter() {
        @Override
        public void saveUserName(String s) {
            //持久化用户为自己设置的名字
            System.out.println("saveUserName>>"+s);
        }

        @Override
        public String getUserName() {
            //获取用户为自己设置的名字
            return "主人";
        }

        @Override
        public void saveRobotName(String s) {
            //保存用户设置的机器人名字
        }

        @Override
        public String getRobotName() {
            //获取用户为机器人设置的名字
            return "小灵";
        }

        @Override
        public void saveGender(int i) {
            //保存用户设置的机器人的性别
        }

        @Override
        public String getGender() {
            //获取用户设置的机器人的性别
            return "保密";
        }

        @Override
        public void saveBirthday(Date date) {
            //保存用户设置的机器人的出生年月日
        }

        @Override
        public Date getBirthday() {
            //获取用户设置的机器人的出生年月日
            return new Date(System.currentTimeMillis()-3600000*365);
        }

        @Override
        public void saveParent(String s) {
            //保存用户设置的机器人的父母
        }

        @Override
        public String getParent() {
            //获取用户设置的机器人的父母,暂不支持
            return null;
        }

        @Override
        public void saveFather(String s) {

        }

        @Override
        public void saveMother(String s) {

        }

        @Override
        public String getFather() {
            return "天父";
        }

        @Override
        public String getMother() {
            return "圣母玛利亚";
        }

        @Override
        public String getWeight() {
            return "10千克";
        }

        @Override
        public String getHeight() {
            return "40厘米";
        }

        @Override
        public String getMaker() {
            return "广州灵聚";
        }

        @Override
        public String getBirthplace() {
            return "广州";
        }

        @Override
        public String getIntroduce() {
            return "你猜";
        }
    };

    private NetworkAdapter networkAdapter=new NetworkAdapter() {
        @Override
        public boolean isOnline() {
            return true;
        }

        @Override
        public NetType currentNetworkType() {
            return null;
        }
    };

    private LocationAdapter locationAdapter = new LocationAdapter() {

        @Override
        public double getCurLng() {
            return 113.954334;
        }

        @Override
        public double getCurLat() {
            return 22.560235;
        }

        @Override
        public String getCurCity() {
            return "深圳市";
        }

        @Override
        public String getCurAddressDetail() {
            return "广东省深圳市";
        }
    };

    /**
     * 音乐播放的上下文接口.
     * 实现该接口是为了让聊天机器人能够随时获取当前播放的音频文件的信息
     *
     */
    MusicContext musicContext = new MusicContext() {
        /**
         * 获取当前播放歌曲的名字
         * @return 若无返回null
         */
        public String getName(){
            //...
            return "未知歌曲";
        }
        /**
         * 获取当前播放歌曲的演唱歌手
         * @return 若无返回null
         */
        public String getSinger(){
            //...
            return "未知歌手";
        }
        /**
         * 获取当前播放歌曲所属的专辑名称
         * @return 若无返回null
         */
        public String getAlums(){
            //...
            return null;
        }
        /**
         * 获取当前播放歌曲的的MusicId<br>
         * 如果当前播放的歌曲是在线歌曲，返回MusicId,如果是本地音频，返回音频文件的绝对路径
         * @return 若无返回null
         */
        public String getMusicId(){
            //...
            return null;
        }
        /**
         * 根据歌曲名获取本地对应的`歌曲集合
         * @param name 歌曲名，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicByName(String name){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 根据歌手获取本地对应歌手的所有歌曲集合
         * @param singer 歌手名，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicBySinger(String singer){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 获取本地对应专辑的所有歌曲集合
         * @param album 专辑名，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicByAlbum(String album){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 获取本地对应歌曲名+歌手的歌曲集合
         * @param name 歌名，需判空
         * @param singer 歌手，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicByNameAndSinger(String name,String singer){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 获取本地对应歌曲名+专辑名的歌曲实体集合
         * @param name 歌曲名，需判空
         * @param album 专辑名，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicByNameAndAlbum(String name,String album){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 根据歌手或者歌名获取本地对应歌曲集合
         * @param str 歌名or歌手，需判空
         * @return 若无返回空list，不能为null
         */
        public List<AudioEntity> getMusicByNameOrSinger(String str){
            //...
            return new ArrayList<AudioEntity>();
        }
        /**
         * 当前播放列表歌曲是否是在线歌曲
         * @return true：在线，false：离线
         */
        public boolean isOnlineMC(){
            //请维护当前播放歌曲的信息，返回真实的情况
            return true;
        }
        /**
         * 判断手机里是否有歌曲
         * @return true：有，false：没有
         */
        public boolean hasMusic(){
            //检索手机里的本地歌曲，请按实际返回
            return false;
        }
    };

    /**
     * 该方法在后台线程执行，请勿在该线程更新ui
     * @param result
     */
    @Override
    public void onResult(IChatResult result) {
        mLeungNluResultListener.onResult(result);
    }

    static class Msg {
        public static final int INPUT_TYPE=0;
        public static final int OUTPUT_TYPE=1;

        private String message;
        private int type;

        public Msg(String message, int type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public boolean process(String text) {
        return AndroidChatRobotBuilder.get().robot().process(text, this);
    }
}
