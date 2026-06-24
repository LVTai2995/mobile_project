package com.example.weather_trip.domain.model;

public class Event {
    private long id;
    private long userId;
    private String title;
    private String description;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String countryCode;
    private int eventDate;
    private String eventTime;
    private Long eventDatetime;
    private int weatherId;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private Double temperature;
    private Double temperatureMin;
    private Double temperatureMax;
    private Integer humidity;
    private Double windSpeed;
    private Integer clouds;
    private String weatherCityName;
    private Long weatherTimestamp;
    private int reminderMinutes;
    private boolean reminderSent;
    private Long reminderAlarmId;
    private boolean isCompleted;
    private boolean isCancelled;
    private long createdAt;
    private long updatedAt;

    public Event() {
        this.reminderMinutes = 60;
        this.reminderSent = false;
        this.isCompleted = false;
        this.isCancelled = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public int getEventDate() { return eventDate; }
    public void setEventDate(int eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public Long getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Long eventDatetime) { this.eventDatetime = eventDatetime; }

    public int getWeatherId() { return weatherId; }
    public void setWeatherId(int weatherId) { this.weatherId = weatherId; }

    public String getWeatherMain() { return weatherMain; }
    public void setWeatherMain(String weatherMain) { this.weatherMain = weatherMain; }

    public String getWeatherDescription() { return weatherDescription; }
    public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }

    public String getWeatherIcon() { return weatherIcon; }
    public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(Double temperatureMin) { this.temperatureMin = temperatureMin; }

    public Double getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(Double temperatureMax) { this.temperatureMax = temperatureMax; }

    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }

    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public Integer getClouds() { return clouds; }
    public void setClouds(Integer clouds) { this.clouds = clouds; }

    public String getWeatherCityName() { return weatherCityName; }
    public void setWeatherCityName(String weatherCityName) { this.weatherCityName = weatherCityName; }

    public Long getWeatherTimestamp() { return weatherTimestamp; }
    public void setWeatherTimestamp(Long weatherTimestamp) { this.weatherTimestamp = weatherTimestamp; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public Long getReminderAlarmId() { return reminderAlarmId; }
    public void setReminderAlarmId(Long reminderAlarmId) { this.reminderAlarmId = reminderAlarmId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getWeatherEmoji() {
        if (weatherId == 800) return "☀️";
        if (weatherId <= 802) return "⛅";
        if (weatherId <= 804) return "☁️";
        if (weatherId < 600) return "🌧️";
        if (weatherId < 700) return "❄️";
        if (weatherId < 800) return "🌫️";
        if (weatherId < 900) return "⛈️";
        return "🌡️";
    }

    public String getWeatherVietnamese() {
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
        return weatherMain != null ? weatherMain : "Không rõ";
    }
}
