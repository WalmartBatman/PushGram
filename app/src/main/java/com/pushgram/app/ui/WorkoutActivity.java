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
import com.pushgram.app.camera.PushUpAnalyzer;
import com.pushgram.app.databinding.ActivityWorkoutBinding;
import com.pushgram.app.model.CreditManager;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.WorkoutLog;
import com.pushgram.app.rank.RankManager;

public class WorkoutActivity extends AppCompatActivity
        implements PushUpAnalyzer.PushUpListener {

    private static final int REQUEST_CAMERA = 100;
    private ActivityWorkoutBinding binding;
    private CameraProcessor cameraProcessor;
    private CreditManager creditManager;
    private ProgressionStore progressionStore;
    private RankManager rankManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int sessionReps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        creditManager    = CreditManager.getInstance(this);
        progressionStore = ProgressionStore.getInstance(this);
        rankManager      = RankManager.getInstance(this);
        binding.btnBack.setOnClickListener(v -> finish());

        // Change #5 — camera flip button, always visible during workout
        binding.btnFlipCamera.setOnClickListener(v -> {
            if (cameraProcessor != null) {
                cameraProcessor.switchCamera();
                boolean isFront = cameraProcessor.isFrontFacing();
                Toast.makeText(this,
                    isFront ? "Switched to front camera" : "Switched to back camera",
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
        cameraProcessor = new CameraProcessor(this, this, binding.cameraPreview, this);
        cameraProcessor.start();
        binding.tvStatus.setText("Position yourself for push-ups");
    }

    @Override
    public void onRepCompleted(boolean isPerfectForm) {
        if (isPerfectForm) {
            sessionReps++;
            creditManager.addCredit();

            // Award XP for push-up
            int level = progressionStore.getProgressionLevel("push_up");
            long xp = level * 4L; // scales with progression level
            WorkoutLog log = new WorkoutLog("push_up", 1, 1, level, xp, true);
            progressionStore.logWorkout(log);
            rankManager.syncCurrentUser();

            mainHandler.post(() -> {
                binding.tvSessionReps.setText(String.valueOf(sessionReps));
                binding.tvTotalCredits.setText(String.valueOf(creditManager.getCredits()));
                binding.tvSessionCredits.setText(String.valueOf(sessionReps));
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
        } else {
            mainHandler.post(() -> binding.tvStatus.setText("Fix your form for credits!"));
        }
    }

    @Override
    public void onPhaseChanged(PushUpAnalyzer.Phase phase, double angle) {
        mainHandler.post(() -> {
            String a = String.format("%.0f°", angle);
            switch (phase) {
                case UP:
                    binding.tvPhase.setText("UP " + a);
                    binding.tvPhase.setTextColor(0xFF4CAF50); break;
                case DOWN:
                    binding.tvPhase.setText("DOWN " + a);
                    binding.tvPhase.setTextColor(0xFF4FC3F7); break;
                default:
                    binding.tvPhase.setText("→ " + a);
                    binding.tvPhase.setTextColor(0xFFFFFFFF);
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
