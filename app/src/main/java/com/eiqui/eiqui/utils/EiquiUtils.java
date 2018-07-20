package com.eiqui.eiqui.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;

import com.eiqui.eiqui.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by uchar on 19/09/16.
 */
public class EiquiUtils {
    final static private String GROUP_NEW_TASK = "group_new_task";
    static private Integer mNotificationCount = 0;


    static public String dateToStringLong(Date date) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormatGmt.format(date);
    }

    static public Date stringLongToDate(String date) {
        Date dateParsed = null;
        try {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateParsed = dateFormatGmt.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateParsed;
    }

    // Source from: http://stackoverflow.com/questions/4846484/md5-hashing-in-android
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    static public int compareDateToday(Date date, Integer type, Integer diff) {
        Calendar cal = Calendar.getInstance();
        cal.add(type, diff);
        return cal.getTime().compareTo(date);
    }
    static public int compareDateToday(String date, Integer type, Integer diff) {
        Calendar cal = Calendar.getInstance();
        cal.add(type, diff);
        return cal.getTime().compareTo(stringLongToDate(date));
    }

    static public int rgbToInt(Integer r, Integer g, Integer b, Integer a) {
        int rgb = Math.min(a, 255);
        rgb = (rgb << 8) + Math.min(r, 255);
        rgb = (rgb << 8) + Math.min(g, 255);
        rgb = (rgb << 8) + Math.min(b, 255);
        return rgb;
    }

    // Random color idea from: http://stackoverflow.com/questions/9186038/php-generate-rgb#9186155
    static public int generateColorRandom(char letter) {
        String aleatStr = md5("COLOR"+letter);
        Integer r = Integer.parseInt(aleatStr.substring(0, 2), 16);
        Integer g = Integer.parseInt(aleatStr.substring(2, 4), 16);
        Integer b = Integer.parseInt(aleatStr.substring(4, 6), 16);
        return rgbToInt(r, g, b, 255);
    }

    static public void createNotification(Context context, Integer icon, String title, String text, Intent intent) {
        PendingIntent pIntent = PendingIntent.getActivity(context, ++mNotificationCount, intent, 0);

        Notification.Builder n = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setContentIntent(pIntent)
                .setGroup(GROUP_NEW_TASK)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationCount, n.getNotification());
    }

}
