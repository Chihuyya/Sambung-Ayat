package com.example.sambungayat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.ViewHolder> {

    private List<Surah> surahListMaster = new ArrayList<>();
    private List<Integer> selectedSurahIds = new ArrayList<>();
    // Warna Coklat Logo
    private final int COLOR_PRIMARY = Color.parseColor("#5D4037");
    private final int COLOR_SECONDARY = Color.parseColor("#D4AF37");

    public SurahAdapter(List<Surah> surahList) {
        this.surahListMaster = surahList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surah, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Surah surah = surahListMaster.get(position);
        holder.tvNumber.setText(String.valueOf(surah.getSurahNumber()));
        holder.tvName.setText(surah.getName());
        holder.tvArabic.setText(surah.getNameArabic());
        holder.tvInfo.setText(surah.getTotalVerses() + " Ayat");
        
        int progress = surah.getProgress();
        holder.progressIndicator.setProgress(progress);
        holder.tvProgressPercent.setText(progress + "%");

        boolean isLocked = surah.getStatus().equalsIgnoreCase("locked");
        holder.imgLock.setVisibility(isLocked ? View.VISIBLE : View.GONE);

        if (isLocked) {
            holder.cardView.setAlpha(0.6f);
            holder.cardView.setStrokeColor(Color.LTGRAY);
            holder.tvNumber.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
            holder.itemView.setOnClickListener(v -> 
                new SweetAlertDialog(v.getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Terkunci!")
                        .setContentText("Selesaikan surah sebelumnya untuk membuka ini.")
                        .setConfirmText("Siap")
                        .setConfirmButtonBackgroundColor(COLOR_PRIMARY)
                        .show()
            );
        } else {
            holder.cardView.setAlpha(1.0f);
            holder.tvNumber.setBackgroundTintList(android.content.res.ColorStateList.valueOf(COLOR_PRIMARY));
            
            if (selectedSurahIds.contains(surah.getId())) {
                // Style saat terpilih
                holder.cardView.setStrokeColor(COLOR_SECONDARY);
                holder.cardView.setStrokeWidth(6);
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFDF5E6")); // Light cream
            } else if (progress >= 100) {
                // Style saat sudah lulus
                holder.cardView.setStrokeColor(COLOR_PRIMARY);
                holder.cardView.setStrokeWidth(3);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            } else {
                // Style normal
                holder.cardView.setStrokeColor(Color.parseColor("#D7CCC8"));
                holder.cardView.setStrokeWidth(2);
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (selectedSurahIds.contains(surah.getId())) {
                    selectedSurahIds.remove(Integer.valueOf(surah.getId()));
                } else {
                    selectedSurahIds.add(surah.getId());
                }
                notifyItemChanged(position);
            });
        }
    }

    public void updateData(List<Surah> newList) {
        this.surahListMaster = newList;
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedSurahIds() { return selectedSurahIds; }
    
    public String getSelectedSurahName() {
        if (selectedSurahIds.size() == 1) {
            for (Surah s : surahListMaster) if (selectedSurahIds.contains(s.getId())) return s.getName();
        }
        return "Campuran";
    }

    @Override
    public int getItemCount() { return surahListMaster.size(); }

    public void selectAll() {
        selectedSurahIds.clear();
        for (Surah s : surahListMaster) if (!s.getStatus().equalsIgnoreCase("locked")) selectedSurahIds.add(s.getId());
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedSurahIds.clear();
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        int unlockCount = 0;
        for (Surah s : surahListMaster) if (!s.getStatus().equalsIgnoreCase("locked")) unlockCount++;
        return unlockCount > 0 && selectedSurahIds.size() == unlockCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvName, tvInfo, tvArabic, tvProgressPercent;
        ImageView imgLock;
        LinearProgressIndicator progressIndicator;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvSurahNumber);
            tvName = itemView.findViewById(R.id.tvSurahName);
            tvArabic = itemView.findViewById(R.id.tvSurahArabic);
            tvInfo = itemView.findViewById(R.id.tvSurahInfo);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            imgLock = itemView.findViewById(R.id.imgLock);
            progressIndicator = itemView.findViewById(R.id.progressSurah);
            cardView = (MaterialCardView) itemView;
        }
    }
}
