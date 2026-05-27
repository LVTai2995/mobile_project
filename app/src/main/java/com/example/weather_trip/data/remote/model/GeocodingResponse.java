package com.example.weather_trip.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeocodingResponse {

    @SerializedName("results")
    private List<GeocodingResult> results;

    public List<GeocodingResult> getResults() {
        return results;
    }

    public static class GeocodingResult {
        @SerializedName("name")
        private String name;

        @SerializedName("local_names")
        private Object localNames;

        @SerializedName("lat")
        private double lat;

        @SerializedName("lon")
        private double lon;

        @SerializedName("country")
        private String country;

        @SerializedName("state")
        private String state;

        public String getName() { return name; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
        public String getCountry() { return country; }
        public String getState() { return state; }

        public String getDisplayName() {
            StringBuilder sb = new StringBuilder(name);
            if (state != null && !state.isEmpty()) {
                sb.append(", ").append(state);
            }
            if (country != null && !country.isEmpty()) {
                sb.append(", ").append(country);
            }
            return sb.toString();
        }

        public String getCity() {
            if (state != null && !state.isEmpty()) {
                return state;
            }
            return name;
        }
    }
}
