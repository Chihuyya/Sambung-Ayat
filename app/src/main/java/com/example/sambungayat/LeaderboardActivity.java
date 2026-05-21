package com.example.sambungayat;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class LeaderboardActivity extends AppCompatActivity {

    private LinearLayout containerLeaderboard;
    private Button btnKembali;

    // Sesuaikan URL ini dengan nama folder API kamu di XAMPP
    private final String LEADERBOARD_URL = "http://10.0.2.2/API_sambung_ayat/get_leaderboard.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        containerLeaderboard = findViewById(R.id.containerLeaderboard);
        btnKembali = findViewById(R.id.btnKembali);

        // Aksi tombol kembali
        btnKembali.setOnClickListener(v -> finish());

        // Tarik data peringkat dari server XAMPP
        muatDataLeaderboard();
    }

    private void muatDataLeaderboard() {
        // Menggunakan Executor (Background Thread) agar aplikasi tidak lag/freeze saat memuat internet
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(LEADERBOARD_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(sb.toString());

                // Kembali ke Main Thread (UI) untuk merender tampilan
                runOnUiThread(() -> {
                    containerLeaderboard.removeAllViews();

                    if (jsonArray.length() == 0) {
                        TextView tvKosong = new TextView(LeaderboardActivity.this);
                        tvKosong.setText("Belum ada skor yang tersimpan. Jadilah yang pertama!");
                        tvKosong.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                        tvKosong.setTextSize(16);
                        containerLeaderboard.addView(tvKosong);
                        return;
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String nama = obj.getString("nama_pemain");
                            int skor = obj.getInt("skor");
                            String waktu = obj.getString("waktu_main");

                            // Membuat kotak kartu peringkat secara otomatis dari Java
                            LinearLayout card = new LinearLayout(LeaderboardActivity.this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundColor(Color.WHITE);
                            card.setPadding(30, 20, 30, 20);

                            // Mengatur margin antar kartu
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(0, 0, 0, 15);
                            card.setLayoutParams(params);

                            // Isi teks kartu (Peringkat, Nama, dan Skor)
                            TextView tvInfo = new TextView(LeaderboardActivity.this);
                            String medali = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : "🔸";
                            tvInfo.setText(medali + " Peringkat " + (i + 1) + "\n" + nama);
                            tvInfo.setTextSize(18);
                            tvInfo.setTextColor(Color.parseColor("#37474F"));
                            tvInfo.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

                            TextView tvScore = new TextView(LeaderboardActivity.this);
                            tvScore.setText("Skor: " + skor + " Poin");
                            tvScore.setTextSize(16);
                            tvScore.setTextColor(Color.parseColor("#4CAF50"));
                            tvScore.setPadding(0, 5, 0, 0);

                            // Masukkan teks ke dalam kartu, lalu kartu ke dalam container utama
                            card.addView(tvInfo);
                            card.addView(tvScore);
                            containerLeaderboard.addView(card);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LeaderboardActivity.this, "Gagal mengambil data Leaderboard dari XAMPP!", Toast.LENGTH_LONG).show());
            }
        });
    }
}