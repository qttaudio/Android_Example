package com.example.qttexample.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {


    /**
     * 获取当前时间戳
     * @return
     */
    public static long currentTimeMillis(){
        return System.currentTimeMillis();
    }

    /**
     * 获取当前日期和时间
     * @param formatType
     * @return
     */
    public static String getCurrentDateStr(String formatType){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(formatType);
        return sdf.format(date);
    }

    /**
     * 时间转换 Date ——> long
     * @param date
     * @return
     */
    public static long date2Long(Date date){
        return date.getTime();
    }

    /**
     * 时间转换 Date ——> String
     * @param date
     * @param formatType
     * @return
     */
    public static String date2String(Date date, String formatType){
        return new SimpleDateFormat(formatType).format(date);
    }

    /**
     * 时间转换 long ——> Date
     * @param time
     * @param formatType
     * @return
     */
    public static Date long2Date(long time, String formatType){
        Date oldDate = new Date(time);
        String dateStr = date2String(oldDate, formatType);
        Date date = string2Date(dateStr, formatType);
        return date;
    }

    /**
     * 时间转换 long ——> String
     * @param time
     * @param formatType
     * @return
     */
    public static String long2String(long time, String formatType){
        Date date = long2Date(time, formatType);
        String strTime = date2String(date, formatType);
        return strTime;
    }

    /**
     * 时间转换 String ——> Date
     * @param strTime
     * @param formatType
     * @return
     */
    public static Date string2Date(String strTime, String formatType){
        SimpleDateFormat format = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = format.parse(strTime);
        } catch ( ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 时间转换 String ——> long
     * @param strTime
     * @param formatType
     * @return
     */
    public static long string2Long(String strTime, String formatType){
        Date date = string2Date(strTime, formatType);
        if(date == null){
            return 0;
        } else {
            long time = date2Long(date);
            return time;
        }
    }

    /**
     * 将int类型数字转换成时分秒毫秒的格式数据
     */
    public static String msecToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            second = time / 1000;
            minute = second / 60;
            if (second < 60) {
                timeStr = "00:" + unitFormat(second);
            } else if (minute < 60) {
                second = second % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {// 数字>=3600 000的时候
                hour = minute / 60;
                minute = minute % 60;
                second = second - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {// 时分秒的格式转换
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    public static String unitFormat2(int i) {// 毫秒的格式转换
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "00" + Integer.toString(i);
        else if (i >= 10 && i < 100) {
            retStr = "0" + Integer.toString(i);
        } else
            retStr = "" + i;
        return retStr;
    }

}
