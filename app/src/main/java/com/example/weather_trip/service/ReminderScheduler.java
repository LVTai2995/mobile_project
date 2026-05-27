package com.example.weather_trip.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.weather_trip.receiver.ReminderBroadcastReceiver;

public class ReminderScheduler {

    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleReminder(long eventId, long eventTimeMillis, int reminderMinutesBefore, String eventTitle) {
        if (eventTimeMillis <= 0) return;

        long reminderTime = eventTimeMillis - (reminderMinutesBefore * 60 * 1000L);
        if (reminderTime <= System.currentTimeMillis()) return;

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", eventTitle);
        intent.putExtra("event_time_millis", eventTimeMillis);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) eventId, intent, flags);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        } catch (Exception e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    public void cancelReminder(long eventId) {
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) eventId, intent, flags);
        alarmManager.cancel(pendingIntent);
    }
}
