package com.example.sambungayat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class PlayQuizActivity extends AppCompatActivity {

    private MaterialCardView cardOption1, cardOption2;
    private View dotOption1, dotOption2;
    private View radioContainer1, radioContainer2;
    private LinearLayout layoutFeedbackBadge;
    private MaterialButton btnSubmitAnswer;
    private LinearProgressIndicator quizProgressBar;

    // Menyimpan indeks pilihan saat ini (-1 berarti belum memilih)
    private int selectedOptionIndex = -1;
    private boolean isCheckedState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_quiz);

        // Binding Views
        cardOption1 = findViewById(R.id.cardOption1);
        cardOption2 = findViewById(R.id.cardOption2);
        dotOption1 = findViewById(R.id.dotOption1);
        dotOption2 = findViewById(R.id.dotOption2);
        radioContainer1 = findViewById(R.id.radioContainer1);
        radioContainer2 = findViewById(R.id.radioContainer2);
        layoutFeedbackBadge = findViewById(R.id.layoutFeedbackBadge);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        quizProgressBar = findViewById(R.id.quizProgressBar);

        findViewById(R.id.btnExitQuiz).setOnClickListener(v -> finish());

        // Aksi Klik Opsi Kartu Pertama
        cardOption1.setOnClickListener(v -> selectOption(1));

        // Aksi Klik Opsi Kartu Kedua
        cardOption2.setOnClickListener(v -> selectOption(2));

        // Aksi Klik Tombol Verifikasi/Lanjut
        btnSubmitAnswer.setOnClickListener(v -> {
            if (!isCheckedState) {
                // Tahap 1: Verifikasi Jawaban (Sesuai dengan logic JavaScript 'submitBtn')
                isCheckedState = true;
                layoutFeedbackBadge.setVisibility(View.VISIBLE);
                quizProgressBar.setProgress(75); // Perbarui bar kuis menjadi 75%
                btnSubmitAnswer.setText("Lanjutkan");
            } else {
                // Tahap 2: Pindah ke Pertanyaan Berikutnya
                Toast.makeText(this, "Memuat soal berikutnya...", Toast.LENGTH_SHORT).show();
                resetQuizState();
            }
        });
    }

    private void selectOption(int optionIndex) {
        if (isCheckedState) return; // Kunci seleksi jika jawaban sudah diperiksa

        selectedOptionIndex = optionIndex;
        btnSubmitAnswer.setEnabled(true); // Aktifkan tombol aksi

        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int subtleBorderColor = ContextCompat.getColor(this, R.color.border_subtle);

        // Reset struktur visual Opsi 1
        cardOption1.setStrokeColor(subtleBorderColor);
        cardOption1.setCardBackgroundColor(Color.WHITE);
        dotOption1.setVisibility(View.GONE);
        radioContainer1.setBackgroundResource(R.drawable.bg_radio_circle);

        // Reset struktur visual Opsi 2
        cardOption2.setStrokeColor(subtleBorderColor);
        cardOption2.setCardBackgroundColor(Color.WHITE);
        dotOption2.setVisibility(View.GONE);
        radioContainer2.setBackgroundResource(R.drawable.bg_radio_circle);

        // Berikan penekanan visual warna hijau/primary pada opsi terpilih
        if (optionIndex == 1) {
            cardOption1.setStrokeColor(primaryColor);
            cardOption1.setCardBackgroundColor(Color.parseColor("#F4FBF7")); // BG Hijau Transparan/Muda
            dotOption1.setVisibility(View.VISIBLE);
        } else if (optionIndex == 2) {
            cardOption2.setStrokeColor(primaryColor);
            cardOption2.setCardBackgroundColor(Color.parseColor("#F4FBF7"));
            dotOption2.setVisibility(View.VISIBLE);
        }
    }

    private void resetQuizState() {
        isCheckedState = false;
        selectedOptionIndex = -1;
        btnSubmitAnswer.setEnabled(false);
        btnSubmitAnswer.setText("Periksa Jawaban");
        layoutFeedbackBadge.setVisibility(View.GONE);

        int subtleBorderColor = ContextCompat.getColor(this, R.color.border_subtle);

        cardOption1.setStrokeColor(subtleBorderColor);
        cardOption1.setCardBackgroundColor(Color.WHITE);
        dotOption1.setVisibility(View.GONE);

        cardOption2.setStrokeColor(subtleBorderColor);
        cardOption2.setCardBackgroundColor(Color.WHITE);
        dotOption2.setVisibility(View.GONE);
    }
}