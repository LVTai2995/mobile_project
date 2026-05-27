package com.example.weather_trip.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/";
    private static final String GEOCODING_BASE_URL = "https://api.openweathermap.org/";

    private static Retrofit weatherRetrofit = null;
    private static Retrofit geocodingRetrofit = null;

    public static Retrofit getWeatherRetrofit() {
        if (weatherRetrofit == null) {
            synchronized (ApiClient.class) {
                if (weatherRetrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    weatherRetrofit = new Retrofit.Builder()
                            .baseUrl(WEATHER_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return weatherRetrofit;
    }

    public static Retrofit getGeocodingRetrofit() {
        if (geocodingRetrofit == null) {
            synchronized (ApiClient.class) {
                if (geocodingRetrofit == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();

                    geocodingRetrofit = new Retrofit.Builder()
                            .baseUrl(GEOCODING_BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return geocodingRetrofit;
    }
}
