package com.pushgram.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pushgram.app.camera.CameraProcessor;
import com.pushgram.app.camera.ExerciseAnalyzer;
import com.pushgram.app.databinding.ActivityWorkoutBinding;
import com.pushgram.app.model.CreditManager;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.WorkoutLog;
import com.pushgram.app.rank.RankManager;

public class WorkoutActivity extends AppCompatActivity
        implements ExerciseAnalyzer.RepListener {

    private static final int REQUEST_CAMERA = 100;
    private ActivityWorkoutBinding binding;
    private CameraProcessor cameraProcessor;
    private ExerciseAnalyzer exerciseAnalyzer;
    private CreditManager creditManager;
    private ProgressionStore progressionStore;
    private RankManager rankManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int sessionReps = 0;
    private int sessionCredits = 0;
    private ExerciseAnalyzer.ExerciseType currentExercise = ExerciseAnalyzer.ExerciseType.PUSHUP;
    private static final float[] CREDITS = { 2.0f, 1.5f, 1.0f };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        creditManager    = CreditManager.getInstance(this);
        progressionStore = ProgressionStore.getInstance(this);
        rankManager      = RankManager.getInstance(this);
        binding.btnBack.setOnClickListener(v -> finish());

        binding.chipGroupExercise.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == binding.chipPushups.getId()) {
                currentExercise = ExerciseAnalyzer.ExerciseType.PUSHUP;
            } else if (id == binding.chipSquats.getId()) {
                currentExercise = ExerciseAnalyzer.ExerciseType.SQUAT;
            } else if (id == binding.chipCrunches.getId()) {
                currentExercise = ExerciseAnalyzer.ExerciseType.CRUNCH;
            }
            if (exerciseAnalyzer != null) exerciseAnalyzer.setExerciseType(currentExercise);
            sessionReps = 0; sessionCredits = 0;
            binding.tvSessionReps.setText("0");
            binding.tvSessionCredits.setText("0");
            binding.tvPhase.setText("Get in position");
        });

        binding.btnFlipCamera.setOnClickListener(v -> {
            if (cameraProcessor != null) {
                cameraProcessor.switchCamera();
                Toast.makeText(this,
                    cameraProcessor.isFrontFacing() ? "Front camera" : "Back camera",
                    Toast.LENGTH_SHORT).show();
            }
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
        exerciseAnalyzer = new ExerciseAnalyzer(currentExercise, this);
        cameraProcessor  = new CameraProcessor(this, this, binding.cameraPreview, exerciseAnalyzer);
        cameraProcessor.start();
        binding.tvStatus.setText("Position yourself in frame");
    }

    @Override
    public void onRepCompleted(boolean goodForm) {
        sessionReps++;
        float credit = CREDITS[currentExercise.ordinal()];
        sessionCredits += (int) credit;
        creditManager.addCredits((int) credit);
        String exKey = currentExercise == ExerciseAnalyzer.ExerciseType.PUSHUP  ? "push_up"
                     : currentExercise == ExerciseAnalyzer.ExerciseType.SQUAT   ? "squat"
                     : "crunch";
        int level = progressionStore.getProgressionLevel(exKey);
        long xp   = level * (long) Math.round(credit) * 2;
        progressionStore.logWorkout(new WorkoutLog(exKey, 1, 1, level, xp, goodForm));
        rankManager.syncCurrentUser();
        final int reps = sessionReps; final int creds = sessionCredits;
        mainHandler.post(() -> {
            binding.tvSessionReps.setText(String.valueOf(reps));
            binding.tvSessionCredits.setText(String.valueOf(creds));
            binding.tvTotalCredits.setText(String.valueOf(creditManager.getCredits()));
            binding.tvCreditToast.setText("+" + credit + " credits 🎬");
            binding.tvCreditToast.setVisibility(View.VISIBLE);
            binding.tvCreditToast.setAlpha(1f);
            binding.tvCreditToast.animate().alpha(0f).setStartDelay(900).setDuration(400)
                    .withEndAction(() -> binding.tvCreditToast.setVisibility(View.GONE)).start();
            binding.tvSessionReps.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                    .withEndAction(() -> binding.tvSessionReps.animate()
                            .scaleX(1f).scaleY(1f).setDuration(100).start()).start();
        });
    }

    @Override public void onPhaseChanged(String phase, double angle) {
        mainHandler.post(() -> binding.tvPhase.setText(phase));
    }

    @Override public void onFeedback(String msg) {
        mainHandler.post(() -> binding.tvStatus.setText(msg));
    }

    @Override
    public void onRequestPermissionsResult(int rc, @NonNull String[] perms,
                                            @NonNull int[] results) {
        super.onRequestPermissionsResult(rc, perms, results);
        if (rc == REQUEST_CAMERA && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else { Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show(); finish(); }
    }

    @Override protected void onPause() {
        super.onPause();
        if (cameraProcessor != null) cameraProcessor.stop();
    }

    @Override protected void onResume() {
        super.onResume();
        if (cameraProcessor != null) cameraProcessor.start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (cameraProcessor != null) cameraProcessor.stop();
    }
}
