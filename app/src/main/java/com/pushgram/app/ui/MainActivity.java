package com.pushgram.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
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

    private static final String PREFS       = "pushgram_prefs";
    private static final String KEY_PIC_URI = "profile_pic_uri";

    private ActivityMainBinding binding;
    private CreditManager creditManager;
    private RankManager rankManager;
    private SharedPreferences prefs;

    // Change #3 — profile picture picker
    private final ActivityResultLauncher<Intent> pickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Persist permission + save URI
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        prefs.edit().putString(KEY_PIC_URI, uri.toString()).apply();
                        loadProfilePic(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Change #2 — redirect to onboarding if not done
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean("onboarding_done", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        creditManager = CreditManager.getInstance(this);
        rankManager   = RankManager.getInstance(this);
        rankManager.addDemoEntries();
        setupButtons();
        loadSavedProfilePic();
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

        // Change #3 — tap avatar to pick new profile picture
        binding.ivProfileAvatar.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickerLauncher.launch(intent);
    }

    private void loadSavedProfilePic() {
        String uriStr = prefs.getString(KEY_PIC_URI, null);
        if (uriStr != null) loadProfilePic(Uri.parse(uriStr));
    }

    private void loadProfilePic(Uri uri) {
        Glide.with(this)
             .load(uri)
             .transform(new CircleCrop())
             .placeholder(android.R.drawable.ic_menu_gallery)
             .into(binding.imgProfilePic);
        binding.tvAvatarInitials.setVisibility(View.GONE);
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
        // Change #4 — banner is at bottom; always show, just update visibility
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
