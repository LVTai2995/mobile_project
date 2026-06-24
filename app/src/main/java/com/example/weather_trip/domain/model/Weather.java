package com.example.weather_trip.domain.model;

public class Weather {
    private double latitude;
    private double longitude;
    private int weatherId;
    private String main;
    private String description;
    private String icon;
    private double temp;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private int windDeg;
    private int clouds;
    private int visibility;
    private double rain;
    private double snow;
    private long timestamp;
    private String cityName;
    private String country;
    private long sunrise;
    private long sunset;
    private int timezone;
    private double pop;

    public Weather() {}

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getWeatherId() { return weatherId; }
    public void setWeatherId(int weatherId) { this.weatherId = weatherId; }

    public String getMain() { return main; }
    public void setMain(String main) { this.main = main; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public int getWindDeg() { return windDeg; }
    public void setWindDeg(int windDeg) { this.windDeg = windDeg; }

    public int getClouds() { return clouds; }
    public void setClouds(int clouds) { this.clouds = clouds; }

    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }

    public double getRain() { return rain; }
    public void setRain(double rain) { this.rain = rain; }

    public double getSnow() { return snow; }
    public void setSnow(double snow) { this.snow = snow; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public long getSunrise() { return sunrise; }
    public void setSunrise(long sunrise) { this.sunrise = sunrise; }

    public long getSunset() { return sunset; }
    public void setSunset(long sunset) { this.sunset = sunset; }

    public int getTimezone() { return timezone; }
    public void setTimezone(int timezone) { this.timezone = timezone; }

    public double getPop() { return pop; }
    public void setPop(double pop) { this.pop = pop; }

    public String getEmoji() {
        if (weatherId == 800) return "☀️";
        if (weatherId <= 802) return "⛅";
        if (weatherId <= 804) return "☁️";
        if (weatherId < 600) return "🌧️";
        if (weatherId < 700) return "❄️";
        if (weatherId < 800) return "🌫️";
        if (weatherId < 900) return "⛈️";
        return "🌡️";
    }

    public String getVietnamese() {
        if (weatherId == 800) return "Trời quang";
        if (weatherId == 801) return "Trời ít mây";
        if (weatherId == 802) return "Mây rải rác";
        if (weatherId == 803) return "Mây đứt gãy";
        if (weatherId == 804) return "Trời âm u";
        if (weatherId == 500) return "Mưa nhẹ";
        if (weatherId == 501) return "Mưa vừa";
        if (weatherId == 502) return "Mưa to";
        if (weatherId >= 200 && weatherId < 300) return "Giông";
        if (weatherId >= 600 && weatherId < 700) return "Tuyết";
        if (weatherId == 701 || weatherId == 741) return "Sương mù";
        return main != null ? main : "Không rõ";
    }
}
