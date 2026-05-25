package com.example.sambungayat;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.ViewHolder> implements Filterable {

    private final List<Surah> surahList;
    private List<Surah> surahListFiltered;

    public SurahAdapter(List<Surah> surahList) {
        this.surahList = surahList;
        this.surahListFiltered = surahList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Surah surah = surahListFiltered.get(position);
        holder.tvNumber.setText(String.valueOf(surah.getSurahNumber()));
        holder.tvName.setText(surah.getName());
        holder.tvInfo.setText(surah.getTotalVerses() + " Ayat");
        
        // Status: 'locked', 'unlocked', 'passed'
        String status = surah.getStatus();
        if (status.equals("locked")) {
            holder.cardView.setAlpha(0.5f);
            holder.cardView.setClickable(false);
            holder.tvStatus.setText("🔒 Terkunci");
            holder.tvStatus.setTextColor(Color.GRAY);
        } else if (status.equals("passed")) {
            holder.cardView.setAlpha(1.0f);
            holder.cardView.setClickable(true);
            holder.tvStatus.setText("✅ Selesai");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.cardView.setClickable(true);
            holder.tvStatus.setText("📖 Terbuka");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (!status.equals("locked")) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("SURAH_ID", surah.getId());
                intent.putExtra("SURAH_NAME", surah.getName());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return surahListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    surahListFiltered = surahList;
                } else {
                    List<Surah> filtered = new ArrayList<>();
                    for (Surah row : surahList) {
                        if (row.getName().toLowerCase().contains(query)) {
                            filtered.add(row);
                        }
                    }
                    surahListFiltered = filtered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = surahListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                surahListFiltered = (List<Surah>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvName, tvInfo, tvStatus;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvSurahNumber);
            tvName = itemView.findViewById(R.id.tvSurahName);
            tvInfo = itemView.findViewById(R.id.tvSurahInfo);
            tvStatus = itemView.findViewById(R.id.tvProgressPercent); // Menggunakan ID yang ada untuk status
            cardView = (MaterialCardView) itemView;
        }
    }
}