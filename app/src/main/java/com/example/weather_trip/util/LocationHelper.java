package com.example.weather_trip.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void getCurrentLocation(LocationResultCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Không có quyền truy cập vị trí");
            return;
        }

        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // Timeout fallback
        final android.os.Handler handler = mainHandler;
        Runnable timeoutRunnable = () -> {
            android.util.Log.d("LocationHelper", "getLastLocation timeout, requesting fresh location");
            requestFreshLocation(callback);
        };
        handler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    handler.removeCallbacks(timeoutRunnable);
                    if (location != null) {
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        requestFreshLocation(callback);
                    }
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacks(timeoutRunnable);
                    requestFreshLocation(callback);
                });
    }

    private static final long LOCATION_TIMEOUT_MS = 10000;

    private void requestFreshLocation(LocationResultCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed("Không có quyền truy cập vị trí");
            return;
        }

        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // Timeout fallback
        Runnable timeoutRunnable = () -> {
            fusedLocationClient.removeLocationUpdates(receiver);
            callback.onLocationFailed("Hết thời gian lấy vị trí");
        };
        mainHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT_MS);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        receiver = new LocationReceiver(callback, mainHandler, timeoutRunnable);
        fusedLocationClient.requestLocationUpdates(locationRequest, receiver, Looper.getMainLooper());
    }

    private LocationReceiver receiver;

    public interface LocationResultCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationFailed(String error);
    }

    private static class LocationReceiver extends LocationCallback {
        private final LocationResultCallback callback;
        private final android.os.Handler handler;
        private final Runnable timeoutRunnable;
        private boolean hasResponded = false;

        LocationReceiver(LocationResultCallback callback, android.os.Handler handler, Runnable timeoutRunnable) {
            this.callback = callback;
            this.handler = handler;
            this.timeoutRunnable = timeoutRunnable;
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (hasResponded) return;
            hasResponded = true;
            handler.removeCallbacks(timeoutRunnable);

            Location location = locationResult.getLastLocation();
            if (location != null) {
                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
            } else {
                callback.onLocationFailed("Không thể lấy vị trí");
            }
        }
    }
}
