package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        if (sharedPref.getInt("USER_ID", 0) != 0) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> performLogin());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) syncAccountWithBackend(account);
            } catch (ApiException e) {
                showErrorDialog("Gagal masuk dengan Google: " + e.getStatusCode());
            }
        }
    }

    private void syncAccountWithBackend(GoogleSignInAccount acct) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(COLOR_PRIMARY);
        pDialog.setTitleText("Sinkronisasi Akun...");
        pDialog.setCancelable(false);
        pDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_LOGIN_GOOGLE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setDoOutput(true);

                String postData = "email=" + URLEncoder.encode(acct.getEmail() != null ? acct.getEmail() : "", "UTF-8") +
                        "&username=" + URLEncoder.encode(acct.getDisplayName() != null ? acct.getDisplayName() : "", "UTF-8") +
                        "&google_id=" + URLEncoder.encode(acct.getId() != null ? acct.getId() : "", "UTF-8") +
                        "&photo_url=" + URLEncoder.encode(acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : "", "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    JSONObject res = new JSONObject(sb.toString());
                    runOnUiThread(() -> { pDialog.dismissWithAnimation(); handleLoginResponse(res); });
                } else {
                    runOnUiThread(() -> { pDialog.dismissWithAnimation(); showErrorDialog("Server error: " + responseCode); });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { pDialog.dismissWithAnimation(); showErrorDialog("Koneksi gagal. Periksa IP Server Anda."); });
            }
        });
    }

    private void performLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showErrorDialog("Harap isi username dan password");
            return;
        }

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(COLOR_PRIMARY);
        pDialog.setTitleText("Sedang Masuk...");
        pDialog.setCancelable(false);
        pDialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_LOGIN);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setDoOutput(true);

                String postData = "username=" + URLEncoder.encode(username, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    JSONObject res = new JSONObject(sb.toString());
                    runOnUiThread(() -> { pDialog.dismissWithAnimation(); handleLoginResponse(res); });
                } else {
                    runOnUiThread(() -> { pDialog.dismissWithAnimation(); showErrorDialog("Server error: " + responseCode); });
                }
            } catch (Exception e) {
                runOnUiThread(() -> { pDialog.dismissWithAnimation(); showErrorDialog("Gagal terhubung ke server. Periksa koneksi WiFi dan IP Laptop."); });
            }
        });
    }

    private void handleLoginResponse(JSONObject res) {
        try {
            if (res.getString("status").equals("success")) {
                SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("USER_ID", res.getInt("user_id"));
                editor.putString("USERNAME", res.getString("username"));
                editor.putString("EMAIL", res.optString("email", ""));
                editor.putString("PHOTO_URL", res.optString("photo_url", ""));
                editor.apply();

                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Berhasil!")
                        .setContentText("Selamat Datang Kembali!")
                        .setConfirmText("Masuk Beranda")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        }).show();
            } else {
                showErrorDialog(res.getString("message"));
            }
        } catch (Exception e) { showErrorDialog("Terjadi kesalahan sistem"); }
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