package com.pushgram.app.rank.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pushgram.app.R;
import com.pushgram.app.databinding.ActivityLeaderboardBinding;
import com.pushgram.app.firebase.FirebaseManager;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.UserProfile;
import com.pushgram.app.rank.RankManager;
import com.pushgram.app.rank.model.LeaderboardEntry;
import com.pushgram.app.rank.model.RankTier;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ActivityLeaderboardBinding binding;
    private RankManager rankManager;
    private ProgressionStore store;
    private FirebaseManager firebase;
    private LeaderboardAdapter adapter;
    private boolean showingGlobal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rankManager = RankManager.getInstance(this);
        store       = ProgressionStore.getInstance(this);
        firebase    = FirebaseManager.getInstance();

        // Add demo entries so leaderboard isn't empty on first launch
        rankManager.addDemoEntries();
        rankManager.syncCurrentUser();

        setupHeader();
        setupTabs();
        setupList();
        loadLocal();
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());

        UserProfile p = store.getProfile();
        if (p != null) {
            RankTier rank = RankTier.fromXp(p.getTotalXp());
            binding.tvMyRank.setText(rank.getDisplayName());
            binding.tvMyXp.setText(p.getTotalXp() + " XP");
            binding.tvMyName.setText(p.getDisplayName());
            binding.tvMyReps.setText(p.getTotalReps() + " total reps");
        }

        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
    }

    private void setupTabs() {
        binding.btnLocalTab.setOnClickListener(v -> {
            showingGlobal = false;
            binding.btnLocalTab.setAlpha(1f);
            binding.btnGlobalTab.setAlpha(0.5f);
            loadLocal();
        });

        binding.btnGlobalTab.setOnClickListener(v -> {
            showingGlobal = true;
            binding.btnLocalTab.setAlpha(0.5f);
            binding.btnGlobalTab.setAlpha(1f);
            loadGlobal();
        });
    }

    private void setupList() {
        adapter = new LeaderboardAdapter(this, new ArrayList<>());
        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLeaderboard.setAdapter(adapter);
    }

    private void loadLocal() {
        binding.tvLeaderboardTitle.setText("📱 Local Rankings");
        binding.tvLeaderboardSubtitle.setText("Players on this device");
        binding.progressLoading.setVisibility(View.GONE);

        List<LeaderboardEntry> board = rankManager.getRankedLocalBoard();
        adapter.updateData(board);
        updateMyPosition(board);
    }

    private void loadGlobal() {
        binding.tvLeaderboardTitle.setText("🌍 Global Rankings");
        binding.tvLeaderboardSubtitle.setText("Top players worldwide");

        if (!firebase.isAvailable()) {
            binding.progressLoading.setVisibility(View.GONE);
            Toast.makeText(this,
                    "Global leaderboard requires Firebase setup.\nSee README for instructions.",
                    Toast.LENGTH_LONG).show();
            showingGlobal = false;
            loadLocal();
            return;
        }

        binding.progressLoading.setVisibility(View.VISIBLE);
        firebase.pushToGlobalLeaderboard(store.getProfile());

        UserProfile p = store.getProfile();
        String uid = p != null ? p.getUserId() : "";

        firebase.fetchGlobalLeaderboard(uid, new FirebaseManager.LeaderboardCallback() {
            @Override
            public void onLoaded(List<LeaderboardEntry> entries) {
                runOnUiThread(() -> {
                    binding.progressLoading.setVisibility(View.GONE);
                    adapter.updateData(entries);
                    updateMyPosition(entries);
                });
            }
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    binding.progressLoading.setVisibility(View.GONE);
                    Toast.makeText(LeaderboardActivity.this,
                            "Failed to load: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateMyPosition(List<LeaderboardEntry> board) {
        for (LeaderboardEntry e : board) {
            if (e.isCurrentUser()) {
                binding.tvMyPosition.setText("Your rank: " + e.getRankMedal());
                return;
            }
        }
        binding.tvMyPosition.setText("Your rank: Unranked");
    }

    private void showEditNameDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        UserProfile p = store.getProfile();
        if (p != null) input.setText(p.getDisplayName());
        input.setPadding(48, 24, 48, 8);

        new AlertDialog.Builder(this)
                .setTitle("Set Display Name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        store.setDisplayName(name);
                        rankManager.syncCurrentUser();
                        binding.tvMyName.setText(name);
                        if (showingGlobal) loadGlobal(); else loadLocal();
                        Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
