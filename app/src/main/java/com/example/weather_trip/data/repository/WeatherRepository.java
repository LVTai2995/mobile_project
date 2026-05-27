package com.example.weather_trip.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.weather_trip.BuildConfig;
import com.example.weather_trip.data.local.dao.WeatherCacheDao;
import com.example.weather_trip.data.remote.ApiClient;
import com.example.weather_trip.data.remote.api.WeatherApiService;
import com.example.weather_trip.data.remote.model.ForecastResponse;
import com.example.weather_trip.data.remote.model.WeatherResponse;
import com.example.weather_trip.domain.model.Weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private static final String TAG = "WeatherRepository";
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;

    private final WeatherApiService apiService;
    private final WeatherCacheDao cacheDao;

    public WeatherRepository(Context context) {
        this.apiService = ApiClient.getWeatherRetrofit().create(WeatherApiService.class);
        this.cacheDao = new WeatherCacheDao(context);
    }

    public void getWeatherByCity(String city, WeatherCallback callback) {
        android.util.Log.d("WeatherRepo", "getWeatherByCity: city=" + city + ", API_KEY=" + (API_KEY != null && !API_KEY.isEmpty() ? "SET" : "EMPTY"));
        int today = getTodayDate();

        Weather cached = cacheDao.getWeather(city, today);
        if (cached != null) {
            android.util.Log.d("WeatherRepo", "getWeatherByCity: returning cached weather");
            callback.onSuccess(cached);
            return;
        }

        android.util.Log.d("WeatherRepo", "getWeatherByCity: calling API");
        apiService.getWeatherByCity(city, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                android.util.Log.d("WeatherRepo", "getWeatherByCity: response code=" + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Weather weather = response.body().toWeather();
                    cacheDao.saveWeather(weather, city, today);
                    android.util.Log.d("WeatherRepo", "getWeatherByCity: success, temp=" + weather.getTemp());
                    callback.onSuccess(weather);
                } else {
                    android.util.Log.e("WeatherRepo", "getWeatherByCity: failed, code=" + response.code());
                    callback.onError("Không lấy được thời tiết: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                android.util.Log.e("WeatherRepo", "getWeatherByCity: network failure", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getForecastByCity(String city, ForecastCallback callback) {
        apiService.getForecastByCity(city, API_KEY, "metric").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Không lấy được dự báo: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Log.e(TAG, "Forecast API call failed", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        apiService.getWeatherByCoords(lat, lon, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().toWeather());
                } else {
                    callback.onError("Không lấy được thời tiết: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public Weather getWeatherByCitySync(String city) {
        int today = getTodayDate();
        Weather cached = cacheDao.getWeather(city, today);
        if (cached != null) return cached;

        try {
            Response<WeatherResponse> response = apiService.getWeatherByCity(city, API_KEY, "metric").execute();
            if (response.isSuccessful() && response.body() != null) {
                Weather weather = response.body().toWeather();
                cacheDao.saveWeather(weather, city, today);
                return weather;
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync API call failed", e);
        }
        return null;
    }

    private int getTodayDate() {
        return Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()));
    }

    public interface WeatherCallback {
        void onSuccess(Weather weather);
        void onError(String message);
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResponse forecast);
        void onError(String message);
    }
}
