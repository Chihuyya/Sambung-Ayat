package com.example.sambungayat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay 2.5 detik lalu pindah ke Menu Utama
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // FIX: Pastikan memanggil MenuActivity.class (Abaikan error jika belum buat MenuActivity)
                Intent intent = new Intent(SplashActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2500);
    }
}