package com.example.sambungayat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDailyReminder, switchTipsRetention;
    private SharedPreferences sharedPreferences;

    private static final String SHARED_PREFS_NAME = "notification_settings";
    private static final String KEY_DAILY_REMINDER = "key_daily_reminder";
    private static final String KEY_TIPS_RETENTION = "key_tips_retention";
    private static final int NOTIFICATION_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.notification_settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        switchDailyReminder = findViewById(R.id.switchDailyReminder);
        switchTipsRetention = findViewById(R.id.switchTipsRetention);

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        loadSettings();

        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_DAILY_REMINDER, isChecked);
            if (isChecked) {
                if (checkNotificationPermission()) {
                    scheduleDailyReminder();
                    Toast.makeText(this, "Pengingat Harian Diaktifkan (19:00)", Toast.LENGTH_SHORT).show();
                } else {
                    switchDailyReminder.setChecked(false);
                    saveSetting(KEY_DAILY_REMINDER, false);
                }
            } else {
                cancelDailyReminder();
                Toast.makeText(this, "Pengingat Harian Dinonaktifkan", Toast.LENGTH_SHORT).show();
            }
        });

        switchTipsRetention.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_TIPS_RETENTION, isChecked);
            String status = isChecked ? "Diaktifkan" : "Dinonaktifkan";
            Toast.makeText(this, "Tips & Hafalan " + status, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission for notifications
            // Simple check, in a real app you'd request it properly.
            return true; 
        }
        return true;
    }

    private void scheduleDailyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Atur waktu jam 19:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Jika waktu sudah terlewat hari ini, jadwalkan untuk besok
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void cancelDailyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
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
