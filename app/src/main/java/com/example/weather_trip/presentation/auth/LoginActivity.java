package com.example.weather_trip.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.weather_trip.R;
import com.example.weather_trip.data.repository.UserRepository;
import com.example.weather_trip.domain.model.User;
import com.example.weather_trip.presentation.common.BaseActivity;
import com.example.weather_trip.presentation.common.SessionManager;
import com.example.weather_trip.presentation.home.HomeActivity;
import com.example.weather_trip.service.GoogleOAuthService;
import com.example.weather_trip.service.GoogleSignInHelper;

public class LoginActivity extends BaseActivity {

    private static final int RC_GOOGLE_SIGN_IN = 1001;

    private EditText etEmail, etPassword;
    private TextView tvRegister;
    private View btnLogin, btnGoogle;

    private UserRepository userRepository;
    private SessionManager sessionManager;
    private GoogleSignInHelper googleHelper;
    private GoogleOAuthService oauthService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        mainHandler = new Handler(Looper.getMainLooper());
        initViews();
        initRepositories();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void initRepositories() {
        userRepository = new UserRepository(this);
        oauthService = new GoogleOAuthService();
        googleHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.GoogleSignInCallback() {
            @Override
            public void onSuccess(String idToken, String email, String name) {
                processGoogleToken(idToken, email, name);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void setupListeners() {
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> attemptLogin());

        btnGoogle.setOnClickListener(v -> {
            googleHelper.signIn(this, RC_GOOGLE_SIGN_IN);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!validateInput(email, password)) return;

        showLoading("Đang đăng nhập...");

        new Thread(() -> {
            User user = userRepository.login(email, password);

            mainHandler.post(() -> {
                hideLoading();
                if (user != null) {
                    sessionManager.createSession(user.getId(), user.getEmail(), user.getDisplayName());
                    navigateToHome();
                } else {
                    showToast("Email hoặc mật khẩu không đúng");
                }
            });
        }).start();
    }

    private boolean validateInput(String email, String password) {
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
        return true;
    }

    private void processGoogleToken(String idToken, String email, String name) {
        showLoading("Đang xử lý...");

        oauthService.verifyToken(idToken, new GoogleOAuthService.GoogleCallback() {
            @Override
            public void onSuccess(GoogleOAuthService.GoogleUserInfo info) {
                mainHandler.post(() -> {
                    hideLoading();
                    if (info != null && info.sub != null) {
                        new Thread(() -> {
                            User user = userRepository.loginWithGoogle(info.sub, 
                                    info.email != null ? info.email : email, 
                                    info.name != null ? info.name : name);
                            mainHandler.post(() -> {
                                if (user != null) {
                                    sessionManager.createSession(user.getId(), user.getEmail(), user.getDisplayName());
                                    navigateToHome();
                                } else {
                                    showToast("Đăng nhập Google thất bại");
                                }
                            });
                        }).start();
                    } else {
                        showToast("Không thể xác thực Google token");
                    }
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    hideLoading();
                    showToast(message);
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN && data != null) {
            googleHelper.handleResult(data);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
