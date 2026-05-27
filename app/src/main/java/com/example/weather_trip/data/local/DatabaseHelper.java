package com.example.weather_trip.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weather_trip.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT NOT NULL UNIQUE," +
                "password_hash TEXT," +
                "oauth_provider TEXT DEFAULT 'email'," +
                "oauth_id TEXT," +
                "display_name TEXT," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL," +
                "UNIQUE(oauth_provider, oauth_id)" +
                ")");

        db.execSQL("CREATE TABLE events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "location_name TEXT NOT NULL," +
                "latitude REAL," +
                "longitude REAL," +
                "country_code TEXT," +
                "event_date INTEGER NOT NULL," +
                "event_time TEXT," +
                "event_datetime INTEGER," +
                "weather_id INTEGER," +
                "weather_main TEXT," +
                "weather_description TEXT," +
                "weather_icon TEXT," +
                "temperature REAL," +
                "temperature_min REAL," +
                "temperature_max REAL," +
                "humidity INTEGER," +
                "wind_speed REAL," +
                "clouds INTEGER," +
                "weather_city_name TEXT," +
                "weather_timestamp INTEGER," +
                "reminder_minutes INTEGER DEFAULT 60," +
                "reminder_sent INTEGER DEFAULT 0," +
                "reminder_alarm_id INTEGER," +
                "is_completed INTEGER DEFAULT 0," +
                "is_cancelled INTEGER DEFAULT 0," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")");

        db.execSQL("CREATE TABLE weather_cache (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "location_key TEXT NOT NULL," +
                "city_name TEXT NOT NULL," +
                "country_code TEXT," +
                "weather_id INTEGER," +
                "weather_main TEXT," +
                "weather_description TEXT," +
                "weather_icon TEXT," +
                "temperature REAL," +
                "temperature_min REAL," +
                "temperature_max REAL," +
                "humidity INTEGER," +
                "wind_speed REAL," +
                "clouds INTEGER," +
                "cached_date INTEGER NOT NULL," +
                "fetched_at INTEGER NOT NULL," +
                "UNIQUE(city_name, cached_date)" +
                ")");

        db.execSQL("CREATE TABLE sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "token TEXT NOT NULL," +
                "device_info TEXT," +
                "is_active INTEGER DEFAULT 1," +
                "created_at INTEGER NOT NULL," +
                "expires_at INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")");

        db.execSQL("CREATE INDEX idx_events_user ON events(user_id)");
        db.execSQL("CREATE INDEX idx_events_date ON events(event_date)");
        db.execSQL("CREATE INDEX idx_events_user_date ON events(user_id, event_date)");
        db.execSQL("CREATE INDEX idx_events_reminder ON events(event_datetime, reminder_sent)");
        db.execSQL("CREATE INDEX idx_weather_cache_lookup ON weather_cache(city_name, cached_date)");
        db.execSQL("CREATE INDEX idx_sessions_token ON sessions(token)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS sessions");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS weather_cache");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
