package com.example.weather_trip.data.remote.model;

import com.example.weather_trip.domain.model.Weather;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ForecastResponse {

    @SerializedName("cod")
    private String code;

    @SerializedName("message")
    private int message;

    @SerializedName("cnt")
    private int count;

    @SerializedName("list")
    private List<ForecastItem> list;

    @SerializedName("city")
    private City city;

    public String getCode() { return code; }
    public int getMessage() { return message; }
    public int getCount() { return count; }
    public List<ForecastItem> getList() { return list; }
    public City getCity() { return city; }

    public List<Weather> getDailyForecasts() {
        List<Weather> dailyForecasts = new ArrayList<>();
        if (list == null || list.isEmpty()) return dailyForecasts;

        int lastDate = -1;
        for (ForecastItem item : list) {
            int date = (int) (item.dt / 86400);
            if (date != lastDate) {
                lastDate = date;
                Weather w = item.toWeather();
                if (city != null) {
                    w.setCityName(city.name);
                    w.setCountry(city.country);
                }
                dailyForecasts.add(w);
            }
        }
        return dailyForecasts;
    }

    public Weather getForecastForDate(int targetDate) {
        if (list == null || list.isEmpty()) return null;

        Weather closest = null;
        long minDiff = Long.MAX_VALUE;

        for (ForecastItem item : list) {
            int itemDate = (int) (item.dt / 86400);
            if (itemDate == targetDate) {
                long diff = Math.abs(item.dt * 1000 - System.currentTimeMillis());
                if (diff < minDiff) {
                    minDiff = diff;
                    closest = item.toWeather();
                }
            }
        }
        return closest;
    }

    static class City {
        @SerializedName("id")
        long id;
        @SerializedName("name")
        String name;
        @SerializedName("coord")
        Coord coord;
        @SerializedName("country")
        String country;
        @SerializedName("population")
        int population;
        @SerializedName("timezone")
        int timezone;
        @SerializedName("sunrise")
        long sunrise;
        @SerializedName("sunset")
        long sunset;
    }

    static class Coord {
        @SerializedName("lon")
        double lon;
        @SerializedName("lat")
        double lat;
    }

    static class ForecastItem {
        @SerializedName("dt")
        long dt;

        @SerializedName("main")
        MainInfo main;

        @SerializedName("weather")
        List<WeatherInfo> weather;

        @SerializedName("clouds")
        Clouds clouds;

        @SerializedName("wind")
        Wind wind;

        @SerializedName("visibility")
        int visibility;

        @SerializedName("pop")
        double pop;

        @SerializedName("rain")
        Rain rain;

        @SerializedName("sys")
        Sys sys;

        @SerializedName("dt_txt")
        String dtTxt;

        Weather toWeather() {
            Weather w = new Weather();
            w.setTimestamp(dt);

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

            w.setVisibility(visibility);
            w.setPop(pop);

            return w;
        }
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
        @SerializedName("sea_level")
        int seaLevel;
        @SerializedName("grnd_level")
        int grndLevel;
        @SerializedName("humidity")
        int humidity;
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

    static class Clouds {
        @SerializedName("all")
        int all;
    }

    static class Wind {
        @SerializedName("speed")
        double speed;
        @SerializedName("deg")
        int deg;
        @SerializedName("gust")
        double gust;
    }

    static class Rain {
        @SerializedName("3h")
        double threeHour;
    }

    static class Sys {
        @SerializedName("pod")
        String pod;
    }
}
