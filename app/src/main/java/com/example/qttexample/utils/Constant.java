package com.example.qttexample.utils;

import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Constant {

    /*
    * https://console.qttaudio.com/#/overview/overview
    * appkey到控制台申请
    * */
    public static final String APP_KEY="923ed094330a06b7c75e41332219774b";

    /*可以不用填*/
    public static final String TOKEN="";

    public static final String ROOM_NAME="room_name";
    public static final String QTT_INFO="qtt_info";
    public static final String SP_KEY_ROOM_NAME="sp_key_room_name";

    public static final int AUDIO_PROFILE_DEFAULT = 0;
    public static final int AUDIO_PROFILE_SPEECH_STANDARD = 1;
    public static final int AUDIO_SCENARIO_DEFAULT = 0;
    public static final int AUDIO_SCENARIO_CHATROOM_GAMING = 5;
    public static final int WHAT_ON_OVER_CONN = 1;
    public static final int WHAT_ON_RECONN = 2;




    public static String getDeviceName() {
        String deviceName = "";
        try {
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method get = SystemProperties.getDeclaredMethod("get", String.class, String.class);
            deviceName = (String) get.invoke(SystemProperties, "ro.product.marketname", "");
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = (String) get.invoke(SystemProperties, "ro.product.model", "");
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deviceName;
    }
}
