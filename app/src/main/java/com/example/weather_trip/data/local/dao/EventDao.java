package com.example.weather_trip.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.weather_trip.data.local.DatabaseHelper;
import com.example.weather_trip.domain.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDao {

    private final DatabaseHelper dbHelper;

    public EventDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public long insert(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = eventToContentValues(event);
        long id = db.insert("events", null, values);
        event.setId(id);
        return id;
    }

    public int update(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = eventToContentValues(event);
        values.put("updated_at", System.currentTimeMillis());
        return db.update("events", values, "id = ?", new String[]{String.valueOf(event.getId())});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("events", "id = ?", new String[]{String.valueOf(id)});
    }

    public Event findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("events", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Event event = null;
        if (cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
        }
        cursor.close();
        return event;
    }

    public List<Event> findByUserId(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("events", null, "user_id = ? AND is_cancelled = 0",
                new String[]{String.valueOf(userId)}, null, null, "event_date ASC, event_time ASC");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            events.add(cursorToEvent(cursor));
        }
        cursor.close();
        return events;
    }

    public List<Event> findByUserIdAndDateRange(long userId, int startDate, int endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("events", null,
                "user_id = ? AND event_date >= ? AND event_date <= ? AND is_cancelled = 0",
                new String[]{String.valueOf(userId), String.valueOf(startDate), String.valueOf(endDate)},
                null, null, "event_date ASC, event_time ASC");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            events.add(cursorToEvent(cursor));
        }
        cursor.close();
        return events;
    }

    public List<Event> findPendingReminders(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long now = System.currentTimeMillis();
        Cursor cursor = db.query("events", null,
                "user_id = ? AND reminder_sent = 0 AND is_cancelled = 0 AND event_datetime IS NOT NULL AND event_datetime > ?",
                new String[]{String.valueOf(userId), String.valueOf(now)},
                null, null, "event_datetime ASC");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            events.add(cursorToEvent(cursor));
        }
        cursor.close();
        return events;
    }

    public List<Event> findAllPendingReminders() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long now = System.currentTimeMillis();
        Cursor cursor = db.query("events", null,
                "reminder_sent = 0 AND is_cancelled = 0 AND event_datetime IS NOT NULL AND event_datetime > ?",
                new String[]{String.valueOf(now)},
                null, null, "event_datetime ASC");
        List<Event> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            events.add(cursorToEvent(cursor));
        }
        cursor.close();
        return events;
    }

    public int markReminderSent(long eventId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("reminder_sent", 1);
        return db.update("events", values, "id = ?", new String[]{String.valueOf(eventId)});
    }

    public int markCompleted(long eventId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_completed", 1);
        values.put("updated_at", System.currentTimeMillis());
        return db.update("events", values, "id = ?", new String[]{String.valueOf(eventId)});
    }

    private ContentValues eventToContentValues(Event event) {
        ContentValues values = new ContentValues();
        values.put("user_id", event.getUserId());
        values.put("title", event.getTitle());
        values.put("description", event.getDescription());
        values.put("location_name", event.getLocationName());
        values.put("latitude", event.getLatitude());
        values.put("longitude", event.getLongitude());
        values.put("country_code", event.getCountryCode());
        values.put("event_date", event.getEventDate());
        values.put("event_time", event.getEventTime());
        values.put("event_datetime", event.getEventDatetime());
        values.put("weather_id", event.getWeatherId());
        values.put("weather_main", event.getWeatherMain());
        values.put("weather_description", event.getWeatherDescription());
        values.put("weather_icon", event.getWeatherIcon());
        values.put("temperature", event.getTemperature());
        values.put("temperature_min", event.getTemperatureMin());
        values.put("temperature_max", event.getTemperatureMax());
        values.put("humidity", event.getHumidity());
        values.put("wind_speed", event.getWindSpeed());
        values.put("clouds", event.getClouds());
        values.put("weather_city_name", event.getWeatherCityName());
        values.put("weather_timestamp", event.getWeatherTimestamp());
        values.put("reminder_minutes", event.getReminderMinutes());
        values.put("reminder_sent", event.isReminderSent() ? 1 : 0);
        values.put("reminder_alarm_id", event.getReminderAlarmId());
        values.put("is_completed", event.isCompleted() ? 1 : 0);
        values.put("is_cancelled", event.isCancelled() ? 1 : 0);
        values.put("created_at", event.getCreatedAt());
        values.put("updated_at", event.getUpdatedAt());
        return values;
    }

    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        event.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        event.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow("location_name")));
        int latIdx = cursor.getColumnIndexOrThrow("latitude");
        event.setLatitude(cursor.isNull(latIdx) ? null : cursor.getDouble(latIdx));
        int lngIdx = cursor.getColumnIndexOrThrow("longitude");
        event.setLongitude(cursor.isNull(lngIdx) ? null : cursor.getDouble(lngIdx));
        event.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow("country_code")));
        event.setEventDate(cursor.getInt(cursor.getColumnIndexOrThrow("event_date")));
        event.setEventTime(cursor.getString(cursor.getColumnIndexOrThrow("event_time")));
        int dtIdx = cursor.getColumnIndexOrThrow("event_datetime");
        event.setEventDatetime(cursor.isNull(dtIdx) ? null : cursor.getLong(dtIdx));
        event.setWeatherId(cursor.getInt(cursor.getColumnIndexOrThrow("weather_id")));
        event.setWeatherMain(cursor.getString(cursor.getColumnIndexOrThrow("weather_main")));
        event.setWeatherDescription(cursor.getString(cursor.getColumnIndexOrThrow("weather_description")));
        event.setWeatherIcon(cursor.getString(cursor.getColumnIndexOrThrow("weather_icon")));
        int tempIdx = cursor.getColumnIndexOrThrow("temperature");
        event.setTemperature(cursor.isNull(tempIdx) ? null : cursor.getDouble(tempIdx));
        int tempMinIdx = cursor.getColumnIndexOrThrow("temperature_min");
        event.setTemperatureMin(cursor.isNull(tempMinIdx) ? null : cursor.getDouble(tempMinIdx));
        int tempMaxIdx = cursor.getColumnIndexOrThrow("temperature_max");
        event.setTemperatureMax(cursor.isNull(tempMaxIdx) ? null : cursor.getDouble(tempMaxIdx));
        int humIdx = cursor.getColumnIndexOrThrow("humidity");
        event.setHumidity(cursor.isNull(humIdx) ? null : cursor.getInt(humIdx));
        int windIdx = cursor.getColumnIndexOrThrow("wind_speed");
        event.setWindSpeed(cursor.isNull(windIdx) ? null : cursor.getDouble(windIdx));
        int cloudsIdx = cursor.getColumnIndexOrThrow("clouds");
        event.setClouds(cursor.isNull(cloudsIdx) ? null : cursor.getInt(cloudsIdx));
        event.setWeatherCityName(cursor.getString(cursor.getColumnIndexOrThrow("weather_city_name")));
        int wsTsIdx = cursor.getColumnIndexOrThrow("weather_timestamp");
        event.setWeatherTimestamp(cursor.isNull(wsTsIdx) ? null : cursor.getLong(wsTsIdx));
        event.setReminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow("reminder_minutes")));
        event.setReminderSent(cursor.getInt(cursor.getColumnIndexOrThrow("reminder_sent")) == 1);
        int raIdx = cursor.getColumnIndexOrThrow("reminder_alarm_id");
        event.setReminderAlarmId(cursor.isNull(raIdx) ? null : cursor.getLong(raIdx));
        event.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
        event.setCancelled(cursor.getInt(cursor.getColumnIndexOrThrow("is_cancelled")) == 1);
        event.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        event.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        return event;
    }
}
