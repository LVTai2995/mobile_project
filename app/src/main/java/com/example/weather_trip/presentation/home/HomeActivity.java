package com.example.weather_trip.presentation.home;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weather_trip.R;
import com.example.weather_trip.data.repository.EventRepository;
import com.example.weather_trip.data.repository.WeatherRepository;
import com.example.weather_trip.domain.model.Event;
import com.example.weather_trip.domain.model.Weather;
import com.example.weather_trip.presentation.auth.LoginActivity;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.presentation.common.SessionManager;
import com.example.weather_trip.presentation.common.WeatherUIHelper;
import com.example.weather_trip.presentation.event.CreateEventActivity;
import com.example.weather_trip.presentation.event.EventDetailActivity;
import com.example.weather_trip.presentation.home.adapter.EventAdapter;
import com.example.weather_trip.presentation.settings.SettingsActivity;
import com.example.weather_trip.util.LocationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private LinearLayout skeletonView;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNav;
    private ImageButton btnAccount;

    // Weather header views
    private View weatherBanner;
    private View weatherContainer;
    private TextView tvCity;
    private TextView tvCurrentDate;
    private TextView tvTemperature;
    private TextView tvWeatherIcon;
    private TextView tvWeatherDesc;
    private TextView tvFeelsLike;
    private TextView tvHumidity;
    private TextView tvGreeting;
    private View weatherLoading;
    private View appBar;

    // Section title
    private TextView tvSectionTitle;
    private TextView tvEventCount;

    private SessionManager sessionManager;
    private EventRepository eventRepository;
    private WeatherRepository weatherRepository;
    private EventAdapter adapter;
    private ExecutorService executor;
    private Handler mainHandler;
    private LocationHelper locationHelper;
    private Runnable weatherTimeoutRunnable;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    loadWeatherWithLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    loadWeatherWithLocation();
                } else {
                    cancelWeatherTimeout();
                    loadWeatherWithDefaultCity();
                    Toast.makeText(this, "Không có quyền vị trí, hiển thị thời tiết mặc định", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        eventRepository = new EventRepository(this);
        weatherRepository = new WeatherRepository(this);
        locationHelper = new LocationHelper(this);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupListeners();
        updateCurrentDate();
        loadCurrentWeather();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void initViews() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        skeletonView = findViewById(R.id.skeletonView);
        fab = findViewById(R.id.fab);
        bottomNav = findViewById(R.id.bottomNav);
        btnAccount = findViewById(R.id.btnAccount);

        // Weather header views
        weatherContainer = findViewById(R.id.weatherContainer);
        weatherBanner = findViewById(R.id.weatherBanner);
        tvCity = findViewById(R.id.tvCity);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvGreeting = findViewById(R.id.tvGreeting);
        weatherLoading = findViewById(R.id.weatherLoading);
        appBar = findViewById(R.id.appBar);

        // Section title
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEventCount = findViewById(R.id.tvEventCount);
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter();
        adapter.setOnEventClickListener(this::onEventClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadCurrentWeather();
            loadEvents();
        });

        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateEventActivity.class));
        });

        btnAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void updateCurrentDate() {
        Date now = new Date();
        tvCurrentDate.setText(dateFormat.format(now));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng!";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều!";
        } else {
            greeting = "Chào buổi tối!";
        }
        tvGreeting.setText(greeting);
    }

    private void loadCurrentWeather() {
        android.util.Log.d("HomeActivity", "loadCurrentWeather: starting");
        
        if (weatherBanner == null || weatherLoading == null) {
            android.util.Log.e("HomeActivity", "loadCurrentWeather: views are NULL! weatherBanner=" + weatherBanner + ", weatherLoading=" + weatherLoading);
            return;
        }
        
        weatherBanner.setVisibility(View.GONE);
        weatherLoading.setVisibility(View.VISIBLE);
        android.util.Log.d("HomeActivity", "loadCurrentWeather: showing loading, hiding banner");

        // Timeout handler - if weather doesn't load in 10 seconds, show error
        weatherTimeoutRunnable = () -> {
            mainHandler.post(() -> {
                android.util.Log.d("HomeActivity", "loadCurrentWeather: TIMEOUT triggered");
                if (weatherLoading != null && weatherLoading.getVisibility() == View.VISIBLE) {
                    weatherLoading.setVisibility(View.GONE);
                    if (weatherBanner != null) weatherBanner.setVisibility(View.VISIBLE);
                    showWeatherError();
                    Toast.makeText(HomeActivity.this, "Không thể cập nhật thời tiết. Vui lòng kiểm tra kết nối mạng.", Toast.LENGTH_SHORT).show();
                }
            });
        };
        mainHandler.postDelayed(weatherTimeoutRunnable, 10000);
        android.util.Log.d("HomeActivity", "loadCurrentWeather: timeout scheduled");

        if (hasLocationPermission()) {
            android.util.Log.d("HomeActivity", "loadCurrentWeather: has permission, loading with location");
            loadWeatherWithLocation();
        } else {
            android.util.Log.d("HomeActivity", "loadCurrentWeather: no permission, requesting");
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void cancelWeatherTimeout() {
        if (weatherTimeoutRunnable != null) {
            mainHandler.removeCallbacks(weatherTimeoutRunnable);
            weatherTimeoutRunnable = null;
        }
    }

    private void loadWeatherWithLocation() {
        android.util.Log.d("HomeActivity", "loadWeatherWithLocation: getting location");
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                android.util.Log.d("HomeActivity", "loadWeatherWithLocation: got location lat=" + latitude + ", lon=" + longitude);
                weatherRepository.getWeatherByCoords(latitude, longitude, new WeatherRepository.WeatherCallback() {
                    @Override
                    public void onSuccess(Weather weather) {
                        android.util.Log.d("HomeActivity", "loadWeatherWithLocation: API success, weather=" + (weather != null ? weather.getTemp() + "C" : "null"));
                        mainHandler.post(() -> {
                            cancelWeatherTimeout();
                            weatherLoading.setVisibility(View.GONE);
                            weatherBanner.setVisibility(View.VISIBLE);
                            displayWeather(weather);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        android.util.Log.e("HomeActivity", "loadWeatherWithLocation: API error=" + message);
                        mainHandler.post(() -> {
                            cancelWeatherTimeout();
                            weatherLoading.setVisibility(View.GONE);
                            weatherBanner.setVisibility(View.VISIBLE);
                            showWeatherError();
                        });
                    }
                });
            }

            @Override
            public void onLocationFailed(String error) {
                android.util.Log.e("HomeActivity", "loadWeatherWithLocation: location failed=" + error);
                mainHandler.post(() -> {
                    cancelWeatherTimeout();
                    Toast.makeText(HomeActivity.this, "Không lấy được vị trí: " + error, Toast.LENGTH_SHORT).show();
                    loadWeatherWithDefaultCity();
                });
            }
        });
    }

    private void loadWeatherWithDefaultCity() {
        String defaultCity = "Ho Chi Minh";
        android.util.Log.d("HomeActivity", "loadWeatherWithDefaultCity: fetching weather for " + defaultCity);
        weatherRepository.getWeatherByCity(defaultCity, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(Weather weather) {
                android.util.Log.d("HomeActivity", "loadWeatherWithDefaultCity: API success, weather=" + (weather != null ? weather.getTemp() + "C" : "null"));
                mainHandler.post(() -> {
                    cancelWeatherTimeout();
                    weatherLoading.setVisibility(View.GONE);
                    weatherBanner.setVisibility(View.VISIBLE);
                    displayWeather(weather);
                });
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("HomeActivity", "loadWeatherWithDefaultCity: API error=" + message);
                mainHandler.post(() -> {
                    cancelWeatherTimeout();
                    weatherLoading.setVisibility(View.GONE);
                    weatherBanner.setVisibility(View.VISIBLE);
                    showWeatherError();
                });
            }
        });
    }

    private void showWeatherError() {
        tvTemperature.setText("--");
        tvWeatherIcon.setText("🌤️");
        tvWeatherDesc.setText("Không thể cập nhật");
        tvFeelsLike.setText("Cảm giác --°C");
        tvHumidity.setText("Độ ẩm --%");
    }

    private void displayWeather(Weather weather) {
        if (weather != null) {
            // Set temperature
            tvTemperature.setText(String.format("%.0f", weather.getTemp()));

            // Set weather icon and description
            tvWeatherIcon.setText(weather.getEmoji());
            tvWeatherDesc.setText(weather.getVietnamese());

            // Set feels like
            tvFeelsLike.setText(String.format("Cảm giác %.0f°C", weather.getFeelsLike()));

            // Set humidity
            tvHumidity.setText(String.format("Độ ẩm %d%%", weather.getHumidity()));

            // Set city name
            if (weather.getCityName() != null) {
                tvCity.setText(weather.getCityName());
            }

            // Apply weather gradient background
            applyWeatherBackground(weather.getMain());
        }
    }

    private void applyWeatherBackground(String weatherMain) {
        int backgroundRes;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (weatherMain != null) {
            switch (weatherMain.toLowerCase()) {
                case "clear":
                case "sunny":
                    backgroundRes = (hour >= 6 && hour < 18) ?
                            R.drawable.bg_weather_sunny : R.drawable.bg_weather_night;
                    break;
                case "clouds":
                    backgroundRes = R.drawable.bg_weather_cloudy;
                    break;
                case "rain":
                case "drizzle":
                    backgroundRes = R.drawable.bg_weather_rainy;
                    break;
                case "thunderstorm":
                    backgroundRes = R.drawable.bg_weather_stormy;
                    break;
                case "snow":
                    backgroundRes = R.drawable.bg_weather_snowy;
                    break;
                default:
                    backgroundRes = R.drawable.bg_weather_sunny;
            }
        } else {
            backgroundRes = R.drawable.bg_weather_sunny;
        }

        weatherBanner.setBackgroundResource(backgroundRes);
    }

    private void loadEvents() {
        showSkeletonLoading();

        executor.execute(() -> {
            long userId = sessionManager.getUserId();
            List<Event> events = eventRepository.getEventsByUserId(userId);

            long now = System.currentTimeMillis();
            for (Event event : events) {
                if (event.getEventDatetime() != null && !event.isCompleted() && event.getEventDatetime() <= now) {
                    eventRepository.markCompleted(event.getId());
                    event.setCompleted(true);
                }
            }

            mainHandler.post(() -> {
                hideSkeletonLoading();
                swipeRefresh.setRefreshing(false);

                // Update event count
                tvEventCount.setText(events.size() + " sự kiện");

                if (events.isEmpty()) {
                    showEmptyState();
                } else {
                    showEventsList(events);
                }
            });
        });
    }

    private void showSkeletonLoading() {
        skeletonView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        startSkeletonAnimation();
    }

    private void hideSkeletonLoading() {
        skeletonView.setVisibility(View.GONE);
        stopSkeletonAnimation();
    }

    private void startSkeletonAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(skeletonView, "alpha", 1f, 0.5f, 1f);
        animator.setDuration(1500);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
        skeletonView.setTag(animator);
    }

    private void stopSkeletonAnimation() {
        if (skeletonView.getTag() instanceof ObjectAnimator) {
            ((ObjectAnimator) skeletonView.getTag()).cancel();
        }
    }

    private void showEmptyState() {
        skeletonView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void showEventsList(List<Event> events) {
        skeletonView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.setEvents(events);
    }

    private void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelWeatherTimeout();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
