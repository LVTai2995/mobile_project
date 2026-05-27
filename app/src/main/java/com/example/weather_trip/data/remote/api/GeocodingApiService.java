package com.example.weather_trip.data.remote.api;

import com.example.weather_trip.data.remote.model.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApiService {

    @GET("geo/1.0/direct")
    Call<List<GeocodingResponse.GeocodingResult>> searchPlaces(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}
