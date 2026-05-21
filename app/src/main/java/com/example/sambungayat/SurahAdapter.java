package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.ViewHolder> {
    private Context context;
    private List<Surah> surahList;

    public SurahAdapter(Context context, List<Surah> surahList) {
        this.context = context;
        this.surahList = surahList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Surah surah = surahList.get(position);
        holder.tvNumber.setText(String.valueOf(surah.getId()));
        holder.tvSurahName.setText(surah.getNameId());
        holder.tvSurahArabic.setText(surah.getNameArabic());
        holder.tvTotalVerses.setText(surah.getTotalVerses() + " Ayat");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("SURAH_ID", surah.getId());
            intent.putExtra("SURAH_NAME", surah.getNameId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return surahList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvSurahName, tvSurahArabic, tvTotalVerses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvSurahName = itemView.findViewById(R.id.tvSurahName);
            tvSurahArabic = itemView.findViewById(R.id.tvSurahArabic);
            tvTotalVerses = itemView.findViewById(R.id.tvTotalVerses);
        }
    }
}