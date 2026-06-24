package com.example.weather_trip.presentation.common;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.example.weather_trip.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Utility class for weather-reactive UI components
 */
public class WeatherUIHelper {

    public static final String WEATHER_SUNNY = "sunny";
    public static final String WEATHER_CLEAR = "clear";
    public static final String WEATHER_CLOUDS = "clouds";
    public static final String WEATHER_RAIN = "rain";
    public static final String WEATHER_DRIZZLE = "drizzle";
    public static final String WEATHER_THUNDERSTORM = "thunderstorm";
    public static final String WEATHER_SNOW = "snow";
    public static final String WEATHER_MIST = "mist";
    public static final String WEATHER_UNKNOWN = "unknown";

    public static String getWeatherType(String weatherMain) {
        if (weatherMain == null) return WEATHER_UNKNOWN;

        switch (weatherMain.toLowerCase()) {
            case "clear":
            case "sunny":
                return WEATHER_SUNNY;
            case "clouds":
            case "overcast":
                return WEATHER_CLOUDS;
            case "rain":
                return WEATHER_RAIN;
            case "drizzle":
                return WEATHER_DRIZZLE;
            case "thunderstorm":
                return WEATHER_THUNDERSTORM;
            case "snow":
                return WEATHER_SNOW;
            case "mist":
            case "fog":
            case "haze":
                return WEATHER_CLOUDS;
            default:
                return WEATHER_UNKNOWN;
        }
    }

    public static int getWeatherBackgroundColor(Context context, String weatherMain) {
        String type = getWeatherType(weatherMain);

        switch (type) {
            case WEATHER_SUNNY:
                return ContextCompat.getColor(context, R.color.weather_sunny_bg);
            case WEATHER_CLOUDS:
                return ContextCompat.getColor(context, R.color.weather_cloudy_bg);
            case WEATHER_RAIN:
            case WEATHER_DRIZZLE:
                return ContextCompat.getColor(context, R.color.weather_rainy_bg);
            case WEATHER_THUNDERSTORM:
                return ContextCompat.getColor(context, R.color.weather_stormy_bg);
            case WEATHER_SNOW:
                return ContextCompat.getColor(context, R.color.weather_snowy_bg);
            default:
                return ContextCompat.getColor(context, R.color.weather_default_bg);
        }
    }

    public static int getWeatherAccentColor(Context context, String weatherMain) {
        String type = getWeatherType(weatherMain);

        switch (type) {
            case WEATHER_SUNNY:
                return ContextCompat.getColor(context, R.color.weather_sunny_accent);
            case WEATHER_CLOUDS:
                return ContextCompat.getColor(context, R.color.weather_cloudy_accent);
            case WEATHER_RAIN:
            case WEATHER_DRIZZLE:
                return ContextCompat.getColor(context, R.color.weather_rainy_accent);
            case WEATHER_THUNDERSTORM:
                return ContextCompat.getColor(context, R.color.weather_stormy_accent);
            case WEATHER_SNOW:
                return ContextCompat.getColor(context, R.color.weather_snowy_accent);
            default:
                return ContextCompat.getColor(context, R.color.weather_default_accent);
        }
    }

    public static String getWeatherEmoji(String weatherMain) {
        if (weatherMain == null) return "🌤️";

        switch (weatherMain.toLowerCase()) {
            case "clear":
            case "sunny":
                return "☀️";
            case "clouds":
                return "☁️";
            case "rain":
                return "🌧️";
            case "drizzle":
                return "🌦️";
            case "thunderstorm":
                return "⛈️";
            case "snow":
                return "❄️";
            case "mist":
            case "fog":
            case "haze":
                return "🌫️";
            default:
                return "🌤️";
        }
    }

    public static void applyWeatherBackground(View cardView, String weatherMain) {
        Context context = cardView.getContext();
        int bgColor = getWeatherBackgroundColor(context, weatherMain);

        if (cardView instanceof MaterialCardView) {
            ((MaterialCardView) cardView).setCardBackgroundColor(bgColor);
        } else {
            cardView.setBackgroundColor(bgColor);
        }
    }

    public static void applyWeatherBackgroundWithAccent(View cardView, String weatherMain) {
        Context context = cardView.getContext();
        int bgColor = getWeatherBackgroundColor(context, weatherMain);
        int accentColor = getWeatherAccentColor(context, weatherMain);

        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(accentColor, hsv);
        int accentLight = android.graphics.Color.HSVToColor(
                android.graphics.Color.alpha(bgColor),
                new float[]{hsv[0], hsv[1] * 0.5f, 1f}
        );

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{accentLight, bgColor}
        );
        gradient.setCornerRadius(28f);
        cardView.setBackground(gradient);
    }
}
