package com.pushgram.app.progression.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pushgram.app.R;
import com.pushgram.app.databinding.ActivityExercisesBinding;
import com.pushgram.app.progression.data.ExerciseLibrary;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.Exercise;
import com.pushgram.app.rank.model.RankTier;

import java.util.ArrayList;
import java.util.List;

public class ExercisesActivity extends AppCompatActivity {

    private ActivityExercisesBinding binding;
    private ProgressionStore store;
    private ExerciseLibrary library;
    private ExerciseAdapter adapter;
    private Exercise.Category currentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExercisesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        store   = ProgressionStore.getInstance(this);
        library = ExerciseLibrary.getInstance();

        setupHeader();
        setupFilters();
        setupList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeader();
        loadExercises();
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void refreshHeader() {
        long xp = store.getTotalXp();
        RankTier rank = RankTier.fromXp(xp);
        binding.tvRank.setText(rank.getDisplayName());
        binding.tvXp.setText(xp + " XP");
        binding.rankProgress.setProgress((int)(rank.progressInTier(xp) * 100));
        binding.tvXpToNext.setText(rank.maxXp == Long.MAX_VALUE ? "MAX" :
                rank.xpToNextTier(xp) + " XP to " + nextRankName(rank));
    }

    private String nextRankName(RankTier current) {
        RankTier[] tiers = RankTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i] == current) return tiers[i + 1].name;
        }
        return "Legend";
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> { currentFilter = null; loadExercises(); });
        binding.chipPush.setOnClickListener(v -> { currentFilter = Exercise.Category.PUSH; loadExercises(); });
        binding.chipPull.setOnClickListener(v -> { currentFilter = Exercise.Category.PULL; loadExercises(); });
        binding.chipLegs.setOnClickListener(v -> { currentFilter = Exercise.Category.LEGS; loadExercises(); });
        binding.chipCore.setOnClickListener(v -> { currentFilter = Exercise.Category.CORE; loadExercises(); });
        binding.chipSkills.setOnClickListener(v -> { currentFilter = Exercise.Category.FULL_BODY; loadExercises(); });
    }

    private void setupList() {
        adapter = new ExerciseAdapter(this, new ArrayList<>(), exercise -> {
            Intent intent = new Intent(this, ExerciseDetailActivity.class);
            intent.putExtra("exercise_id", exercise.getId());
            startActivity(intent);
        });
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExercises.setAdapter(adapter);
        loadExercises();
    }

    private void loadExercises() {
        List<Exercise> list = currentFilter == null
                ? library.getAll()
                : library.getByCategory(currentFilter);

        // Add progress info
        List<ExerciseAdapter.ExerciseItem> items = new ArrayList<>();
        for (Exercise e : list) {
            int level = store.getProgressionLevel(e.getId());
            int bestReps = store.getBestReps(e.getId());
            items.add(new ExerciseAdapter.ExerciseItem(e, level, bestReps));
        }
        adapter.updateData(items);
    }
}
