package com.example.sambungayat;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNewPassword, etConfirmPassword;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnResetPassword = findViewById(R.id.btnResetPassword);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnResetPassword.setOnClickListener(v -> performResetPassword());
    }

    private void performResetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            showErrorDialog("Harap isi semua kolom");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showErrorDialog("Konfirmasi kata sandi tidak cocok");
            return;
        }

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(COLOR_PRIMARY);
        pDialog.setTitleText("Memproses...");
        pDialog.setCancelable(false);
        pDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_RESET_PASSWORD);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);

                String postData = "email=" + URLEncoder.encode(email, "UTF-8") +
                        "&new_password=" + URLEncoder.encode(newPassword, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    JSONObject res = new JSONObject(sb.toString());
                    runOnUiThread(() -> {
                        pDialog.dismissWithAnimation();
                        handleResetResponse(res);
                    });
                } else {
                    runOnUiThread(() -> {
                        pDialog.dismissWithAnimation();
                        showErrorDialog("Server error: " + responseCode);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pDialog.dismissWithAnimation();
                    showErrorDialog("Gagal terhubung ke server");
                });
            }
        });
    }

    private void handleResetResponse(JSONObject res) {
        try {
            if (res.getString("status").equals("success")) {
                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Berhasil!")
                        .setContentText("Kata sandi telah diperbarui. Silakan masuk kembali.")
                        .setConfirmText("OK")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            finish();
                        }).show();
            } else {
                showErrorDialog(res.getString("message"));
            }
        } catch (Exception e) {
            showErrorDialog("Terjadi kesalahan sistem");
        }
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Gagal")
                .setContentText(message)
                .setConfirmText("OK")
                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                .show();
    }
}
