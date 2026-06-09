package com.example.sambungayat;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvSurahInfo, tvNyawa, tvScore, tvComboStreak, tvSoalArab, tvHasilSusunan, tvArtiAyat, tvDetailAyat;
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
    private Set<Integer> failedQuestions = new HashSet<>();

    private MediaPlayer mediaPlayer, effectPlayer;
    private Typeface uthmaniFont;
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");

    private float qariVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private float playbackSpeed = 1.0f;
    private String currentPlayingUrl = "";

    // Simpan streak tertinggi yang dicapai user dalam satu sesi ini untuk dikirim ke server
    private int maxStreakInSession = 0;

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
        tvArtiAyat = findViewById(R.id.tvArtiAyat);
        tvDetailAyat = findViewById(R.id.tvDetailAyat);
        layoutPilihanKata = findViewById(R.id.layoutPilihanKata);
        btnPutarSuara = findViewById(R.id.btnPutarSuara);
        progressBar = findViewById(R.id.progressBar);
        dropZoneCard = findViewById(R.id.dropZoneCard);
        btnHint = findViewById(R.id.btnHint);

        if (tvHasilSusunan != null) tvHasilSusunan.setTypeface(uthmaniFont);
        if (tvSoalArab != null) tvSoalArab.setTypeface(uthmaniFont);

        ArrayList<Integer> selectedSurahIds = getIntent().getIntegerArrayListExtra("SELECTED_SURAH_IDS");
        int limit = getIntent().getIntExtra("LIMIT", 5);
        String surahName = getIntent().getStringExtra("SURAH_NAME");

        if (tvSurahInfo != null) tvSurahInfo.setText(surahName != null ? surahName : "Sambung Ayat");

        View btnMenyerah = findViewById(R.id.btnMenyerah);
        if (btnMenyerah != null) btnMenyerah.setOnClickListener(v -> finish());

        if (btnHint != null) btnHint.setOnClickListener(v -> showHintWarning());

        if (btnPutarSuara != null) {
            btnPutarSuara.setOnClickListener(v -> {
                if (!questionList.isEmpty() && currentQuestionIndex < questionList.size()) {
                    toggleAudio(questionList.get(currentQuestionIndex).audioUrl);
                }
            });
        }

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
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(qariVolume, qariVolume);
        }
    }

    private void showHintWarning() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Butuh Bantuan?")
                .setContentText("Nyawa kamu akan berkurang 1 untuk melihat potongan ayat ini. Lanjutkan?")
                .setConfirmText("Ya, Gunakan")
                .setCancelText("Batal")
                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                .setConfirmClickListener(sDialog -> {
                    if (nyawa >= 1) {
                        nyawa--;
                        if (tvNyawa != null) tvNyawa.setText("❤️ " + nyawa);

                        String hintAyat = questionList.get(currentQuestionIndex).jawaban;

                        sDialog.setTitleText("Bantuan Ayat")
                                .setContentText(hintAyat)
                                .setConfirmText("OK")
                                .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                                .showCancelButton(false)
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                                .changeAlertType(SweetAlertDialog.NORMAL_TYPE);

                        TextView contentText = sDialog.findViewById(cn.pedant.SweetAlert.R.id.content_text);
                        if (contentText != null) {
                            contentText.setTypeface(uthmaniFont);
                            contentText.setTextSize(22);
                        }
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
        if (dropZoneCard != null) {
            dropZoneCard.setOnDragListener((v, event) -> {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String dragData = item.getText().toString();
                    if (tvHasilSusunan != null) tvHasilSusunan.setText(dragData);
                    checkAnswer(dragData);
                }
                return true;
            });
        }
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
                String urlString = Config.URL_GET_SOAL + idParam + "&limit=" + limit;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject res = new JSONObject(sb.toString());
                    if (res.optString("status").equals("success")) {
                        JSONArray arr = res.getJSONArray("data");
                        questionList.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String audio = obj.optString("audio_url", "");
                            int sId = obj.optInt("surah_id", 1);
                            int vNum = obj.optInt("verse_number", 1);

                            if (!audio.isEmpty() && !audio.startsWith("http") && !audio.equals("null")) {
                                audio = Config.BASE_URL + "audio/" + audio;
                            }
                            if (audio.isEmpty() || audio.equals("null")) {
                                audio = String.format(Locale.US, "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3", sId, vNum);
                            }

                            questionList.add(new Question(
                                    obj.optString("soal", ""),
                                    obj.optString("jawaban_benar", ""),
                                    audio,
                                    obj.optString("text_indo", "Terjemahan tidak tersedia"),
                                    obj.optString("pilihan_1", ""),
                                    obj.optString("pilihan_2", ""),
                                    obj.optString("pilihan_3", ""),
                                    obj.optString("pilihan_4", ""),
                                    sId,
                                    vNum,
                                    obj.optString("nama_surat", "Surat")
                            ));
                        }
                        Collections.shuffle(questionList);
                        runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            displayQuestion();
                        });
                    } else {
                        throw new Exception(res.optString("message", "Gagal mengambil data"));
                    }
                } else {
                    throw new Exception("HTTP Error: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error loadQuestions: " + e.getMessage());
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat soal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            endSession(false);
            return;
        }
        Question q = questionList.get(currentQuestionIndex);
        if (tvSoalArab != null) tvSoalArab.setText(q.soal);

        if (tvArtiAyat != null) tvArtiAyat.setText("\"" + q.textIndo + "\"");

        if (tvDetailAyat != null) {
            tvDetailAyat.setText("Surah " + q.namaSurat + " • Ayat " + q.verseNumber);
        }

        if (tvHasilSusunan != null) {
            tvHasilSusunan.setText("");
            tvHasilSusunan.setHint("Tarik ayat lanjutan ke sini");
        }

        if (layoutPilihanKata != null) {
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
        }
        playAudio(q.audioUrl);
    }

    private void checkAnswer(String userAns) {
        if (currentQuestionIndex >= questionList.size()) return;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_play));
        }

        Question q = questionList.get(currentQuestionIndex);
        if (userAns.trim().equals(q.jawaban.trim())) {
            comboStreak++;

            if (comboStreak > maxStreakInSession) {
                maxStreakInSession = comboStreak;
            }

            int pointsEarned = (comboStreak >= 5) ? 20 : 10;
            score += pointsEarned;
            if (!failedQuestions.contains(currentQuestionIndex)) correctCount++;

            if (comboStreak % 5 == 0 && nyawa < 3) {
                nyawa++;
                Toast.makeText(this, "Combo " + comboStreak + "! Bonus +1 ❤️", Toast.LENGTH_SHORT).show();
            }
            if (tvScore != null) tvScore.setText("Skor: " + score);
            if (tvNyawa != null) tvNyawa.setText("❤️ " + nyawa);
            if (tvComboStreak != null) tvComboStreak.setText("Combo: " + comboStreak + (comboStreak >= 3 ? " 🔥" : ""));
            playEffect(R.raw.benar);

            if (comboStreak == 3 || comboStreak == 10 || comboStreak == 50 || comboStreak == 100) {
                showCustomAlert(true, "STREAK 🔥 " + comboStreak, "Luar biasa! " + comboStreak + " kali benar beruntun!", false);
            } else {
                showCustomAlert(true, "Masyaallah!", "Jawaban kamu benar.", false);
            }
        } else {
            failedQuestions.add(currentQuestionIndex);
            nyawa--;
            comboStreak = 0;
            if (tvNyawa != null) tvNyawa.setText("❤️ " + nyawa);
            if (tvComboStreak != null) tvComboStreak.setText("Combo: 0");
            playEffect(R.raw.salah);
            if (nyawa <= 0) endSession(true);
            else showCustomAlert(false, "Ayo Murajaah!", "Kurang tepat, ayo coba lagi.", false);
        }
    }

    private void updateScoreOnServer(int totalScore, int maxStreak) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_SUBMIT_SCORE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);

                // HEADER PENTING: Mendefinisikan format body request sebagai form data biasa agar terbaca oleh PHP $_POST
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "user_id=" + userId + "&score=" + totalScore + "&streak=" + maxStreak;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.flush(); os.close();

                // Memicu koneksi agar mengeksekusi request sepenuhnya
                int responseCode = conn.getResponseCode();
                Log.d("SERVER_SCORE", "Response Code: " + responseCode);
                conn.disconnect();
            } catch (Exception e) { Log.e("SERVER", "Gagal update skor: " + e.getMessage()); }
        });
    }

    private void updateProgressOnServer(int surahId, int progress) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(Config.URL_UPDATE_PROGRESS);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);

                // Menambahkan header form-urlencoded ke progres juga agar stabil
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "user_id=" + userId + "&surah_id=" + surahId + "&juz_id=" + juzId + "&progress=" + progress;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.flush(); os.close();
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) { Log.e("SERVER", "Gagal update progres: " + e.getMessage()); }
        });
    }

    private void endSession(boolean isGameOver) {
        int totalQuestions = Math.max(1, questionList.size());
        int finalProgress = (correctCount * 100) / totalQuestions;

        Set<Integer> uniqueSurahs = new HashSet<>();
        for (Question q : questionList) uniqueSurahs.add(q.surahId);
        for (Integer sId : uniqueSurahs) updateProgressOnServer(sId, finalProgress);

        // Kirim akumulasi total skor latihan dan streak tertinggi sekaligus ke server PHP saat sesi berakhir
        if (score > 0) {
            updateScoreOnServer(score, maxStreakInSession);
        }

        String title = isGameOver ? "Yaaah!" : "Selesai!";
        String message = isGameOver ? "Nyawa kamu habis!\nProgres: " + finalProgress + "%"
                : "Alhamdulillah! Kamu menyelesaikan muraajah ini.\nBenar: " + correctCount + "/" + totalQuestions + "\nProgres: " + finalProgress + "%";

        showCustomAlert(!isGameOver, title, message, true);
    }

    private void showCustomAlert(boolean isCorrect, String title, String message, boolean isFinish) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom_alert);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        TextView tvTitle = dialog.findViewById(R.id.dialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.dialogMessage);
        ImageView imgIcon = dialog.findViewById(R.id.dialogIcon);
        MaterialButton btn = dialog.findViewById(R.id.btnNext);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);

        if (imgIcon != null) {
            if (isFinish || title.contains("STREAK")) imgIcon.setImageResource(R.drawable.ic_star);
            else imgIcon.setImageResource(isCorrect ? R.drawable.ic_star : R.drawable.ic_close);
        }

        if (btn != null) {
            btn.setText(isFinish ? "SELESAI" : "LANJUT");
            btn.setOnClickListener(v -> {
                dialog.dismiss();
                if (isFinish) finish();
                else if (isCorrect) { currentQuestionIndex++; displayQuestion(); }
            });
        }
        dialog.show();
    }

    private void toggleAudio(String url) {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && url.equals(currentPlayingUrl)) {
            mediaPlayer.pause();
            runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_play));
        } else if (mediaPlayer != null && !mediaPlayer.isPlaying() && url.equals(currentPlayingUrl)) {
            mediaPlayer.start();
            runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_pause));
        } else {
            playAudio(url);
        }
    }

    private void playAudio(String url) {
        Log.d("AUDIO", "Playing URL: " + url);
        currentPlayingUrl = url;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());
                } else {
                    mediaPlayer.reset();
                }

                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Android)");

                mediaPlayer.setDataSource(this, android.net.Uri.parse(url), headers);
                mediaPlayer.setVolume(qariVolume, qariVolume);

                mediaPlayer.setOnPreparedListener(mp -> {
                    try {
                        PlaybackParams params = new PlaybackParams();
                        params.setSpeed(playbackSpeed);
                        mp.setPlaybackParams(params);
                        mp.start();
                        runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_pause));
                    } catch (Exception e) {
                        mp.start();
                        runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_pause));
                    }
                });

                mediaPlayer.setOnCompletionListener(mp -> {
                    runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_play));
                });

                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e("AUDIO", "MediaPlayer Error - What: " + what + ", Extra: " + extra);
                    runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_play));
                    return true;
                });

                mediaPlayer.prepareAsync();

            } catch (Exception e) {
                Log.e("AUDIO", "Gagal prepare audio: " + e.getMessage());
                runOnUiThread(() -> btnPutarSuara.setImageResource(android.R.drawable.ic_media_play));
            }
        });
    }

    private void playEffect(int resId) {
        try {
            if (effectPlayer != null) {
                effectPlayer.stop();
                effectPlayer.release();
            }
            effectPlayer = MediaPlayer.create(this, resId);
            if (effectPlayer != null) {
                effectPlayer.setVolume(sfxVolume, sfxVolume);
                effectPlayer.start();
            }
        } catch (Exception e) { Log.e("AUDIO", "Gagal putar efek: " + e.getMessage()); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (effectPlayer != null) {
            effectPlayer.release();
            effectPlayer = null;
        }
    }

    private static class Question {
        String soal, jawaban, audioUrl, textIndo, p1, p2, p3, p4, namaSurat;
        int surahId, verseNumber;
        public Question(String s, String j, String a, String t, String p1, String p2, String p3, String p4, int surahId, int verseNumber, String namaSurat) {
            this.soal = s; this.jawaban = j; this.audioUrl = a; this.textIndo = t;
            this.p1 = p1; this.p2 = p2; this.p3 = p3; this.p4 = p4;
            this.surahId = surahId; this.verseNumber = verseNumber; this.namaSurat = namaSurat;
        }
    }
}