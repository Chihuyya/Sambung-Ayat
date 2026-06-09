package com.example.sambungayat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> performRegistration());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorDialog("Harap lengkapi seluruh kolom.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Konfirmasi sandi tidak cocok.");
            return;
        }

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(COLOR_PRIMARY);
        pDialog.setTitleText("Mendaftarkan Akun...").show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_REGISTER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000); // Diperpanjang ke 15 detik
                conn.setReadTimeout(15000);

                String postData = "username=" + URLEncoder.encode(username, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    pDialog.dismissWithAnimation();
                    try {
                        if (res.getString("status").equals("success")) {
                            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Berhasil!")
                                    .setContentText("Silakan login dengan akun baru Anda.")
                                    .setConfirmText("OK")
                                    .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                                    .setConfirmClickListener(sDialog -> {
                                        sDialog.dismissWithAnimation();
                                        finish();
                                    }).show();
                        } else {
                            showErrorDialog(res.getString("message"));
                        }
                    } catch (Exception e) { showErrorDialog("Format respon salah"); }
                });
            } catch (Exception e) {
                runOnUiThread(() -> { pDialog.dismissWithAnimation(); showErrorDialog("Koneksi server terputus."); });
            }
        });
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Gagal")
                .setContentText(message)
                .setConfirmText("Tutup")
                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                .show();
    }
}
