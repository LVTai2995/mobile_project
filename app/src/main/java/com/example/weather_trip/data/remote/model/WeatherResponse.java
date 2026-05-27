package com.example.weather_trip.data.remote.model;

import com.example.weather_trip.domain.model.Weather;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("coord")
    private Coord coord;

    @SerializedName("weather")
    private List<WeatherInfo> weather;

    @SerializedName("main")
    private MainInfo main;

    @SerializedName("visibility")
    private int visibility;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("clouds")
    private Clouds clouds;

    @SerializedName("rain")
    private Rain rain;

    @SerializedName("snow")
    private Snow snow;

    @SerializedName("dt")
    private long timestamp;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("timezone")
    private int timezone;

    @SerializedName("id")
    private long cityId;

    @SerializedName("name")
    private String cityName;

    @SerializedName("cod")
    private int code;

    public Weather toWeather() {
        Weather w = new Weather();
        if (coord != null) {
            w.setLatitude(coord.lon);
            w.setLongitude(coord.lat);
        }
        if (weather != null && !weather.isEmpty()) {
            WeatherInfo info = weather.get(0);
            w.setWeatherId(info.id);
            w.setMain(info.main);
            w.setDescription(info.description);
            w.setIcon(info.icon);
        }
        if (main != null) {
            w.setTemp(main.temp);
            w.setFeelsLike(main.feelsLike);
            w.setTempMin(main.tempMin);
            w.setTempMax(main.tempMax);
            w.setHumidity(main.humidity);
            w.setPressure(main.pressure);
        }
        if (wind != null) {
            w.setWindSpeed(wind.speed);
            w.setWindDeg(wind.deg);
        }
        if (clouds != null) {
            w.setClouds(clouds.all);
        }
        if (rain != null) {
            w.setRain(rain.oneHour);
        }
        if (snow != null) {
            w.setSnow(snow.oneHour);
        }
        w.setVisibility(visibility);
        w.setTimestamp(timestamp);
        if (sys != null) {
            w.setCountry(sys.country);
            w.setSunrise(sys.sunrise);
            w.setSunset(sys.sunset);
        }
        w.setTimezone(timezone);
        w.setCityName(cityName);
        return w;
    }

    static class Coord {
        @SerializedName("lon")
        double lon;
        @SerializedName("lat")
        double lat;
    }

    static class WeatherInfo {
        @SerializedName("id")
        int id;
        @SerializedName("main")
        String main;
        @SerializedName("description")
        String description;
        @SerializedName("icon")
        String icon;
    }

    static class MainInfo {
        @SerializedName("temp")
        double temp;
        @SerializedName("feels_like")
        double feelsLike;
        @SerializedName("temp_min")
        double tempMin;
        @SerializedName("temp_max")
        double tempMax;
        @SerializedName("pressure")
        int pressure;
        @SerializedName("humidity")
        int humidity;
    }

    static class Wind {
        @SerializedName("speed")
        double speed;
        @SerializedName("deg")
        int deg;
    }

    static class Clouds {
        @SerializedName("all")
        int all;
    }

    static class Rain {
        @SerializedName("1h")
        double oneHour;
    }

    static class Snow {
        @SerializedName("1h")
        double oneHour;
    }

    static class Sys {
        @SerializedName("type")
        int type;
        @SerializedName("id")
        int id;
        @SerializedName("country")
        String country;
        @SerializedName("sunrise")
        long sunrise;
        @SerializedName("sunset")
        long sunset;
    }
}
