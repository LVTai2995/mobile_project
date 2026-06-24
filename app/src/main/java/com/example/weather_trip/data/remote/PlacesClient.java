package com.example.weather_trip.data.remote;

import com.example.weather_trip.data.remote.api.GeocodingApiService;
import com.example.weather_trip.data.remote.model.GeocodingResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlacesClient {

    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (PlacesClient.class) {
                if (retrofit == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static GeocodingApiService getGeocodingService() {
        return getRetrofit().create(GeocodingApiService.class);
    }
}
