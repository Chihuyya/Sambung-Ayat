package com.example.sambungayat;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.flexbox.FlexboxLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvSurahInfo, tvNyawa, tvScore, tvComboStreak, tvSoalArab, tvHasilSusunan;
    private FlexboxLayout layoutPilihanKata;
    private ImageButton btnPutarSuara;
    private View progressBar, dropZoneCard;
    private MaterialButton btnHint;
    
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int nyawa = 3;
    private int comboStreak = 0;
    private int correctCount = 0;
    private int userId;
    private int juzId = 1; 
    
    private MediaPlayer mediaPlayer, effectPlayer;
    private Typeface uthmaniFont;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    private float qariVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private float playbackSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try { uthmaniFont = ResourcesCompat.getFont(this, R.font.uthmani); } 
        catch (Exception e) { uthmaniFont = Typeface.DEFAULT; }

        SharedPreferences sharedPref = getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("USER_ID", 0);
        juzId = getIntent().getIntExtra("JUZ_ID", 1);

        tvSurahInfo = findViewById(R.id.tvSurahInfo);
        tvNyawa = findViewById(R.id.tvNyawa);
        tvScore = findViewById(R.id.tvScore);
        tvComboStreak = findViewById(R.id.tvComboStreak);
        tvSoalArab = findViewById(R.id.tvSoalArab);
        tvHasilSusunan = findViewById(R.id.tvHasilSusunan);
        layoutPilihanKata = findViewById(R.id.layoutPilihanKata);
        btnPutarSuara = findViewById(R.id.btnPutarSuara);
        progressBar = findViewById(R.id.progressBar);
        dropZoneCard = findViewById(R.id.dropZoneCard);
        btnHint = findViewById(R.id.btnHint);

        tvHasilSusunan.setTypeface(uthmaniFont);
        tvSoalArab.setTypeface(uthmaniFont);

        ArrayList<Integer> selectedSurahIds = getIntent().getIntegerArrayListExtra("SELECTED_SURAH_IDS");
        int limit = getIntent().getIntExtra("LIMIT", 5);
        String surahName = getIntent().getStringExtra("SURAH_NAME");

        tvSurahInfo.setText(surahName != null ? surahName : "Sambung Ayat");
        findViewById(R.id.btnMenyerah).setOnClickListener(v -> finish());
        
        btnHint.setOnClickListener(v -> showHintWarning());

        btnPutarSuara.setOnClickListener(v -> {
            if (!questionList.isEmpty() && currentQuestionIndex < questionList.size()) {
                playAudio(questionList.get(currentQuestionIndex).audioUrl);
            }
        });
        
        setupDropZone();
        loadQuestions(selectedSurahIds, limit);
        loadAudioSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAudioSettings();
    }

    private void loadAudioSettings() {
        SharedPreferences audioPref = getSharedPreferences("audio_settings", Context.MODE_PRIVATE);
        qariVolume = audioPref.getFloat("qari_volume", 100f) / 100f;
        sfxVolume = audioPref.getFloat("sfx_volume", 75f) / 100f;
        playbackSpeed = audioPref.getFloat("playback_speed", 1.0f);
    }

    private void showHintWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Butuh Bantuan?")
                .setContentText("Nyawa kamu akan berkurang 1 untuk melihat arti ayat ini. Lanjutkan?")
                .setConfirmText("Ya, Gunakan")
                .setCancelText("Batal")
                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                .setConfirmClickListener(sDialog -> {
                    if (nyawa > 1) {
                        nyawa--;
                        tvNyawa.setText("❤️ " + nyawa);
                        sDialog.setTitleText("Bantuan Arti")
                               .setContentText(questionList.get(currentQuestionIndex).textIndo)
                               .setConfirmText("OK")
                               .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                               .showCancelButton(false)
                               .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                               .changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                    } else {
                        sDialog.setTitleText("Gagal")
                               .setContentText("Nyawa kamu tidak cukup!")
                               .setConfirmText("OK")
                               .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                               .showCancelButton(false)
                               .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    }
                }).show();
    }

    private void setupDropZone() {
        dropZoneCard.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                ClipData.Item item = event.getClipData().getItemAt(0);
                String dragData = item.getText().toString();
                tvHasilSusunan.setText(dragData);
                checkAnswer(dragData);
            }
            return true;
        });
    }

    private void loadQuestions(List<Integer> surahIds, int limit) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                StringBuilder sbIds = new StringBuilder();
                if (surahIds != null) {
                    for (int i = 0; i < surahIds.size(); i++) {
                        sbIds.append(surahIds.get(i));
                        if (i < surahIds.size() - 1) sbIds.append(",");
                    }
                }
                String idParam = sbIds.length() > 0 ? sbIds.toString() : "1";

                URL url = new URL(Config.URL_GET_SOAL + idParam + "&limit=" + limit);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                
                JSONObject res = new JSONObject(sb.toString());
                if (res.getString("status").equals("success")) {
                    JSONArray arr = res.getJSONArray("data");
                    questionList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        
                        String audio = obj.optString("audio_url", "");
                        int sId = obj.optInt("surah_id", 1);
                        int vNum = obj.optInt("verse_number", 1);
                        
                        if (audio.isEmpty() || audio.equals("null")) {
                            audio = String.format(Locale.US, "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", sId, vNum);
                        }

                        questionList.add(new Question(
                            obj.getString("soal"), obj.getString("jawaban_benar"), audio,
                            obj.optString("text_indo", "Terjemahan tidak tersedia"),
                            obj.getString("pilihan_1"), obj.getString("pilihan_2"), obj.getString("pilihan_3"), obj.getString("pilihan_4"),
                            sId
                        ));
                    }
                    
                    Collections.shuffle(questionList);
                    
                    runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        displayQuestion();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat soal", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            finishSesi();
            return;
        }
        Question q = questionList.get(currentQuestionIndex);
        tvSoalArab.setText(q.soal);
        tvHasilSusunan.setText("");
        tvHasilSusunan.setHint("Tarik ayat lanjutan ke sini");
        layoutPilihanKata.removeAllViews();
        List<String> options = new ArrayList<>();
        options.add(q.p1); options.add(q.p2); options.add(q.p3); options.add(q.p4);
        Collections.shuffle(options);
        for (String teks : options) {
            TextView tv = new TextView(this);
            tv.setText(teks);
            tv.setTextSize(20);
            tv.setTypeface(uthmaniFont);
            tv.setTextDirection(View.TEXT_DIRECTION_RTL);
            tv.setTextColor(Color.parseColor("#5D4037"));
            tv.setPadding(30, 25, 30, 25);
            tv.setBackgroundResource(R.drawable.bg_rounded_number);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 15, 0, 15);
            tv.setLayoutParams(lp);
            tv.setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("answer", teks);
                v.startDragAndDrop(data, new View.DragShadowBuilder(v), v, 0);
                return true;
            });
            layoutPilihanKata.addView(tv);
        }
        playAudio(q.audioUrl);
    }

    private void checkAnswer(String userAns) {
        Question q = questionList.get(currentQuestionIndex);
        if (userAns.trim().equals(q.jawaban.trim())) {
            comboStreak++;
            int pointsEarned = (comboStreak >= 5) ? 20 : 10;
            score += pointsEarned;
            correctCount++;
            
            if (comboStreak % 5 == 0 && nyawa < 3) {
                nyawa++;
                Toast.makeText(this, "Combo " + comboStreak + "! Bonus +1 ❤️", Toast.LENGTH_SHORT).show();
            }

            tvScore.setText("Skor: " + score);
            tvNyawa.setText("❤️ " + nyawa);
            tvComboStreak.setText("Combo: " + comboStreak + (comboStreak >= 3 ? " 🔥" : ""));
            
            playEffect(R.raw.benar);
            updateScoreOnServer(pointsEarned);

            if (comboStreak == 3 || comboStreak == 10 || comboStreak == 50 || comboStreak == 100) {
                showCustomAlert(true, "STREAK 🔥 " + comboStreak, "Luar biasa! " + comboStreak + " kali benar beruntun!", false);
            } else {
                showCustomAlert(true, "Maa Syaa Allah!", "Jawaban kamu benar.", false);
            }
            
        } else {
            nyawa--;
            comboStreak = 0;
            tvNyawa.setText("❤️ " + nyawa);
            tvComboStreak.setText("Combo: 0");
            
            playEffect(R.raw.salah);
            if (nyawa <= 0) showCustomAlert(false, "Yaaah!", "Nyawa kamu habis!", true);
            else showCustomAlert(false, "Ayo Murajaah!", "Kurang tepat, ayo coba lagi.", false);
        }
    }

    private void updateScoreOnServer(int points) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_SUBMIT_SCORE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "user_id=" + userId + "&score=" + points + "&streak=" + comboStreak;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();
                conn.getInputStream();
            } catch (Exception e) { Log.e("SERVER", "Gagal update skor"); }
        });
    }

    private void updateProgressOnServer(int surahId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_UPDATE_PROGRESS);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "user_id=" + userId + "&surah_id=" + surahId + "&juz_id=" + juzId;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush(); os.close();
                conn.getInputStream();
            } catch (Exception e) { Log.e("SERVER", "Gagal update progres"); }
        });
    }

    private void finishSesi() {
        int totalQuestions = questionList.size() > 0 ? questionList.size() : 1;
        int finalProgress = (correctCount * 100) / totalQuestions;
        
        // Simpan progres ke database HANYA JIKA sesi selesai (Bukan Game Over)
        if (nyawa > 0) {
            Set<Integer> uniqueSurahs = new HashSet<>();
            for (Question q : questionList) {
                uniqueSurahs.add(q.surahId);
            }
            for (Integer sId : uniqueSurahs) {
                updateProgressOnServer(sId);
            }
        }

        String message = "Alhamdulillah! Kamu menyelesaikan muraajah ini.\nBenar: " + correctCount + "/" + questionList.size() + "\nProgres: " + finalProgress + "%";
        showCustomAlert(true, "Selesai!", message, true);
    }

    private void showCustomAlert(boolean isCorrect, String title, String message, boolean isFinish) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom_alert);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        
        TextView tvTitle = dialog.findViewById(R.id.dialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.dialogMessage);
        ImageView imgIcon = dialog.findViewById(R.id.dialogIcon);
        MaterialButton btn = dialog.findViewById(R.id.btnNext);

        tvTitle.setText(title);
        tvMessage.setText(message);
        
        if (isFinish || title.contains("STREAK")) {
            imgIcon.setImageResource(R.drawable.ic_star);
        } else {
            imgIcon.setImageResource(isCorrect ? R.drawable.ic_star : R.drawable.ic_close);
        }

        btn.setText(isFinish ? "SELESAI" : "LANJUT");
        
        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (isFinish) finish();
            else if (isCorrect) { 
                currentQuestionIndex++; 
                displayQuestion(); 
            }
        });
        dialog.show();
    }

    private void playAudio(String url) {
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setVolume(qariVolume, qariVolume);
            mediaPlayer.prepare();
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(playbackSpeed);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayer.start();
        } catch (Exception e) { Log.e("AUDIO", "Gagal putar audio ayat: " + url); }
    }

    private void playEffect(int resId) {
        try {
            if (effectPlayer != null) effectPlayer.release();
            effectPlayer = MediaPlayer.create(this, resId);
            if (effectPlayer != null) {
                effectPlayer.setVolume(sfxVolume, sfxVolume);
                effectPlayer.start();
            }
        } catch (Exception e) { Log.e("AUDIO", "Gagal putar efek"); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
        if (effectPlayer != null) effectPlayer.release();
    }

    private static class Question {
        String soal, jawaban, audioUrl, textIndo, p1, p2, p3, p4;
        int surahId;
        public Question(String s, String j, String a, String t, String p1, String p2, String p3, String p4, int surahId) {
            this.soal = s; this.jawaban = j; this.audioUrl = a; this.textIndo = t;
            this.p1 = p1; this.p2 = p2; this.p3 = p3; this.p4 = p4;
            this.surahId = surahId;
        }
    }
}