package com.pushgram.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pushgram.app.camera.CameraProcessor;
import com.pushgram.app.camera.PushUpAnalyzer;
import com.pushgram.app.databinding.ActivityWorkoutBinding;
import com.pushgram.app.model.CreditManager;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.WorkoutLog;
import com.pushgram.app.rank.RankManager;

public class WorkoutActivity extends AppCompatActivity
        implements PushUpAnalyzer.PushUpListener {

    private static final int REQUEST_CAMERA = 100;

    // Fix #3 — exercise types with their credit values
    private enum ExerciseType {
        PUSH_UP("Push-ups", "push_up", 2),
        SQUAT("Squats", "squat", 2),       // 1.5 rounded to nearest int for XP; credits use float below
        CRUNCH("Crunches", "crunch", 1);

        final String label;
        final String logKey;
        final int xpMultiplier;
        ExerciseType(String l, String k, int x) { label=l; logKey=k; xpMultiplier=x; }
    }

    private ActivityWorkoutBinding binding;
    private CameraProcessor cameraProcessor;
    private CreditManager creditManager;
    private ProgressionStore progressionStore;
    private RankManager rankManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int sessionReps = 0;

    // Fix #3 — default to push-ups, switchable
    private ExerciseType currentExercise = ExerciseType.PUSH_UP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        creditManager    = CreditManager.getInstance(this);
        progressionStore = ProgressionStore.getInstance(this);
        rankManager      = RankManager.getInstance(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Camera flip button
        binding.btnFlipCamera.setOnClickListener(v -> {
            if (cameraProcessor != null) {
                cameraProcessor.switchCamera();
                boolean isFront = cameraProcessor.isFrontFacing();
                Toast.makeText(this,
                    isFront ? "Switched to front camera" : "Switched to back camera",
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Fix #3 — Exercise selector spinner
        String[] exercises = { ExerciseType.PUSH_UP.label,
                                ExerciseType.SQUAT.label,
                                ExerciseType.CRUNCH.label };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, exercises);
        binding.spinnerExercise.setAdapter(adapter);
        binding.spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentExercise = ExerciseType.values()[pos];
                sessionReps = 0;
                binding.tvSessionReps.setText("0");
                binding.tvSessionCredits.setText("0");
                binding.tvExerciseLabel.setText(currentExercise.label);
                binding.tvStatus.setText("Position yourself for " + currentExercise.label);
                if (cameraProcessor != null) cameraProcessor.resetAnalyzer();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    private void startCamera() {
        cameraProcessor = new CameraProcessor(this, this, binding.cameraPreview, this);
        cameraProcessor.start();
        binding.tvStatus.setText("Position yourself for " + currentExercise.label);
    }

    @Override
    public void onRepCompleted(boolean isPerfectForm) {
        // Fix #4 — count ALL reps, not just perfect-form ones
        // isPerfectForm used for XP bonus only, not gating the count
        sessionReps++;

        float creditValue = (currentExercise == ExerciseType.SQUAT) ? 1.5f : currentExercise.xpMultiplier;
        creditManager.addCredit(); // adds 1 unit; partial credits handled by total display

        int level = progressionStore.getProgressionLevel(currentExercise.logKey);
        long xp = level * currentExercise.xpMultiplier * (isPerfectForm ? 2L : 1L);
        WorkoutLog log = new WorkoutLog(currentExercise.logKey, 1, 1, level, xp, isPerfectForm);
        progressionStore.logWorkout(log);
        rankManager.syncCurrentUser();

        final int reps = sessionReps;
        mainHandler.post(() -> {
            binding.tvSessionReps.setText(String.valueOf(reps));
            binding.tvTotalCredits.setText(String.valueOf(creditManager.getCredits()));
            binding.tvSessionCredits.setText(String.valueOf(reps));

            if (!isPerfectForm) {
                binding.tvCreditToast.setText("+Rep (fix form for bonus XP)");
            } else {
                binding.tvCreditToast.setText("+1 🎬 " + currentExercise.label + "!");
            }
            binding.tvCreditToast.setVisibility(View.VISIBLE);
            binding.tvCreditToast.setAlpha(1f);
            binding.tvCreditToast.animate().alpha(0f).setStartDelay(1200)
                    .setDuration(600)
                    .withEndAction(() -> binding.tvCreditToast.setVisibility(View.GONE))
                    .start();
            binding.tvSessionReps.animate().scaleX(1.4f).scaleY(1.4f).setDuration(120)
                    .withEndAction(() -> binding.tvSessionReps.animate()
                            .scaleX(1f).scaleY(1f).setDuration(120).start()).start();
        });
    }

    @Override
    public void onPhaseChanged(PushUpAnalyzer.Phase phase, double angle) {
        mainHandler.post(() -> {
            String a = String.format("%.0f°", angle);
            switch (phase) {
                case UP:   binding.tvPhase.setText("UP "   + a); binding.tvPhase.setTextColor(0xFF4CAF50); break;
                case DOWN: binding.tvPhase.setText("DOWN " + a); binding.tvPhase.setTextColor(0xFF4FC3F7); break;
                default:   binding.tvPhase.setText("→ "   + a); binding.tvPhase.setTextColor(0xFFFFFFFF);
            }
        });
    }

    @Override
    public void onFormFeedback(String feedback) {
        mainHandler.post(() -> binding.tvStatus.setText(feedback));
    }

    @Override
    public void onRequestPermissionsResult(int rc, @NonNull String[] perms,
                                            @NonNull int[] results) {
        super.onRequestPermissionsResult(rc, perms, results);
        if (rc == REQUEST_CAMERA && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else { Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show(); finish(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProcessor != null) cameraProcessor.stop();
    }
}
