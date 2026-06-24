package com.example.weather_trip.data.remote.api;

import com.example.weather_trip.data.remote.model.ForecastResponse;
import com.example.weather_trip.data.remote.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("data/2.5/forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("data/2.5/forecast")
    Call<ForecastResponse> getForecastByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
