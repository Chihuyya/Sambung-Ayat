package com.example.sambungayat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {

    private final List<Badge> badgeList;

    public BadgeAdapter(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Badge badge = badgeList.get(position);

        holder.tvTitle.setText(badge.getTitle());
        holder.tvDesc.setText(badge.getDescription());
        holder.ivIcon.setImageResource(badge.getIconResId());

        if (badge.isUnlocked()) {
            // State: Unlocked (Warna Aktif - Tema Gold/Primary)
            holder.iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF8E1")));
            holder.ivIcon.setColorFilter(Color.parseColor("#FBC02D"));
            holder.tvTitle.setTextColor(Color.parseColor("#121C2A")); // on-surface
            holder.cardBadge.setCardBackgroundColor(Color.WHITE);
            holder.cardBadge.setAlpha(1.0f);
        } else {
            // State: Locked (Warna Pasif - Abu-abu Transparan)
            holder.iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F0F0F0")));
            holder.ivIcon.setColorFilter(Color.parseColor("#9E9E9E"));
            holder.tvTitle.setTextColor(Color.parseColor("#757575"));
            holder.cardBadge.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
            holder.cardBadge.setAlpha(0.6f);
        }
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardBadge;
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvTitle, tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBadge = itemView.findViewById(R.id.cardBadge);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivIcon = itemView.findViewById(R.id.ivBadgeIcon);
            tvTitle = itemView.findViewById(R.id.tvBadgeTitle);
            tvDesc = itemView.findViewById(R.id.tvBadgeDesc);
        }
    }
}