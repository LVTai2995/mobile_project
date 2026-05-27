package com.example.weather_trip.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.weather_trip.R;
import com.example.weather_trip.WeatherTripApplication;
import com.example.weather_trip.presentation.event.EventDetailActivity;

public class NotificationHelper {

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void showReminderNotification(long eventId, String eventTitle) {
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) eventId, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WeatherTripApplication.CHANNEL_REMINDER_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhắc nhở sự kiện")
                .setContentText(eventTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) eventId, builder.build());
        }
    }
}
