package com.example.weather_trip.data.local.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weather_trip.domain.model.User;

import java.util.UUID;

public class SessionDao {

    private static final String PREFS_NAME = "weather_trip_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_OAUTH_PROVIDER = "oauth_provider";

    private final SharedPreferences prefs;

    public SessionDao(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void createSession(User user) {
        String token = UUID.randomUUID().toString();
        prefs.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_NAME, user.getDisplayName())
                .putString(KEY_OAUTH_PROVIDER, user.getOauthProvider())
                .apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getOauthProvider() {
        return prefs.getString(KEY_OAUTH_PROVIDER, "email");
    }

    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1) != -1;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
