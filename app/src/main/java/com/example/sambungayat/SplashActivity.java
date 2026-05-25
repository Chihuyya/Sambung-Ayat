package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
            int userId = sharedPref.getInt("USER_ID", 0);

            Intent intent;
            if (userId != 0) {
                // Jika sudah login, ke Dashboard
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                // Jika belum, ke Login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500);
    }
}