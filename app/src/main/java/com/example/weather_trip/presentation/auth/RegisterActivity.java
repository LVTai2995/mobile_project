package com.example.weather_trip.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.weather_trip.R;
import com.example.weather_trip.data.repository.UserRepository;
import com.example.weather_trip.domain.model.User;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.presentation.common.SessionManager;
import com.example.weather_trip.presentation.home.HomeActivity;

public class RegisterActivity extends BaseActivity {

    private EditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private View btnRegister;
    private TextView tvLogin;

    private UserRepository userRepository;
    private SessionManager sessionManager;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initRepositories();
        setupListeners();
    }

    private void initViews() {
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initRepositories() {
        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!validateInput(displayName, email, password, confirmPassword)) return;

        showLoading("Đang đăng ký...");

        new Thread(() -> {
            User user = userRepository.register(email, password, displayName);

            mainHandler.post(() -> {
                hideLoading();
                if (user != null) {
                    sessionManager.createSession(user.getId(), user.getEmail(), user.getDisplayName());
                    navigateToHome();
                } else {
                    showToast("Email đã được sử dụng");
                }
            });
        }).start();
    }

    private boolean validateInput(String displayName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
