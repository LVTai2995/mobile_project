package com.example.weather_trip.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.weather_trip.data.local.DatabaseHelper;
import com.example.weather_trip.domain.model.Weather;

public class WeatherCacheDao {

    private static final long CACHE_VALIDITY_MS = 3 * 60 * 60 * 1000;

    private final DatabaseHelper dbHelper;

    public WeatherCacheDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public Weather getWeather(String cityName, int date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("weather_cache", null,
                "city_name = ? AND cached_date = ?",
                new String[]{cityName, String.valueOf(date)},
                null, null, null);

        Weather weather = null;
        if (cursor.moveToFirst()) {
            weather = cursorToWeather(cursor);
            if (System.currentTimeMillis() - weather.getTimestamp() > CACHE_VALIDITY_MS) {
                weather = null;
            }
        }
        cursor.close();
        return weather;
    }

    public void saveWeather(Weather weather, String cityName, int date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("location_key", cityName + "_" + date);
        values.put("city_name", cityName);
        values.put("country_code", weather.getCountry());
        values.put("weather_id", weather.getWeatherId());
        values.put("weather_main", weather.getMain());
        values.put("weather_description", weather.getDescription());
        values.put("weather_icon", weather.getIcon());
        values.put("temperature", weather.getTemp());
        values.put("temperature_min", weather.getTempMin());
        values.put("temperature_max", weather.getTempMax());
        values.put("humidity", weather.getHumidity());
        values.put("wind_speed", weather.getWindSpeed());
        values.put("clouds", weather.getClouds());
        values.put("cached_date", date);
        values.put("fetched_at", System.currentTimeMillis());

        db.delete("weather_cache", "city_name = ? AND cached_date = ?",
                new String[]{cityName, String.valueOf(date)});
        db.insert("weather_cache", null, values);
    }

    public void clearOldCache() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long cutoff = System.currentTimeMillis() - CACHE_VALIDITY_MS * 24;
        db.delete("weather_cache", "fetched_at < ?", new String[]{String.valueOf(cutoff)});
    }

    private Weather cursorToWeather(Cursor cursor) {
        Weather weather = new Weather();
        weather.setWeatherId(cursor.getInt(cursor.getColumnIndexOrThrow("weather_id")));
        weather.setMain(cursor.getString(cursor.getColumnIndexOrThrow("weather_main")));
        weather.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("weather_description")));
        weather.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("weather_icon")));
        weather.setTemp(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature")));
        weather.setTempMin(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature_min")));
        weather.setTempMax(cursor.getDouble(cursor.getColumnIndexOrThrow("temperature_max")));
        weather.setHumidity(cursor.getInt(cursor.getColumnIndexOrThrow("humidity")));
        weather.setWindSpeed(cursor.getDouble(cursor.getColumnIndexOrThrow("wind_speed")));
        weather.setClouds(cursor.getInt(cursor.getColumnIndexOrThrow("clouds")));
        weather.setCityName(cursor.getString(cursor.getColumnIndexOrThrow("city_name")));
        weather.setCountry(cursor.getString(cursor.getColumnIndexOrThrow("country_code")));
        weather.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("fetched_at")));
        return weather;
    }
}
