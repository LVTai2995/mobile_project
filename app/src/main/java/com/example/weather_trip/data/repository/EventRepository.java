package com.example.weather_trip.data.repository;

import android.content.Context;

import com.example.weather_trip.data.local.dao.EventDao;
import com.example.weather_trip.domain.model.Event;

import java.util.List;

public class EventRepository {

    private final EventDao eventDao;

    public EventRepository(Context context) {
        this.eventDao = new EventDao(context);
    }

    public long createEvent(Event event) {
        return eventDao.insert(event);
    }

    public int updateEvent(Event event) {
        return eventDao.update(event);
    }

    public int deleteEvent(long eventId) {
        return eventDao.delete(eventId);
    }

    public Event getEventById(long eventId) {
        return eventDao.findById(eventId);
    }

    public List<Event> getEventsByUserId(long userId) {
        return eventDao.findByUserId(userId);
    }

    public List<Event> getEventsByDateRange(long userId, int startDate, int endDate) {
        return eventDao.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<Event> getPendingReminders(long userId) {
        return eventDao.findPendingReminders(userId);
    }

    public List<Event> getAllPendingReminders() {
        return eventDao.findAllPendingReminders();
    }

    public int markReminderSent(long eventId) {
        return eventDao.markReminderSent(eventId);
    }

    public int markCompleted(long eventId) {
        return eventDao.markCompleted(eventId);
    }
}
