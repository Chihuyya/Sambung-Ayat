package com.example.sambungayat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Komponen UI
    private TextView tvSurahInfo, tvScore, tvNyawa, tvHasilSusunan;
    private ImageButton btnPutarSuara;
    private LinearLayout layoutPilihanKata;
    private Button btnHint, btnMenyerah;

    // Logika Game
    private int currentScore = 0;
    private int nyawa = 3;
    private int surahId;
    private String surahName;
    private int currentIndex = 0;

    private MediaPlayer mediaPlayer;
    private List<QuizModel> quizList = new ArrayList<>();

    // Logika Susun Ayat
    private String[] potonganKataBenar;
    private ArrayList<String> listPilihanUser = new ArrayList<>();

    // Endpoint API XAMPP sesuai nama folder kamu
    private final String GET_AYAT_URL = "http://10.0.2.2/API_sambung_ayat/get_ayat.php?surah_id=";
    private final String SUBMIT_SCORE_URL = "http://10.0.2.2/API_sambung_ayat/submit_score.php";

    // Model Data yang presisi dengan tabel 'verses' databasemu
    public static class QuizModel {
        int verseNumber;
        String textArabFull;
        String audioUrl;

        public QuizModel(int verseNumber, String textArabFull, String audioUrl) {
            this.verseNumber = verseNumber;
            this.textArabFull = textArabFull;
            this.audioUrl = audioUrl;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi UI
        tvSurahInfo = findViewById(R.id.tvSurahInfo);
        tvScore = findViewById(R.id.tvScore);
        tvNyawa = findViewById(R.id.tvNyawa);
        tvHasilSusunan = findViewById(R.id.tvHasilSusunan);
        btnPutarSuara = findViewById(R.id.btnPutarSuara);
        layoutPilihanKata = findViewById(R.id.layoutPilihanKata);
        btnHint = findViewById(R.id.btnHint);
        btnMenyerah = findViewById(R.id.btnMenyerah);

        // Menerima data kiriman dari MenuActivity
        surahId = getIntent().getIntExtra("SURAH_ID", 1);
        surahName = getIntent().getStringExtra("SURAH_NAME");

        tvSurahInfo.setText("Surah: " + surahName + " (Memuat Ayat...)");

        // Ambil data ayat dari database secara real-time
        loadQuizDataFromXampp(surahId);

        btnMenyerah.setOnClickListener(v -> {
            if (!quizList.isEmpty() && currentIndex < quizList.size()) {
                QuizModel currentQuiz = quizList.get(currentIndex);
                Toast.makeText(MainActivity.this, "Kamu Menyerah! Jawabannya: " + currentQuiz.textArabFull, Toast.LENGTH_LONG).show();
            }
            akhiriPermainan();
        });
    }

    private void loadQuizDataFromXampp(int id) {
        quizList.clear();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(GET_AYAT_URL + id);
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int verseNumber = obj.getInt("verse_number");
                    String textArabFull = obj.getString("text_arab_full"); // Presisi dengan nama kolom di SQL
                    String audioUrl = obj.getString("audio_url");           // Presisi dengan nama kolom di SQL

                    quizList.add(new QuizModel(verseNumber, textArabFull, audioUrl));
                }

                runOnUiThread(() -> {
                    if (quizList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Surah ini belum memiliki data ayat di database!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        displayQuestion();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Gagal terhubung ke database server!", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void displayQuestion() {
        if (currentIndex >= quizList.size() || nyawa <= 0) {
            akhiriPermainan();
            return;
        }

        tvScore.setText("Skor: " + currentScore);
        tvNyawa.setText("❤️ Nyawa: " + nyawa);

        QuizModel currentQuiz = quizList.get(currentIndex);
        tvSurahInfo.setText(surahName + " : Ayat " + currentQuiz.verseNumber);

        // Reset komponen susunan kata
        tvHasilSusunan.setText("");
        listPilihanUser.clear();
        layoutPilihanKata.removeAllViews();

        // Putar audio ayat saat tombol diklik
        btnPutarSuara.setOnClickListener(v -> putarAudioAyat(currentQuiz.audioUrl));

        // Membagi kalimat utuh menjadi potongan kata berdasarkan spasi secara presisi
        potonganKataBenar = currentQuiz.textArabFull.trim().split("\\s+");

        btnHint.setOnClickListener(v -> {
            if (potonganKataBenar.length > 0) {
                Toast.makeText(MainActivity.this, "💡 Petunjuk: Kata pertama adalah '" + potonganKataBenar[0] + "'", Toast.LENGTH_LONG).show();
            }
        });

        // Acak urutan potongan kata untuk dijadikan tombol permainan
        ArrayList<String> listKataAcak = new ArrayList<>();
        Collections.addAll(listKataAcak, potonganKataBenar);
        Collections.shuffle(listKataAcak);

        // Tampilkan tombol kata acak secara dinamis
        for (String kata : listKataAcak) {
            Button btnKata = new Button(this);
            btnKata.setText(kata);
            btnKata.setAllCaps(false);
            btnKata.setTextSize(18);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 5, 10, 5);
            btnKata.setLayoutParams(params);

            btnKata.setOnClickListener(v -> {
                listPilihanUser.add(kata);
                updateKolomSusunanLayar();
                btnKata.setEnabled(false); // Kunci tombol agar tidak diklik dua kali

                // Jika kata yang disusun sudah lengkap, lakukan verifikasi jawaban
                if (listPilihanUser.size() == potonganKataBenar.length) {
                    cekHasilSusunanUser();
                }
            });
            layoutPilihanKata.addView(btnKata);
        }
    }

    private void updateKolomSusunanLayar() {
        StringBuilder sb = new StringBuilder();
        for (String k : listPilihanUser) {
            sb.append(k).append(" ");
        }
        tvHasilSusunan.setText(sb.toString().trim());
    }

    private void cekHasilSusunanUser() {
        boolean isBenar = true;
        for (int i = 0; i < potonganKataBenar.length; i++) {
            if (!listPilihanUser.get(i).equals(potonganKataBenar[i])) {
                isBenar = false;
                break;
            }
        }

        if (isBenar) {
            currentScore += 10;
            Toast.makeText(this, "🎉 Hebat, Susunan Ayat Benar!", Toast.LENGTH_SHORT).show();
            currentIndex++;
            displayQuestion();
        } else {
            nyawa--;
            Toast.makeText(this, "❌ Susunan Salah! Coba lagi.", Toast.LENGTH_SHORT).show();
            if (nyawa <= 0) {
                akhiriPermainan();
            } else {
                displayQuestion(); // Reset ulang susunan ayat ini agar user bisa mencoba lagi
            }
        }
    }

    private void putarAudioAyat(String url) {
        if (url == null || url.isEmpty() || url.equals("null")) {
            Toast.makeText(this, "Tautan audio tidak tersedia untuk ayat ini", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal memutar audio, periksa koneksi internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void akhiriPermainan() {
        Toast.makeText(this, "Permainan Selesai! Mengirim Skor...", Toast.LENGTH_LONG).show();
        // Kirim skor akhir secara background ke tabel leaderboard database XAMPP
        submitSkorKeDatabase("Pemain_Anonim", currentScore);
    }

    private void submitSkorKeDatabase(String nama, int skor) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(SUBMIT_SCORE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "nama_pemain=" + URLEncoder.encode(nama, "UTF-8") +
                        "&skor=" + skor;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(MainActivity.this, "Skor Akhir " + skor + " Berhasil Disimpan ke Leaderboard!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Gagal menyimpan skor ke server.", Toast.LENGTH_SHORT).show();
                    }
                    finish(); // Keluar kembali ke menu
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> finish());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}