package com.example.weather_trip.service;

import android.util.Log;

import com.example.weather_trip.data.remote.ApiClient;
import com.google.gson.JsonObject;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GoogleOAuthService {

    private static final String TAG = "GoogleOAuthService";
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final OkHttpClient client;

    public GoogleOAuthService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void verifyToken(String idToken, GoogleCallback callback) {
        Request request = new Request.Builder()
                .url(GOOGLE_TOKEN_INFO_URL + "?id_token=" + idToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Token verification failed", e);
                callback.onError("Token verification failed: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String body = response.body().string();
                        JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();

                        GoogleUserInfo info = new GoogleUserInfo();
                        info.sub = json.get("sub").getAsString();
                        info.email = json.has("email") ? json.get("email").getAsString() : null;
                        info.name = json.has("name") ? json.get("name").getAsString() : null;
                        info.picture = json.has("picture") ? json.get("picture").getAsString() : null;

                        callback.onSuccess(info);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Error parsing response");
                    }
                } else {
                    callback.onError("Token verification failed: " + response.code());
                }
            }
        });
    }

    public interface GoogleCallback {
        void onSuccess(GoogleUserInfo info);
        void onError(String message);
    }

    public static class GoogleUserInfo {
        public String sub;
        public String email;
        public String name;
        public String picture;
    }
}
