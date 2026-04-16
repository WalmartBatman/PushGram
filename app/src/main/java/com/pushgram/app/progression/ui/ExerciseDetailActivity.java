package com.pushgram.app.progression.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pushgram.app.databinding.ActivityExerciseDetailBinding;
import com.pushgram.app.progression.data.ExerciseLibrary;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.Exercise;
import com.pushgram.app.progression.model.WorkoutLog;
import com.pushgram.app.rank.RankManager;
import com.pushgram.app.rank.model.RankTier;

import java.util.List;

public class ExerciseDetailActivity extends AppCompatActivity {

    private ActivityExerciseDetailBinding binding;
    private Exercise exercise;
    private ProgressionStore store;
    private RankManager rankManager;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        store       = ProgressionStore.getInstance(this);
        rankManager = RankManager.getInstance(this);

        String exerciseId = getIntent().getStringExtra("exercise_id");
        exercise = ExerciseLibrary.getInstance().get(exerciseId);
        if (exercise == null) { finish(); return; }

        currentLevel = store.getProgressionLevel(exercise.getId());

        setupUI();
        populateData();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnLogWorkout.setOnClickListener(v -> showLogWorkoutDialog());

        binding.btnAdvanceLevel.setOnClickListener(v -> {
            if (currentLevel < 5) {
                new AlertDialog.Builder(this)
                        .setTitle("Advance to Level " + (currentLevel + 1) + "?")
                        .setMessage("Only advance when you've hit the goal for your current level: " +
                                exercise.getLevel(currentLevel).getGoalText())
                        .setPositiveButton("Yes, I'm ready!", (d, w) -> {
                            store.advanceProgressionLevel(exercise.getId());
                            currentLevel++;
                            populateData();
                            Toast.makeText(this, "🎉 Advanced to Level " + currentLevel + "!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Not yet", null)
                        .show();
            } else {
                Toast.makeText(this, "🏆 Max level reached!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData() {
        // Header
        binding.tvExerciseEmoji.setText(exercise.getEmoji());
        binding.tvExerciseName.setText(exercise.getName());
        binding.tvExerciseCategory.setText(exercise.getCategory().label + " · " + exercise.getDifficultyStars());
        binding.tvExerciseDescription.setText(exercise.getDescription());
        binding.tvEquipment.setText("Equipment: " + exercise.getEquipment());

        // Current progression level
        Exercise.ProgressionLevel lvl = exercise.getLevel(currentLevel);
        binding.tvCurrentLevelName.setText("Level " + currentLevel + " — " + lvl.name);
        binding.tvCurrentLevelDesc.setText(lvl.description);
        binding.tvCurrentLevelGoal.setText("Goal: " + lvl.getGoalText());
        binding.tvFormCue.setText("💡 " + lvl.formCue);
        binding.tvXpPerRep.setText("+" + lvl.xpPerRep + " XP per rep");
        binding.progressionBar.setProgress(currentLevel * 20);
        binding.tvProgressionLabel.setText("Progression: " + currentLevel + "/5");
        binding.btnAdvanceLevel.setText(currentLevel < 5 ? "Advance to Level " + (currentLevel + 1) : "✅ Max Level");

        // All 5 progression levels
        buildProgressionPath();

        // Muscle targeting
        buildMuscleSection();

        // Personal stats
        binding.tvBestReps.setText(String.valueOf(store.getBestReps(exercise.getId())));
        binding.tvTotalReps.setText(String.valueOf(store.getTotalRepsForExercise(exercise.getId())));
        List<WorkoutLog> logs = store.getLogsForExercise(exercise.getId());
        binding.tvWorkoutCount.setText(String.valueOf(logs.size()));
    }

    private void buildProgressionPath() {
        binding.layoutProgressionPath.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            Exercise.ProgressionLevel lvl = exercise.getLevel(i);
            View row = getLayoutInflater().inflate(
                    com.pushgram.app.R.layout.item_progression_level,
                    binding.layoutProgressionPath, false);

            row.findViewById(com.pushgram.app.R.id.tvLevelNumber)
                    .setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            i <= currentLevel ? 0xFFFF6B35 : 0xFF2A2A2A));
            ((android.widget.TextView) row.findViewById(com.pushgram.app.R.id.tvLevelNumber))
                    .setText(String.valueOf(i));
            ((android.widget.TextView) row.findViewById(com.pushgram.app.R.id.tvLevelName))
                    .setText(lvl.name);
            ((android.widget.TextView) row.findViewById(com.pushgram.app.R.id.tvLevelGoal))
                    .setText(lvl.getGoalText() + " · " + lvl.xpPerRep + " XP/rep");

            // Mark completed/current/locked
            android.widget.TextView tvStatus = row.findViewById(com.pushgram.app.R.id.tvLevelStatus);
            if (i < currentLevel) {
                tvStatus.setText("✅ Done");
                tvStatus.setTextColor(0xFF4CAF50);
            } else if (i == currentLevel) {
                tvStatus.setText("▶ Current");
                tvStatus.setTextColor(0xFFFF6B35);
            } else {
                tvStatus.setText("🔒 Locked");
                tvStatus.setTextColor(0xFF666666);
            }

            binding.layoutProgressionPath.addView(row);
        }
    }

    private void buildMuscleSection() {
        // Primary
        StringBuilder primary = new StringBuilder();
        for (Exercise.MuscleGroup mg : exercise.getPrimaryMuscles()) {
            if (primary.length() > 0) primary.append("\n");
            primary.append(mg.emoji).append(" ").append(mg.name).append(" (Primary)");
        }
        // Secondary
        for (Exercise.MuscleGroup mg : exercise.getSecondaryMuscles()) {
            if (primary.length() > 0) primary.append("\n");
            primary.append(mg.emoji).append(" ").append(mg.name).append(" (Secondary)");
        }
        // Stabilizers
        for (Exercise.MuscleGroup mg : exercise.getStabilizers()) {
            if (primary.length() > 0) primary.append("\n");
            primary.append(mg.emoji).append(" ").append(mg.name).append(" (Stabilizer)");
        }
        binding.tvMusclesList.setText(primary.toString());
    }

    private void showLogWorkoutDialog() {
        Exercise.ProgressionLevel lvl = exercise.getLevel(currentLevel);

        View view = getLayoutInflater().inflate(
                com.pushgram.app.R.layout.dialog_log_workout, null);
        android.widget.EditText etReps = view.findViewById(com.pushgram.app.R.id.etReps);
        android.widget.EditText etSets = view.findViewById(com.pushgram.app.R.id.etSets);
        android.widget.TextView tvXpPreview = view.findViewById(com.pushgram.app.R.id.tvXpPreview);

        etReps.setText(String.valueOf(lvl.targetReps));
        etSets.setText(String.valueOf(lvl.targetSets));

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                try {
                    int r = Integer.parseInt(etReps.getText().toString());
                    int st2 = Integer.parseInt(etSets.getText().toString());
                    long xp = (long) r * st2 * lvl.xpPerRep;
                    tvXpPreview.setText("+" + xp + " XP");
                } catch (Exception e) { tvXpPreview.setText(""); }
            }
        };
        etReps.addTextChangedListener(watcher);
        etSets.addTextChangedListener(watcher);

        new AlertDialog.Builder(this)
                .setTitle("Log " + exercise.getName())
                .setView(view)
                .setPositiveButton("Log Workout", (d, w) -> {
                    try {
                        int reps = Integer.parseInt(etReps.getText().toString());
                        int sets = Integer.parseInt(etSets.getText().toString());
                        long xp = (long) reps * sets * lvl.xpPerRep;

                        WorkoutLog log = new WorkoutLog(
                                exercise.getId(), reps, sets, currentLevel, xp, true);
                        store.logWorkout(log);
                        rankManager.syncCurrentUser();

                        binding.tvBestReps.setText(String.valueOf(store.getBestReps(exercise.getId())));
                        binding.tvTotalReps.setText(String.valueOf(store.getTotalRepsForExercise(exercise.getId())));
                        binding.tvWorkoutCount.setText(String.valueOf(
                                store.getLogsForExercise(exercise.getId()).size()));

                        Toast.makeText(this, "✅ Logged! +" + xp + " XP", Toast.LENGTH_SHORT).show();

                        // Check if user has met goal to suggest level advance
                        if (reps >= lvl.targetReps && sets >= lvl.targetSets && currentLevel < 5) {
                            new AlertDialog.Builder(this)
                                    .setTitle("🎉 Goal Hit!")
                                    .setMessage("You hit the goal for Level " + currentLevel + "! Ready to advance?")
                                    .setPositiveButton("Advance!", (d2, w2) -> {
                                        store.advanceProgressionLevel(exercise.getId());
                                        currentLevel++;
                                        populateData();
                                    })
                                    .setNegativeButton("Not yet", null)
                                    .show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
