package com.example.weather_trip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.weather_trip.data.repository.EventRepository;
import com.example.weather_trip.service.NotificationHelper;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra("event_id", -1);
        String eventTitle = intent.getStringExtra("event_title");
        long eventTimeMillis = intent.getLongExtra("event_time_millis", 0);

        if (eventId != -1 && eventTitle != null) {
            if (eventTimeMillis > 0 && eventTimeMillis <= System.currentTimeMillis()) {
                markEventCompleted(context, eventId);
                return;
            }
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showReminderNotification(eventId, eventTitle);
        }
    }

    private void markEventCompleted(Context context, long eventId) {
        new Thread(() -> {
            EventRepository eventRepository = new EventRepository(context);
            eventRepository.markCompleted(eventId);
        }).start();
    }
}
