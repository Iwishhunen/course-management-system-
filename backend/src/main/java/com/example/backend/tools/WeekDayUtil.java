package com.example.backend.tools;


public class WeekDayUtil {

    public static String getWeekDayChinese(int weekDay) {
        return switch (weekDay) {
            case 1 -> "周一";
            case 2 -> "周二";
            case 3 -> "周三";
            case 4 -> "周四";
            case 5 -> "周五";
            case 6 -> "周六";
            case 7 -> "周日";
            default -> "未知";
        };
    }
}
