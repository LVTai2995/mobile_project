package com.example.weather_trip.presentation.event;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.weather_trip.R;
import com.example.weather_trip.data.repository.EventRepository;
import com.example.weather_trip.domain.model.Event;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.service.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDetailActivity extends BaseActivity {

    private ImageButton btnBack, btnEdit, btnDelete;
    private TextView tvTitle, tvDate, tvTime, tvLocation, tvDescription, tvDescriptionLabel;
    private TextView tvReminder;
    private MaterialCardView weatherCard;
    private TextView tvWeatherCity, tvWeatherIcon, tvWeatherTemp, tvWeatherDesc;
    private TextView tvHumidity, tvTempRange, tvWind, tvWeatherAdvice;
    private MaterialButton btnComplete;

    private EventRepository eventRepository;
    private ReminderScheduler reminderScheduler;
    private Handler mainHandler;
    private ExecutorService executor;

    private long eventId;
    private Event currentEvent;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
    private final SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            showToast("Không tìm thấy sự kiện");
            finish();
            return;
        }

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        eventRepository = new EventRepository(this);
        reminderScheduler = new ReminderScheduler(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvent();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvDescriptionLabel = findViewById(R.id.tvDescriptionLabel);
        tvReminder = findViewById(R.id.tvReminder);
        weatherCard = findViewById(R.id.weatherCard);
        tvWeatherCity = findViewById(R.id.tvWeatherCity);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvTempRange = findViewById(R.id.tvTempRange);
        tvWind = findViewById(R.id.tvWind);
        tvWeatherAdvice = findViewById(R.id.tvWeatherAdvice);
        btnComplete = findViewById(R.id.btnComplete);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnComplete.setOnClickListener(v -> completeEvent());
    }

    private void loadEvent() {
        executor.execute(() -> {
            currentEvent = eventRepository.getEventById(eventId);
            mainHandler.post(this::displayEvent);
        });
    }

    private void displayEvent() {
        if (currentEvent == null) {
            showToast("Không tìm thấy sự kiện");
            finish();
            return;
        }

        tvTitle.setText(currentEvent.getTitle());

        // Parse eventDate as yyyyMMdd integer and format for display
        String dateStr = String.format(Locale.US, "%08d", currentEvent.getEventDate());
        try {
            Date eventDate = parseDateFormat.parse(dateStr);
            tvDate.setText(displayDateFormat.format(eventDate));
        } catch (ParseException e) {
            tvDate.setText(String.valueOf(currentEvent.getEventDate()));
        }
        tvTime.setText(currentEvent.getEventTime() != null ? currentEvent.getEventTime() : "Không có giờ cụ thể");
        tvLocation.setText(currentEvent.getLocationName());

        String reminderText;
        int mins = currentEvent.getReminderMinutes();
        if (mins < 60) {
            reminderText = mins + " phút trước";
        } else if (mins < 1440) {
            reminderText = (mins / 60) + " giờ trước";
        } else {
            reminderText = (mins / 1440) + " ngày trước";
        }
        tvReminder.setText(reminderText);

        if (currentEvent.getDescription() != null && !currentEvent.getDescription().isEmpty()) {
            tvDescription.setText(currentEvent.getDescription());
            tvDescriptionLabel.setVisibility(View.VISIBLE);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescriptionLabel.setVisibility(View.GONE);
            tvDescription.setVisibility(View.GONE);
        }

        if (currentEvent.getWeatherId() > 0) {
            weatherCard.setVisibility(View.VISIBLE);
            tvWeatherCity.setText(currentEvent.getWeatherCityName());
            tvWeatherIcon.setText(currentEvent.getWeatherEmoji());
            tvWeatherTemp.setText(String.format("%.0f°C", currentEvent.getTemperature() != null ? currentEvent.getTemperature() : 0));
            tvWeatherDesc.setText(currentEvent.getWeatherVietnamese());
            tvHumidity.setText(currentEvent.getHumidity() + "%");
            tvTempRange.setText(String.format("%.0f-%.0f°C",
                    currentEvent.getTemperatureMin() != null ? currentEvent.getTemperatureMin() : 0,
                    currentEvent.getTemperatureMax() != null ? currentEvent.getTemperatureMax() : 0));
            tvWind.setText(String.format("%.1f m/s", currentEvent.getWindSpeed() != null ? currentEvent.getWindSpeed() : 0));

            int weatherId = currentEvent.getWeatherId();
            if (weatherId >= 200 && weatherId < 600) {
                tvWeatherAdvice.setVisibility(View.VISIBLE);
                tvWeatherAdvice.setText("⚠️ Có thể mưa, nên mang theo ô!");
            } else if (weatherId == 800) {
                tvWeatherAdvice.setVisibility(View.VISIBLE);
                tvWeatherAdvice.setText("🌟 Thời tiết lý tưởng!");
            } else {
                tvWeatherAdvice.setVisibility(View.GONE);
            }
        } else {
            weatherCard.setVisibility(View.GONE);
        }

        if (currentEvent.isCompleted()) {
            btnComplete.setText("Đã hoàn thành");
            btnComplete.setEnabled(false);
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc muốn xóa sự kiện này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteEvent())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteEvent() {
        showLoading("Đang xóa...");

        executor.execute(() -> {
            reminderScheduler.cancelReminder(eventId);
            eventRepository.deleteEvent(eventId);

            mainHandler.post(() -> {
                hideLoading();
                showToast("Đã xóa sự kiện");
                finish();
            });
        });
    }

    private void completeEvent() {
        showLoading("Đang cập nhật...");

        executor.execute(() -> {
            eventRepository.markCompleted(eventId);
            reminderScheduler.cancelReminder(eventId);

            mainHandler.post(() -> {
                hideLoading();
                showToast("Đã hoàn thành sự kiện");
                loadEvent();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
