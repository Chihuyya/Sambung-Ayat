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
    private static final String TAG = "NavbarUtil";

    public static void setupNavbar(Activity activity, int activeId) {
        // Mencari bottomNavBar. Jika di layout pakai <include>, pastikan ID-nya sesuai.
        View navbar = activity.findViewById(R.id.bottomNavBar);
        
        if (navbar == null) {
            Log.e(TAG, "Navbar view not found! check if R.id.bottomNavBar exists in layout");
            return;
        }

        LinearLayout navHome = navbar.findViewById(R.id.navHome);
        LinearLayout navPlay = navbar.findViewById(R.id.navPlay);
        LinearLayout navRanks = navbar.findViewById(R.id.navRanks);
        LinearLayout navProfile = navbar.findViewById(R.id.navProfile);

        if (navHome == null || navPlay == null || navRanks == null || navProfile == null) {
            Log.e(TAG, "One or more nav items not found in navbar layout");
            return;
        }

        // Reset semua ke state default
        resetItem(activity, navHome, R.id.iconHome, R.id.tvHome, R.id.containerHome);
        resetItem(activity, navPlay, R.id.iconPlay, R.id.tvPlay, R.id.containerPlay);
        resetItem(activity, navRanks, R.id.iconRanks, R.id.tvRanks, R.id.containerRanks);
        resetItem(activity, navProfile, R.id.iconProfile, R.id.tvProfile, R.id.containerProfile);

        // Set state aktif untuk menu yang dipilih
        if (activeId == R.id.navHome) setActive(activity, navHome, R.id.iconHome, R.id.tvHome, R.id.containerHome);
        else if (activeId == R.id.navPlay) setActive(activity, navPlay, R.id.iconPlay, R.id.tvPlay, R.id.containerPlay);
        else if (activeId == R.id.navRanks) setActive(activity, navRanks, R.id.iconRanks, R.id.tvRanks, R.id.containerRanks);
        else if (activeId == R.id.navProfile) setActive(activity, navProfile, R.id.iconProfile, R.id.tvProfile, R.id.containerProfile);

        // Click listeners
        navHome.setOnClickListener(v -> {
            if (activeId != R.id.navHome) {
                Intent intent = new Intent(activity, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                if (!(activity instanceof DashboardActivity)) activity.finish();
            }
        });
        navPlay.setOnClickListener(v -> {
            if (activeId != R.id.navPlay) {
                Intent intent = new Intent(activity, PilihSurahActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                if (!(activity instanceof PilihSurahActivity)) activity.finish();
            }
        });
        navRanks.setOnClickListener(v -> {
            if (activeId != R.id.navRanks) {
                // Ranks bisa mengarah ke LeaderboardActivity atau AchievementsActivity
                // Kita gunakan LeaderboardActivity sebagai default Ranks
                Intent intent = new Intent(activity, LeaderboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                if (!(activity instanceof LeaderboardActivity)) activity.finish();
            }
        });
        navProfile.setOnClickListener(v -> {
            if (activeId != R.id.navProfile) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                if (!(activity instanceof ProfileActivity)) activity.finish();
            }
        });
    }

    private static void resetItem(Activity activity, View item, int iconId, int textId, int containerId) {
        ImageView icon = item.findViewById(iconId);
        TextView text = item.findViewById(textId);
        View container = item.findViewById(containerId);

        if (icon != null) icon.setColorFilter(ContextCompat.getColor(activity, R.color.text_variant));
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(activity, R.color.text_variant));
            text.setTypeface(null, Typeface.NORMAL);
            text.setVisibility(View.VISIBLE); // Pastikan teks terlihat
        }
        if (container != null) container.setBackground(null);
    }

    private static void setActive(Activity activity, View item, int iconId, int textId, int containerId) {
        ImageView icon = item.findViewById(iconId);
        TextView text = item.findViewById(textId);
        View container = item.findViewById(containerId);

        if (icon != null) icon.setColorFilter(ContextCompat.getColor(activity, R.color.on_primary_container));
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(activity, R.color.text_main));
            text.setTypeface(null, Typeface.BOLD);
            text.setVisibility(View.VISIBLE); // Pastikan teks terlihat
        }
        if (container != null) container.setBackgroundResource(R.drawable.bg_nav_active_pill);
    }
}