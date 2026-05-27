package com.example.weather_trip.service;

import android.app.Activity;
import android.content.Intent;

import com.example.weather_trip.BuildConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleSignInHelper {

    private GoogleSignInClient signInClient;
    private GoogleSignInCallback callback;

    public interface GoogleSignInCallback {
        void onSuccess(String idToken, String email, String name);
        void onError(String message);
    }

    public GoogleSignInHelper(Activity activity, GoogleSignInCallback callback) {
        this.callback = callback;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        this.signInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signIn(Activity activity, int requestCode) {
        Intent signInIntent = signInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, requestCode);
    }

    public void handleResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken();
                String email = account.getEmail();
                String name = account.getDisplayName();
                callback.onSuccess(idToken, email, name);
            }
        } catch (ApiException e) {
            callback.onError("Google Sign-In failed: " + e.getStatusCode());
        }
    }
}
