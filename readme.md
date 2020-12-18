# QttAudio开发者示例demo

这个项目演示了如何快速集成QttAudio的SDK，实现一对一通话或群聊功能

该示例项目实现了一下功能：

- 加入房间和离开房间
- 主播和观众模式切换
- 静麦和非静麦
- 扬声器和听筒的切换
- 音乐播放/暂停，停止，设置进度等
- 音效：变调，场景效果，如：笑声，拍手掌
- 自定义音频采集和渲染



## 运行环境

- Android Studio 3.0+
- 真机Android设备，不支持模拟器
- Android SDK API 等级 16 或以上

## 快速集成SDK

1. 下载[示例源码](https://github.com/qttaudio/Android_Example)，进行解压，SDK所在位置/app/lib的目录下；
2. 拷贝/app/lib的目录下.so和.jar，然后粘贴在原有的Android项目或者创建新的Android项目/app/lib的目录下，并在在项目的 /app/build.gradle 文件中，添加如下代码：

		android {
	    	...
		    sourceSets {
		        main {
		            jniLibs.srcDirs = ['libs']
		        }
		    }
		}

3. 在/app/src/main/AndroidManifest.xml 文件中添加如下内容：

		<?xml version="1.0" encoding="utf-8"?>
		<manifest xmlns:android="http://schemas.android.com/apk/res/android"
		    package="com.example.qttexample">
		
		     <uses-permission android:name="android.permission.BLUETOOTH" />
              <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
              <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
              <uses-permission android:name="android.permission.READ_PHONE_STATE" />
              <uses-permission android:name="android.permission.RECORD_AUDIO" />
            <uses-permission android:name="android.permission.INTERNET" />
		
		 	...
		
		</manifest>

4. 防止代码混淆

       -keep class com.qttaudio.**{*;}

>注意在Android6.0后一些比较重要的权限必须动态申请，不能只通过AndroidManifest.xml文件静态申请。在语音通话中录音权限应用必须允许的，否则不能采集你的音频数据发送给远端用户。

		public class MainActivity extends AppCompatActivity {
	
		    private static final int PERMISSION_REQUEST_CODE = 0x0001;
		    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
		
		    @Override
		    protected void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.activity_login);
		        isNeedRequestPermission();
		     
		    private boolean isNeedRequestPermission() {
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		            for (String permission : permissions) {
		                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
		                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
		                    return true;
		                }
		            }
		        }
		        return false;
		    }
	}




## 快速开始

1. 初始化引擎Engine
	
	初始化Engine需保证网络正常，SetContext(Context context)context不为空和SetAppkey(String appKey)不为空才能成功创建引擎Engine，可参考示例DEMO的**BaseEngineActivity.class**

		ChannelFactory.SetContext(this);
        ChannelFactory.SetAppkey(Constant.APP_KEY);
        ChannelFactory.SetLogFile("/mnt/sdcard/log");
        ChannelEngine mChannelEngine = ChannelFactory.GetChannelInstance();


2. 如果在初始化时注册想要监听的回调事件，那么添加如下代码：

	    mChannelEngine.setObserver(new ChannelObserver() {
			 //进入房间成功回调
            @Override
            public void onJoinSuccess(String s, long l, ChannelRole channelRole, boolean b, boolean b1) {
               
            }
			//进入房间失败回调
            @Override
            public void onJoinFail(int i, String s) {

            }

			//离开房间回调
            @Override
            public void onLeave() {

            }
			//连接中断回调
            @Override
            public void onConnectionBreak() {

            }
			//连接丢失回调
            @Override
            public void onConnectionLost() {

            }
			//发生错误回调
            @Override
            public void onError(int var1, String var2) {

            }
			//发生警告回调
            @Override
            public void onWarning(int var1, String var2) {

            }
			//人声检测回调
            @Override
            public void onTalking(VolumeInfo[] volumeInfos, int i) {

            }
			//远端用户进入房间回调
            @Override
            public void onOtherJoin(long var1, ChannelRole var3, boolean var4) {

            }
           		 //用户静音切换回调,uid==0时表示自己
            @Override
            public void onMuteStatusChanged(long uid, boolean var3){
            
            }

	     		//远端用户主播和观众模式切换回调
	     		//uid==0,自己主播和观众模式切换回调
            public void onRoleStatusChanged(long uid, ChannelRole var3) {

            }
	    		//远端用户的网络状态回调
	    		//var1==0,本地用户的网络状态回调
            @Override
            public void onNetworkStats(long var1, int var3, int var4, RtcStat var5) {

            }
			//远端用户离开房间
            @Override
            public void onOtherLeave(ChannelUser channelUser) {

            }
			//音源输出/输入切换回调
            @Override
            public void onAudioRouteChanged(int i) {

            }
			//音乐播放状态回调
            @Override
            public void onSoundStateChanged(int i) {

            }
			//场景音效播放完成回调
            @Override
            public void onEffectFinished(int i) {

            }
        });

	**如果需要在事件回调中更新UI，请确保在UI线程中操作，因为所有的事件回调都是工作线程。**

3. 进入房间


	 	//设置音频质量
        setAudioProfile(Constant.AUDIO_PROFILE_MUSIC_STANDARD, Constant.AUDIO_SCENARIO_GAME_STREAMING);
        //设置为观众
        mChannelEngine.changeRole(ChannelRole.AUDIENCE);
        //开启扬声器输出
        mChannelEngine.setSpeakerOn(true);
        //开启人声检测，延迟500ms
        mChannelEngine.setVolumeDectection(500);
		//加入房间：房间名称，token
        mChannelEngine.join(mRoomName, Constant.TOKEN);


4. 主播和观众模式切换
			
		//ChannelRole.AUDIENCE 观众   ChannelRole.TALKER 主播
	     mChannelEngine.changeRole(ChannelRole.TALKER);

5. 离开房间
	
	     mChannelEngine.leave();

6. 释放引擎资源

	    ChannelFactory.Destroy();



## 常用功能相关API

1. 主播和观众角色切换：

       
		mChannelEngine.changeRole(ChannelRole role);

2. 进入房间和离开房间：
        
        
		mChannelEngine.join(String roomId,String token);
		mChannelEngine.leave();

3. 闭麦与人声音量：
	
	
		mChannelEngine.mute(int uid, boolean b);
		mChannelEngine.adjustMicVolume(int volume);

4. 关闭声音

		mChannelEngine.muteAllRemote(boolean);

5. 音乐操作与音量设置：
	
		mChannelEngine.playSound(String path,int cycle,boolean publish);
		mChannelEngine.pauseSound();
		mChannelEngine.resumeSound();
		mChannelEngine.stopSound();
		
 		mChannelEngine.setSoundPublishVolume(int volue);
        mChannelEngine.setSoundPlayoutVolume(int volue);

## 运行程序

用Android Studio打开该项目，连接真机设备，编译并运行。进入房间开始可以进行语音通话。


## 联系我们
