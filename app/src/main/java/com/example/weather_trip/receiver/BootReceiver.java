package com.example.weather_trip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.weather_trip.data.repository.EventRepository;
import com.example.weather_trip.domain.model.Event;
import com.example.weather_trip.service.ReminderScheduler;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            new Thread(() -> {
                ReminderScheduler scheduler = new ReminderScheduler(context);
                EventRepository eventRepo = new EventRepository(context);

                List<Event> pendingEvents = eventRepo.getAllPendingReminders();
                for (Event event : pendingEvents) {
                    if (event.getEventDatetime() != null && event.getEventTime() != null) {
                        scheduler.scheduleReminder(event.getId(), event.getEventDatetime(),
                                event.getReminderMinutes(), event.getTitle());
                    }
                }
            }).start();
        }
    }
}
