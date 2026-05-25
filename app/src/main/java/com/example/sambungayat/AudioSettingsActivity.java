package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

public class AudioSettingsActivity extends AppCompatActivity {

    private Slider sliderQariVolume, sliderSfxVolume;
    private TextView tvQariVolume, tvSfxVolume;
    private MaterialButtonToggleGroup toggleGroupSpeed;
    private MaterialButton btnSaveSettings;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "audio_settings";
    private static final String KEY_QARI_VOL = "qari_volume";
    private static final String KEY_SFX_VOL = "sfx_volume";
    private static final String KEY_SPEED = "playback_speed";

    // Variabel untuk menyimpan nilai sementara sebelum tombol "Simpan" ditekan
    private float currentQariVol = 100f;
    private float currentSfxVol = 75f;
    private float currentSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_settings);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inisialisasi View
        sliderQariVolume = findViewById(R.id.sliderQariVolume);
        sliderSfxVolume = findViewById(R.id.sliderSfxVolume);
        tvQariVolume = findViewById(R.id.tvQariVolume);
        tvSfxVolume = findViewById(R.id.tvSfxVolume);
        toggleGroupSpeed = findViewById(R.id.toggleGroupSpeed);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        // Load pengaturan yang tersimpan
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadSavedSettings();

        // Listener: Slider Volume Qari
        sliderQariVolume.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                currentQariVol = value;
                tvQariVolume.setText((int) value + "%");
            }
        });

        // Listener: Slider Volume SFX
        sliderSfxVolume.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                currentSfxVol = value;
                tvSfxVolume.setText((int) value + "%");
            }
        });

        // Listener: Grup Tombol Kecepatan
        toggleGroupSpeed.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnSpeed05) currentSpeed = 0.5f;
                else if (checkedId == R.id.btnSpeed075) currentSpeed = 0.75f;
                else if (checkedId == R.id.btnSpeed10) currentSpeed = 1.0f;
                else if (checkedId == R.id.btnSpeed125) currentSpeed = 1.25f;
                else if (checkedId == R.id.btnSpeed15) currentSpeed = 1.5f;
            }
        });

        // Listener: Tombol Simpan
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSavedSettings() {
        currentQariVol = sharedPreferences.getFloat(KEY_QARI_VOL, 100f);
        currentSfxVol = sharedPreferences.getFloat(KEY_SFX_VOL, 75f);
        currentSpeed = sharedPreferences.getFloat(KEY_SPEED, 1.0f);

        // Terapkan ke View
        sliderQariVolume.setValue(currentQariVol);
        tvQariVolume.setText((int) currentQariVol + "%");

        sliderSfxVolume.setValue(currentSfxVol);
        tvSfxVolume.setText((int) currentSfxVol + "%");

        if (currentSpeed == 0.5f) toggleGroupSpeed.check(R.id.btnSpeed05);
        else if (currentSpeed == 0.75f) toggleGroupSpeed.check(R.id.btnSpeed075);
        else if (currentSpeed == 1.25f) toggleGroupSpeed.check(R.id.btnSpeed125);
        else if (currentSpeed == 1.5f) toggleGroupSpeed.check(R.id.btnSpeed15);
        else toggleGroupSpeed.check(R.id.btnSpeed10); // Default 1.0x
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_QARI_VOL, currentQariVol);
        editor.putFloat(KEY_SFX_VOL, currentSfxVol);
        editor.putFloat(KEY_SPEED, currentSpeed);
        editor.apply();

        Toast.makeText(this, "Pengaturan Audio Berhasil Disimpan", Toast.LENGTH_SHORT).show();
        finish(); // Menutup halaman dan kembali ke menu sebelumnya
    }
}