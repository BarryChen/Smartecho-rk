# 应用信息

## 应用介绍
本Demo应用对接科大讯飞的语音识别、语音合成以及灵聚科技的智能引擎来实现智能语音对话的功能，并且适配瑞芯微发布的android sdk实现语音唤醒功能，本应用能够作为效果演示及代码参考。

## 应用包名
com.rockchip.smartecho


# 配置文件

## 配置文件简介
可以通过对smartecho.properties修改来实现对应用功能和参数进行配置，具体可配置参数请查看该文件。

## 配置文件路径
smartecho.properties可以放到设备的如下路径，应用会按照下面优先级查找并解析：

- /system/etc/smartecho.properties
- /sdcard/smartecho.properties


# 科大讯飞APPID

## APPID的更换
因为讯飞对每个appid的在线API调用有500次的限制，所以建议去[讯飞官网](http://www.xfyun.cn)注册并申请自己的APPID然后替换,以免测试时候因为超过次数而发生授权错误。

更换方法为:

- 替换smartecho.properties中的APPID的值
- 将讯飞官网下载的应用sdk压缩包的libs/armeabi-v7a和libs/armeabi目录中的libmsc.so拷贝到app/src/main/jniLibs/相应目录下覆盖（如果只有apk，可以直接将libmsc.so推到设备覆盖）

## 使用本地模式
如果暂时不想申请可以使用本地模式进行语音识别和合成，修改方法如下

- 把smartecho.properties里面的cloud改为local，如下
```
IAT_ENGINE_TYPE=local
TTS_ENGINE_TYPE=local
```
- 下载讯飞语记应用，并下载apk内的语音识别和语音合成离线资源

本地语音识别结果和语音合成能力和在线相比会稍差一些，但速度会更快。


# 更新历史
## v1.3.1
- alexa唤醒后停止播放

## v1.3.0
- 适配亚马逊alex
- 支持配置文件选择不同实现

## v1.2.4
- 重构代码，提高扩展性

## v1.2.3
- 添加api.ai语义理解

## v1.2.2
- 添加用户语音输入时候绿灯闪烁功能

## v1.2.1
- 将PARTIAL_WAKE_LOCK替换为FULL_WAKE_LOCK
- 关闭打印大量log

## v1.2.0
- 将语义理解替换为灵聚科技智能引擎
- 对应用加唤醒锁，防止进入休眠

## v1.1.0
- 支持音乐播放
- 支持配置文件进行配置

## v1.0.1
- 修改成后台服务方式
- 支持开机自启动

## v1.0.0
- 对接讯飞语音识别、语音合成、语义理解，实现语音对话
- 实现唤醒词唤醒
