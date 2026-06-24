package com.example.weather_trip.presentation.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weather_trip.R;
import com.example.weather_trip.presentation.auth.LoginActivity;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.presentation.common.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private SwitchMaterial switchReminder;
    private SwitchMaterial switchWeather;
    private MaterialButton btnLogout;
    private LinearLayout profileItem;
    private ImageButton btnBack;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        switchReminder = findViewById(R.id.switchReminder);
        switchWeather = findViewById(R.id.switchWeather);
        btnLogout = findViewById(R.id.btnLogout);
        profileItem = findViewById(R.id.profileItem);
    }

    private void loadUserData() {
        tvUserName.setText(sessionManager.getUserName());
        tvUserEmail.setText(sessionManager.getUserEmail());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        profileItem.setOnClickListener(v -> {
            showToast("Chức năng đang phát triển");
        });

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("Đã bật nhắc nhở sự kiện");
            } else {
                showToast("Đã tắt nhắc nhở sự kiện");
            }
        });

        switchWeather.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showToast("Đã bật thông báo thời tiết");
            } else {
                showToast("Đã tắt thông báo thời tiết");
            }
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logout();
                    navigateToLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
