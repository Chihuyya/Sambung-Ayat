package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDailyReminder, switchTipsRetention;
    private SharedPreferences sharedPreferences;

    private static final String SHARED_PREFS_NAME = "notification_settings";
    private static final String KEY_DAILY_REMINDER = "key_daily_reminder";
    private static final String KEY_TIPS_RETENTION = "key_tips_retention";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Inisialisasi Toolbar & Tombol Kembali
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inisialisasi View Sakelar (Switch)
        switchDailyReminder = findViewById(R.id.switchDailyReminder);
        switchTipsRetention = findViewById(R.id.switchTipsRetention);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        // Memuat status konfigurasi terakhir yang tersimpan (Default: true)
        loadSettings();

        // Listener Aksi untuk Sakelar Pengingat Harian
        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_DAILY_REMINDER, isChecked);
            if (isChecked) {
                Toast.makeText(this, "Pengingat Harian Diaktifkan", Toast.LENGTH_SHORT).show();
                // TODO: Jadwalkan AlarmManager / WorkManager untuk notifikasi harian
            } else {
                Toast.makeText(this, "Pengingat Harian Dinonaktifkan", Toast.LENGTH_SHORT).show();
                // TODO: Batalkan jadwal AlarmManager / WorkManager
            }
        });

        // Listener Aksi untuk Sakelar Tips & Progres
        switchTipsRetention.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_TIPS_RETENTION, isChecked);
            if (isChecked) {
                Toast.makeText(this, "Tips & Progres Hafalan Diaktifkan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tips & Progres Hafalan Dinonaktifkan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSettings() {
        boolean isDailyReminderEnabled = sharedPreferences.getBoolean(KEY_DAILY_REMINDER, true);
        boolean isTipsRetentionEnabled = sharedPreferences.getBoolean(KEY_TIPS_RETENTION, true);

        switchDailyReminder.setChecked(isDailyReminderEnabled);
        switchTipsRetention.setChecked(isTipsRetentionEnabled);
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}