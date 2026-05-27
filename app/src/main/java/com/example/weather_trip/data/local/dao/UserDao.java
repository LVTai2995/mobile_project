package com.example.weather_trip.data.local.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.weather_trip.data.local.DatabaseHelper;
import com.example.weather_trip.domain.model.User;

import org.mindrot.jbcrypt.BCrypt;

public class UserDao {

    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("password_hash", user.getPasswordHash());
        values.put("oauth_provider", user.getOauthProvider());
        values.put("oauth_id", user.getOauthId());
        values.put("display_name", user.getDisplayName());
        values.put("created_at", user.getCreatedAt());
        values.put("updated_at", user.getUpdatedAt());
        long id = db.insert("users", null, values);
        user.setId(id);
        return id;
    }

    public int update(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("password_hash", user.getPasswordHash());
        values.put("display_name", user.getDisplayName());
        values.put("updated_at", System.currentTimeMillis());
        return db.update("users", values, "id = ?", new String[]{String.valueOf(user.getId())});
    }

    public User findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User findByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "email = ?", new String[]{email}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User findByOauthId(String oauthId, String provider) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "oauth_id = ? AND oauth_provider = ?",
                new String[]{oauthId, provider}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User authenticate(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null,
                "email = ? AND oauth_provider = ?",
                new String[]{email, "email"}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            String storedHash = user.getPasswordHash();
            if (storedHash == null || !BCrypt.checkpw(password, storedHash)) {
                cursor.close();
                return null;
            }
        } else {
            cursor.close();
            return null;
        }
        cursor.close();
        return user;
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("users", "id = ?", new String[]{String.valueOf(id)});
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        user.setOauthProvider(cursor.getString(cursor.getColumnIndexOrThrow("oauth_provider")));
        user.setOauthId(cursor.getString(cursor.getColumnIndexOrThrow("oauth_id")));
        user.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow("display_name")));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        user.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        return user;
    }
}
