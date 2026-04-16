package com.pushgram.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pushgram.app.battle.ui.BattleLobbyActivity;
import com.pushgram.app.databinding.ActivityMainBinding;
import com.pushgram.app.model.CreditManager;
import com.pushgram.app.music.ui.MusicActivity;
import com.pushgram.app.progression.ui.ExercisesActivity;
import com.pushgram.app.rank.RankManager;
import com.pushgram.app.rank.model.RankTier;
import com.pushgram.app.rank.ui.LeaderboardActivity;
import com.pushgram.app.service.InstagramMonitorService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CreditManager creditManager;
    private RankManager rankManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        creditManager = CreditManager.getInstance(this);
        rankManager   = RankManager.getInstance(this);
        rankManager.addDemoEntries();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
        updateAccessibilityStatus();
    }

    private void setupButtons() {
        binding.btnStartWorkout.setOnClickListener(v ->
                startActivity(new Intent(this, WorkoutActivity.class)));

        binding.btnExercises.setOnClickListener(v ->
                startActivity(new Intent(this, ExercisesActivity.class)));

        binding.btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        binding.btnBattle.setOnClickListener(v ->
                startActivity(new Intent(this, BattleLobbyActivity.class)));

        binding.btnMusic.setOnClickListener(v ->
                startActivity(new Intent(this, MusicActivity.class)));

        binding.btnEnableAccessibility.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "Find 'PushGram' and toggle it ON", Toast.LENGTH_LONG).show();
        });
    }

    private void refreshStats() {
        binding.tvCredits.setText(String.valueOf(creditManager.getCredits()));
        binding.tvTotalPushups.setText(String.valueOf(creditManager.getTotalPushups()));

        long xp = rankManager != null ?
                com.pushgram.app.progression.data.ProgressionStore.getInstance(this).getTotalXp() : 0;
        RankTier rank = RankTier.fromXp(xp);
        binding.tvRankBadge.setText(rank.getDisplayName());
        binding.tvXpAmount.setText(xp + " XP");
        binding.rankProgressBar.setProgress((int)(rank.progressInTier(xp) * 100));
    }

    private void updateAccessibilityStatus() {
        boolean enabled = isAccessibilityEnabled();
        binding.tvAccessibilityStatus.setText(enabled
                ? "✅ Instagram monitoring ACTIVE"
                : "⚠️ Tap to enable Instagram monitor");
        binding.tvAccessibilityStatus.setTextColor(enabled ? 0xFF4CAF50 : 0xFFFF9800);
        binding.btnEnableAccessibility.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private boolean isAccessibilityEnabled() {
        String svcName = getPackageName() + "/" +
                InstagramMonitorService.class.getCanonicalName();
        try {
            int en = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (en == 0) return false;
            String svcs = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (svcs == null) return false;
            TextUtils.SimpleStringSplitter sp = new TextUtils.SimpleStringSplitter(':');
            sp.setString(svcs);
            while (sp.hasNext()) if (sp.next().equalsIgnoreCase(svcName)) return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
