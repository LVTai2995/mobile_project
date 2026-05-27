package com.example.weather_trip.presentation.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather_trip.BuildConfig;
import com.example.weather_trip.R;
import com.example.weather_trip.data.remote.PlacesClient;
import com.example.weather_trip.data.remote.model.GeocodingResponse;
import com.example.weather_trip.data.repository.EventRepository;
import com.example.weather_trip.data.repository.WeatherRepository;
import com.example.weather_trip.domain.model.Event;
import com.example.weather_trip.domain.model.Weather;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.presentation.common.SessionManager;
import com.example.weather_trip.presentation.event.adapter.PlaceSuggestionAdapter;
import com.example.weather_trip.service.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEventActivity extends BaseActivity {

    private ImageButton btnBack;
    private MaterialButton btnSave;
    private EditText etTitle, etDescription, etLocation;
    private TextView etDate, etTime;
    private LinearLayout etDateContainer, etTimeContainer;
    private Spinner spinnerReminder;
    private MaterialCardView weatherCard;
    private TextView tvWeatherIcon, tvWeatherTemp, tvWeatherDesc, tvWeatherHumidity, tvWeatherAdvice;
    private View weatherLoading;

    private SessionManager sessionManager;
    private EventRepository eventRepository;
    private WeatherRepository weatherRepository;
    private ReminderScheduler reminderScheduler;
    private Handler mainHandler;
    private ExecutorService executor;
    private PlaceSuggestionAdapter suggestionAdapter;
    private RecyclerView rvSuggestions;

    private long eventId;
    private Event currentEvent;
    private Calendar selectedDate = Calendar.getInstance();
    private Weather currentWeather;
    private Runnable searchRunnable;
    private boolean isLocationValidated = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

    private final String[] reminderOptions = {"15 phút", "30 phút", "1 giờ", "2 giờ", "1 ngày"};
    private final int[] reminderMinutes = {15, 30, 60, 120, 1440};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            showToast("Không tìm thấy sự kiện");
            finish();
            return;
        }

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(this);
        eventRepository = new EventRepository(this);
        weatherRepository = new WeatherRepository(this);
        reminderScheduler = new ReminderScheduler(this);

        initViews();
        setupListeners();
        setupReminderSpinner();
        loadEvent();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDateContainer = findViewById(R.id.etDateContainer);
        etTimeContainer = findViewById(R.id.etTimeContainer);
        spinnerReminder = findViewById(R.id.spinnerReminder);
        weatherCard = findViewById(R.id.weatherCard);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvWeatherHumidity = findViewById(R.id.tvWeatherHumidity);
        tvWeatherAdvice = findViewById(R.id.tvWeatherAdvice);
        weatherLoading = findViewById(R.id.weatherLoading);
        rvSuggestions = findViewById(R.id.rvSuggestions);

        suggestionAdapter = new PlaceSuggestionAdapter();
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);
        suggestionAdapter.setOnSuggestionClickListener(this::onSuggestionClick);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isLocationValidated = false;
                btnSave.setEnabled(false);
                if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);
                if (s.length() >= 3) {
                    searchRunnable = () -> searchPlaces(s.toString());
                    mainHandler.postDelayed(searchRunnable, 300);
                } else {
                    suggestionAdapter.clear();
                    rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        etDateContainer.setOnClickListener(v -> showDatePicker());
        etTimeContainer.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> updateEvent());
    }

    private void setupReminderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, reminderOptions);
        spinnerReminder.setAdapter(adapter);
    }

    private void loadEvent() {
        showLoading("Đang tải...");

        executor.execute(() -> {
            currentEvent = eventRepository.getEventById(eventId);

            mainHandler.post(() -> {
                hideLoading();
                if (currentEvent == null) {
                    showToast("Không tìm thấy sự kiện");
                    finish();
                    return;
                }
                populateFields();
            });
        });
    }

    private void populateFields() {
        etTitle.setText(currentEvent.getTitle());
        etDescription.setText(currentEvent.getDescription());
        etLocation.setText(currentEvent.getLocationName());

        // Parse eventDate as yyyyMMdd integer and set calendar
        String dateStr = String.format(Locale.US, "%08d", currentEvent.getEventDate());
        try {
            Date eventDate = parseDateFormat.parse(dateStr);
            selectedDate.setTime(eventDate);
        } catch (ParseException e) {
            // Fallback to current date
        }
        etDate.setText(dateFormat.format(selectedDate.getTime()));

        if (currentEvent.getEventTime() != null) {
            etTime.setText(currentEvent.getEventTime());
            String[] parts = currentEvent.getEventTime().split(":");
            if (parts.length == 2) {
                selectedDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                selectedDate.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            }
        }

        for (int i = 0; i < reminderMinutes.length; i++) {
            if (reminderMinutes[i] == currentEvent.getReminderMinutes()) {
                spinnerReminder.setSelection(i);
                break;
            }
        }

        if (currentEvent.getWeatherId() > 0) {
            currentWeather = new Weather();
            currentWeather.setWeatherId(currentEvent.getWeatherId());
            currentWeather.setMain(currentEvent.getWeatherMain());
            currentWeather.setDescription(currentEvent.getWeatherDescription());
            currentWeather.setTemp(currentEvent.getTemperature());
            currentWeather.setTempMin(currentEvent.getTemperatureMin());
            currentWeather.setTempMax(currentEvent.getTemperatureMax());
            currentWeather.setHumidity(currentEvent.getHumidity());
            currentWeather.setCityName(currentEvent.getWeatherCityName());
            displayWeather(currentWeather);
        }

        btnSave.setEnabled(true);
        isLocationValidated = true;
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            etDate.setText(dateFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDate.set(Calendar.MINUTE, minute);
            etTime.setText(timeFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.MINUTE), true).show();
    }

    private void searchPlaces(String query) {
        PlacesClient.getGeocodingService().searchPlaces(query, 5, BuildConfig.WEATHER_API_KEY)
                .enqueue(new Callback<List<GeocodingResponse.GeocodingResult>>() {
                    @Override
                    public void onResponse(Call<List<GeocodingResponse.GeocodingResult>> call, Response<List<GeocodingResponse.GeocodingResult>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GeocodingResponse.GeocodingResult> results = response.body();
                            if (results != null && !results.isEmpty()) {
                                List<PlaceSuggestionAdapter.PlaceSuggestion> suggestions = new java.util.ArrayList<>();
                                for (GeocodingResponse.GeocodingResult r : results) {
                                    suggestions.add(new PlaceSuggestionAdapter.PlaceSuggestion(
                                            r.getName(), r.getCity(), r.getCountry()));
                                }
                                mainHandler.post(() -> {
                                    suggestionAdapter.setSuggestions(suggestions);
                                    rvSuggestions.setVisibility(View.VISIBLE);
                                });
                            } else {
                                mainHandler.post(() -> {
                                    suggestionAdapter.clear();
                                    rvSuggestions.setVisibility(View.GONE);
                                    Toast.makeText(EditEventActivity.this,
                                            "Không tìm thấy địa điểm này", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GeocodingResponse.GeocodingResult>> call, Throwable t) {
                        mainHandler.post(() -> {
                            suggestionAdapter.clear();
                            rvSuggestions.setVisibility(View.GONE);
                            Toast.makeText(EditEventActivity.this,
                                    "Lỗi tìm kiếm địa điểm", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void onSuggestionClick(String placeName) {
        etLocation.setText(placeName);
        isLocationValidated = true;
        suggestionAdapter.clear();
        rvSuggestions.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        fetchWeather(placeName);
    }

    private void fetchWeather(String city) {
        weatherLoading.setVisibility(View.VISIBLE);
        weatherCard.setVisibility(View.GONE);

        weatherRepository.getWeatherByCity(city, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(Weather weather) {
                mainHandler.post(() -> {
                    weatherLoading.setVisibility(View.GONE);
                    currentWeather = weather;
                    displayWeather(weather);
                    isLocationValidated = true;
                    btnSave.setEnabled(true);
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    weatherLoading.setVisibility(View.GONE);
                    isLocationValidated = false;
                    btnSave.setEnabled(false);
                });
            }
        });
    }

    private void displayWeather(Weather weather) {
        weatherCard.setVisibility(View.VISIBLE);
        tvWeatherIcon.setText(weather.getEmoji());
        tvWeatherTemp.setText(String.format("%.0f°C - %.0f°C", weather.getTempMin(), weather.getTempMax()));
        tvWeatherDesc.setText(weather.getVietnamese());
        tvWeatherHumidity.setText("Độ ẩm: " + weather.getHumidity() + "%");

        int weatherId = weather.getWeatherId();
        if (weatherId >= 200 && weatherId < 600) {
            tvWeatherAdvice.setVisibility(View.VISIBLE);
            tvWeatherAdvice.setText("⚠️ Có thể mưa!");
        } else if (weatherId == 800) {
            tvWeatherAdvice.setVisibility(View.VISIBLE);
            tvWeatherAdvice.setText("🌟 Thời tiết lý tưởng!");
        } else {
            tvWeatherAdvice.setVisibility(View.GONE);
        }
    }

    private void updateEvent() {
        String title = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            etLocation.setError("Vui lòng nhập địa điểm");
            etLocation.requestFocus();
            return;
        }
        if (!isLocationValidated) {
            etLocation.setError("Vui lòng chọn địa điểm từ gợi ý");
            etLocation.requestFocus();
            return;
        }

        showLoading("Đang lưu...");

        executor.execute(() -> {
            currentEvent.setTitle(title);
            currentEvent.setDescription(etDescription.getText().toString().trim());
            currentEvent.setLocationName(location);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            currentEvent.setEventDate(Integer.parseInt(sdf.format(selectedDate.getTime())));
            currentEvent.setEventTime(etTime.getText().toString());
            currentEvent.setEventDatetime(selectedDate.getTimeInMillis());

            int selectedIndex = spinnerReminder.getSelectedItemPosition();
            currentEvent.setReminderMinutes(reminderMinutes[selectedIndex]);

            if (currentWeather != null) {
                currentEvent.setWeatherId(currentWeather.getWeatherId());
                currentEvent.setWeatherMain(currentWeather.getMain());
                currentEvent.setWeatherDescription(currentWeather.getDescription());
                currentEvent.setWeatherIcon(currentWeather.getIcon());
                currentEvent.setTemperature(currentWeather.getTemp());
                currentEvent.setTemperatureMin(currentWeather.getTempMin());
                currentEvent.setTemperatureMax(currentWeather.getTempMax());
                currentEvent.setHumidity(currentWeather.getHumidity());
                currentEvent.setWindSpeed(currentWeather.getWindSpeed());
                currentEvent.setClouds(currentWeather.getClouds());
                currentEvent.setWeatherCityName(currentWeather.getCityName());
                currentEvent.setWeatherTimestamp(currentWeather.getTimestamp());
            }

            eventRepository.updateEvent(currentEvent);
            reminderScheduler.cancelReminder(eventId);
            reminderScheduler.scheduleReminder(eventId, currentEvent.getEventDatetime(), currentEvent.getReminderMinutes(), title);

            mainHandler.post(() -> {
                hideLoading();
                showToast("Đã cập nhật sự kiện");
                setResult(RESULT_OK);
                finish();
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
