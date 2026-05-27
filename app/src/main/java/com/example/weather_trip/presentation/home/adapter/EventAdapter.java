package com.example.weather_trip.presentation.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather_trip.R;
import com.example.weather_trip.domain.model.Event;
import com.example.weather_trip.presentation.common.WeatherUIHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EVENT = 1;

    private final List<Object> items = new ArrayList<>();
    private OnEventClickListener listener;
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM", Locale.US);
    private final SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        items.clear();
        // Use TreeMap to keep dates sorted
        Map<String, List<Event>> groupedEvents = new TreeMap<>();

        for (Event event : events) {
            String dateKey = formatDateKey(event.getEventDate());
            groupedEvents.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(event);
        }

        for (Map.Entry<String, List<Event>> entry : groupedEvents.entrySet()) {
            String dateKey = entry.getKey();
            Date date = parseDate(dateKey);
            if (date != null) {
                items.add(new DateHeader(date, fullDateFormat.format(date), null));
            } else {
                items.add(new DateHeader(null, dateKey, null));
            }
            items.addAll(entry.getValue());
        }

        notifyDataSetChanged();
    }

    private String formatDateKey(int eventDate) {
        return String.format(Locale.US, "%08d", eventDate);
    }

    private Date parseDate(String dateKey) {
        try {
            return parseDateFormat.parse(dateKey);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DateHeader ? TYPE_HEADER : TYPE_EVENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof HeaderViewHolder && item instanceof DateHeader) {
            ((HeaderViewHolder) holder).bind((DateHeader) item);
        } else if (holder instanceof EventViewHolder && item instanceof Event) {
            ((EventViewHolder) holder).bind((Event) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(DateHeader header) {
            tvDate.setText(header.formattedDate);
        }
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvLocation, tvTime, tvTemp, tvWeather, tvWeatherIcon, tvWeatherWarning;
        private final TextView tvDate;
        private final View weatherLayout;
        private final ImageView ivCompleted;
        private final View weatherAccentBar;

        EventViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            tvWeatherIcon = itemView.findViewById(R.id.tvWeatherIcon);
            tvWeatherWarning = itemView.findViewById(R.id.tvWeatherWarning);
            weatherLayout = itemView.findViewById(R.id.weatherLayout);
            ivCompleted = itemView.findViewById(R.id.ivCompleted);
            weatherAccentBar = itemView.findViewById(R.id.weatherAccentBar);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvLocation.setText(event.getLocationName());
            tvTime.setText(event.getEventTime() != null ? event.getEventTime() : "");

            // Format date for the chip
            Date eventDate = parseDate(formatDateKey(event.getEventDate()));
            if (eventDate != null) {
                tvDate.setText(shortDateFormat.format(eventDate));
            } else {
                tvDate.setText(String.valueOf(event.getEventDate()));
            }

            // Bind Weather Info
            if (event.getWeatherMain() != null) {
                weatherLayout.setVisibility(View.VISIBLE);
                tvWeatherIcon.setText(event.getWeatherEmoji());

                StringBuilder weatherInfo = new StringBuilder();
                if (event.getTemperatureMin() != null && event.getTemperatureMax() != null) {
                    String tempRange = String.format(Locale.US, "%.0f-%.0f°C", event.getTemperatureMin(), event.getTemperatureMax());
                    tvTemp.setText(tempRange);
                    weatherInfo.append(tempRange);
                } else if (event.getTemperature() != null) {
                    String temp = String.format(Locale.US, "%.0f°C", event.getTemperature());
                    tvTemp.setText(temp);
                    weatherInfo.append(temp);
                } else {
                    tvTemp.setText("");
                }

                String desc = event.getWeatherVietnamese();
                if (desc != null && !desc.isEmpty()) {
                    if (weatherInfo.length() > 0) weatherInfo.append(" • ");
                    weatherInfo.append(desc);
                }
                tvWeather.setText(weatherInfo.toString());

                // Set accent bar color based on weather
                int accentColor = WeatherUIHelper.getWeatherAccentColor(itemView.getContext(), event.getWeatherMain());
                weatherAccentBar.setBackgroundColor(accentColor);

                boolean hasWarning = event.getWeatherId() >= 200 && event.getWeatherId() < 600;
                tvWeatherWarning.setVisibility(hasWarning ? View.VISIBLE : View.GONE);
                if (hasWarning) {
                    tvWeatherWarning.setText("⚠️ Thời tiết xấu");
                }
            } else {
                weatherLayout.setVisibility(View.GONE);
                tvTemp.setText("");
                weatherAccentBar.setBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.primary));
            }

            ivCompleted.setVisibility(event.isCompleted() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }

    public static class DateHeader {
        public final Date date;
        public final String formattedDate;
        public final String weatherSummary;

        DateHeader(Date date, String formattedDate, String weatherSummary) {
            this.date = date;
            this.formattedDate = formattedDate;
            this.weatherSummary = weatherSummary;
        }
    }
}
