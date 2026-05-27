package com.example.weather_trip.presentation.common;

import com.example.weather_trip.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private View loadingView;
    private ViewGroup container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        container = null;
    }

    private void ensureLoadingView() {
        if (loadingView == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            loadingView = inflater.inflate(R.layout.progress_dialog, null);
            loadingView.setVisibility(View.GONE);

            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            View firstChild = decorView.getChildAt(0);
            if (firstChild instanceof ViewGroup) {
                container = (ViewGroup) firstChild;
            } else {
                container = decorView;
            }
            container.addView(loadingView);
        }
    }

    protected void showLoading(String message) {
        ensureLoadingView();
        TextView progressText = loadingView.findViewById(R.id.progressText);
        progressText.setText(message != null ? message : "Đang tải...");
        loadingView.setVisibility(View.VISIBLE);
        loadingView.bringToFront();
    }

    protected void hideLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (loadingView != null && container != null) {
            container.removeView(loadingView);
        }
        super.onDestroy();
    }
}
