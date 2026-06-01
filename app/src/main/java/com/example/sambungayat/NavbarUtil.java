package com.example.sambungayat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class NavbarUtil {
    public static void setupNavbar(Activity activity, int activeId) {
        View navbar = activity.findViewById(R.id.bottomNavBar);
        if (navbar == null) return;

        LinearLayout navHome = navbar.findViewById(R.id.navHome);
        LinearLayout navPlay = navbar.findViewById(R.id.navPlay);
        LinearLayout navRanks = navbar.findViewById(R.id.navRanks);
        LinearLayout navProfile = navbar.findViewById(R.id.navProfile);

        resetItem(activity, navHome, R.id.iconHome, R.id.tvHome, R.id.containerHome);
        resetItem(activity, navPlay, R.id.iconPlay, R.id.tvPlay, R.id.containerPlay);
        resetItem(activity, navRanks, R.id.iconRanks, R.id.tvRanks, R.id.containerRanks);
        resetItem(activity, navProfile, R.id.iconProfile, R.id.tvProfile, R.id.containerProfile);

        if (activeId == R.id.navHome) setActive(activity, navHome, R.id.iconHome, R.id.tvHome, R.id.containerHome);
        else if (activeId == R.id.navPlay) setActive(activity, navPlay, R.id.iconPlay, R.id.tvPlay, R.id.containerPlay);
        else if (activeId == R.id.navRanks) setActive(activity, navRanks, R.id.iconRanks, R.id.tvRanks, R.id.containerRanks);
        else if (activeId == R.id.navProfile) setActive(activity, navProfile, R.id.iconProfile, R.id.tvProfile, R.id.containerProfile);

        navHome.setOnClickListener(v -> {
            if (activeId != R.id.navHome) {
                activity.startActivity(new Intent(activity, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                activity.overridePendingTransition(0, 0);
            }
        });
        navPlay.setOnClickListener(v -> {
            if (activeId != R.id.navPlay) {
                activity.startActivity(new Intent(activity, PilihSurahActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                activity.overridePendingTransition(0, 0);
            }
        });
        navRanks.setOnClickListener(v -> {
            if (activeId != R.id.navRanks) {
                activity.startActivity(new Intent(activity, LeaderboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                activity.overridePendingTransition(0, 0);
            }
        });
        navProfile.setOnClickListener(v -> {
            if (activeId != R.id.navProfile) {
                activity.startActivity(new Intent(activity, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                activity.overridePendingTransition(0, 0);
            }
        });
    }

    private static void resetItem(Activity activity, View item, int iconId, int textId, int containerId) {
        ImageView icon = item.findViewById(iconId);
        TextView text = item.findViewById(textId);
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(activity, android.R.color.white));
            icon.setAlpha(0.6f);
        }
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(activity, android.R.color.white));
            text.setAlpha(0.6f);
        }
    }

    private static void setActive(Activity activity, View item, int iconId, int textId, int containerId) {
        ImageView icon = item.findViewById(iconId);
        TextView text = item.findViewById(textId);
        View container = item.findViewById(containerId);
        // Menggunakan warna secondary (emas logo) untuk item aktif
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(activity, R.color.secondary));
            icon.setAlpha(1.0f);
        }
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(activity, R.color.secondary));
            text.setTypeface(null, Typeface.BOLD);
            text.setAlpha(1.0f);
        }
        if (container != null) container.setBackgroundResource(R.drawable.bg_nav_active_pill);
    }
}