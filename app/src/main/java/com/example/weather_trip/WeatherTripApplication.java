package com.example.weather_trip;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class WeatherTripApplication extends Application {

    public static final String CHANNEL_REMINDER_ID = "reminder_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER_ID,
                    "Nhắc nhở sự kiện",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Thông báo nhắc nhở trước sự kiện");
            reminderChannel.enableVibration(true);
            reminderChannel.enableLights(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(reminderChannel);
            }
        }
    }
}
