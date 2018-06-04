package com.opop.brazius.chatroom;

import android.app.ActivityManager;
import android.content.Context;

import java.util.Calendar;
import java.util.Locale;



public class Utils {
    public static String getCurrentTime() {
        Calendar rightNow = Calendar.getInstance();
        int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMin = rightNow.get(Calendar.MINUTE);
        return String.format(Locale.ENGLISH, "%d:%d", currentHour, currentMin);
    }
}
