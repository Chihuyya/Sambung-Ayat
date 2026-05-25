package com.example.sambungayat;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvSurahInfo, tvScore, tvNyawa, tvHasilSusunan;
    private ImageButton btnPutarSuara;
    private LinearLayout layoutPilihanKata;
    private Button btnHint, btnMenyerah;

    private int currentScore = 0;
    private int nyawa = 3;
    private int surahId;
    private String surahName;
    private int currentIndex = 0;
    private int userId;
    private String username;

    private MediaPlayer mediaPlayer;
    private List<QuizModel> quizList = new ArrayList<>();
    private ArrayList<String> listPilihanUser = new ArrayList<>();

    public static class QuizModel {
        int verseNumber;
        String textIndo;
        String audioUrl;
        List<String> kataAcak;
        List<String> kataBenar;

        public QuizModel(int verseNumber, String textIndo, String audioUrl, List<String> kataAcak, List<String> kataBenar) {
            this.verseNumber = verseNumber;
            this.textIndo = textIndo;
            this.audioUrl = audioUrl;
            this.kataAcak = kataAcak;
            this.kataBenar = kataBenar;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        username = sharedPref.getString("USERNAME", "Pemain");

        tvSurahInfo = findViewById(R.id.tvSurahInfo);
        tvScore = findViewById(R.id.tvScore);
        tvNyawa = findViewById(R.id.tvNyawa);
        tvHasilSusunan = findViewById(R.id.tvHasilSusunan);
        btnPutarSuara = findViewById(R.id.btnPutarSuara);
        layoutPilihanKata = findViewById(R.id.layoutPilihanKata);
        btnHint = findViewById(R.id.btnHint);
        btnMenyerah = findViewById(R.id.btnMenyerah);

        surahId = getIntent().getIntExtra("SURAH_ID", 1);
        surahName = getIntent().getStringExtra("SURAH_NAME");

        tvSurahInfo.setText("Surah: " + surahName + " (Memuat...)");
        loadQuizData(surahId);

        btnMenyerah.setOnClickListener(v -> akhiriPermainan());
    }

    private void loadQuizData(int id) {
        quizList.clear();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_GET_AYAT + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject response = new JSONObject(sb.toString());
                if (response.getString("status").equals("success")) {
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        JSONArray wordsArr = obj.getJSONArray("kata_acak");
                        
                        List<String> kataAcak = new ArrayList<>();
                        String[] benarArr = new String[wordsArr.length()];
                        
                        for (int j = 0; j < wordsArr.length(); j++) {
                            JSONObject w = wordsArr.getJSONObject(j);
                            String txt = w.getString("word_text");
                            int order = w.getInt("word_order") - 1;
                            kataAcak.add(txt);
                            if (order >= 0 && order < benarArr.length) benarArr[order] = txt;
                        }

                        List<String> kataBenar = new ArrayList<>();
                        for (String s : benarArr) if (s != null) kataBenar.add(s);

                        quizList.add(new QuizModel(
                            obj.getInt("verse_number"),
                            obj.getString("text_indo"),
                            obj.getString("audio_url"),
                            kataAcak,
                            kataBenar
                        ));
                    }
                }

                runOnUiThread(() -> {
                    if (quizList.isEmpty()) {
                        Toast.makeText(this, "Data ayat tidak ditemukan!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        displayQuestion();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error koneksi database!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void displayQuestion() {
        if (currentIndex >= quizList.size() || nyawa <= 0) {
            akhiriPermainan();
            return;
        }

        QuizModel q = quizList.get(currentIndex);
        tvSurahInfo.setText(surahName + " : Ayat " + q.verseNumber);
        tvScore.setText("Skor: " + currentScore);
        tvNyawa.setText("❤️ Nyawa: " + nyawa);
        tvHasilSusunan.setText("");
        listPilihanUser.clear();
        layoutPilihanKata.removeAllViews();

        btnPutarSuara.setOnClickListener(v -> putarAudio(q.audioUrl));
        btnHint.setOnClickListener(v -> Toast.makeText(this, "Hint: " + q.kataBenar.get(0), Toast.LENGTH_SHORT).show());

        for (String kata : q.kataAcak) {
            Button btn = new Button(this);
            btn.setText(kata);
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> {
                listPilihanUser.add(kata);
                StringBuilder sb = new StringBuilder();
                for(String s : listPilihanUser) sb.append(s).append(" ");
                tvHasilSusunan.setText(sb.toString().trim());
                btn.setEnabled(false);
                if (listPilihanUser.size() == q.kataBenar.size()) cekJawaban();
            });
            layoutPilihanKata.addView(btn);
        }
    }

    private void cekJawaban() {
        QuizModel q = quizList.get(currentIndex);
        boolean benar = true;
        for (int i = 0; i < q.kataBenar.size(); i++) {
            if (!listPilihanUser.get(i).equals(q.kataBenar.get(i))) {
                benar = false;
                break;
            }
        }

        if (benar) {
            currentScore += 10;
            currentIndex++;
            Toast.makeText(this, "Benar!", Toast.LENGTH_SHORT).show();
            displayQuestion();
        } else {
            nyawa--;
            Toast.makeText(this, "Salah, coba lagi!", Toast.LENGTH_SHORT).show();
            if (nyawa > 0) displayQuestion();
            else akhiriPermainan();
        }
    }

    private void putarAudio(String url) {
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            
            // Ambil Pengaturan Audio
            SharedPreferences audioPrefs = getSharedPreferences("audio_settings", Context.MODE_PRIVATE);
            float volume = audioPrefs.getFloat("qari_volume", 100f) / 100f;
            float speed = audioPrefs.getFloat("playback_speed", 1.0f);

            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PlaybackParams params = mp.getPlaybackParams();
                    params.setSpeed(speed);
                    mp.setPlaybackParams(params);
                }
                mp.start();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memutar audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void akhiriPermainan() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_SUBMIT_SCORE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "nama_pemain=" + URLEncoder.encode(username, "UTF-8") +
                        "&skor=" + currentScore +
                        "&user_id=" + userId;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject res = new JSONObject(sb.toString());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Permainan Selesai! Skor Anda: " + currentScore, Toast.LENGTH_LONG).show();
                    updateSurahProgress();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::finish);
            }
        });
    }

    private void updateSurahProgress() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_UPDATE_PROGRESS);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "user_id=" + userId + "&surah_id=" + surahId;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();
                conn.getInputStream();
                runOnUiThread(this::finish);
            } catch (Exception e) {
                runOnUiThread(this::finish);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}