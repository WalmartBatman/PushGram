package com.pushgram.app.battle.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pushgram.app.camera.CameraProcessor;
import com.pushgram.app.camera.ExerciseAnalyzer;
import com.pushgram.app.databinding.ActivityBattleBinding;
import com.pushgram.app.firebase.FirebaseManager;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.UserProfile;
import com.pushgram.app.rank.RankManager;

public class BattleActivity extends AppCompatActivity implements ExerciseAnalyzer.RepListener {

    private static final int BATTLE_XP_WIN  = 500;
    private static final int BATTLE_XP_LOSE = 100;

    private ActivityBattleBinding binding;
    private FirebaseManager firebase;
    private ProgressionStore store;
    private RankManager rankManager;
    private CameraProcessor cameraProcessor;

    private String roomCode;
    private boolean isPlayer1;
    private UserProfile myProfile;
    private String opponentId = "";   // FIX #5: store real opponent UID
    private int myReps = 0;
    private int opponentReps = 0;
    private CountDownTimer battleTimer;
    private CountDownTimer countdownTimer;  // FIX #4: save anonymous timer so it can be cancelled
    private boolean battleActive = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBattleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebase    = FirebaseManager.getInstance();
        store       = ProgressionStore.getInstance(this);
        rankManager = RankManager.getInstance(this);
        myProfile   = store.getProfile();

        roomCode  = getIntent().getStringExtra("room_code");
        isPlayer1 = getIntent().getBooleanExtra("is_player1", true);

        binding.btnBack.setOnClickListener(v -> confirmExit());
        binding.tvMyName.setText(myProfile != null ? myProfile.getDisplayName() : "You");
        binding.tvRoomCode.setText("Room: " + roomCode);
        binding.tvMyReps.setText("0");
        binding.tvOpponentReps.setText("0");

        startCamera();
        listenToRoom();

        if (isPlayer1) {
            binding.tvBattleStatus.setText("Waiting for opponent to join...\nShare code: " + roomCode);
        } else {
            binding.tvBattleStatus.setText("Joined! Starting soon...");
        }
    }

    // ── Camera ────────────────────────────────────────────────────────

    private void startCamera() {
        ExerciseAnalyzer analyzer = new ExerciseAnalyzer(ExerciseAnalyzer.ExerciseType.PUSHUP, this);
        cameraProcessor = new CameraProcessor(this, this, binding.cameraPreview, analyzer);
        cameraProcessor.start();
    }

    // ── Firebase ──────────────────────────────────────────────────────

    private void listenToRoom() {
        firebase.listenToBattleRoom(roomCode, new FirebaseManager.BattleCallback() {
            @Override public void onRoomCreated(String code) {}
            @Override public void onRoomJoined(FirebaseManager.BattleRoom room) {}

            @Override
            public void onRoomUpdated(FirebaseManager.BattleRoom room) {
                runOnUiThread(() -> handleRoomUpdate(room));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                    Toast.makeText(BattleActivity.this, "Connection error: " + message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleRoomUpdate(FirebaseManager.BattleRoom room) {
        // FIX #5: capture the real opponent UID on first update
        if (isPlayer1 && room.player2Id != null && !room.player2Id.isEmpty()) {
            opponentId = room.player2Id;
        } else if (!isPlayer1 && room.player1Id != null && !room.player1Id.isEmpty()) {
            opponentId = room.player1Id;
        }

        // Update opponent display name
        String opponentName = isPlayer1 ? room.player2Name : room.player1Name;
        if (opponentName != null && !opponentName.isEmpty()) {
            binding.tvOpponentName.setText(opponentName);
        } else {
            binding.tvOpponentName.setText("Waiting...");
        }

        // Update opponent reps
        opponentReps = isPlayer1 ? room.player2Reps : room.player1Reps;
        binding.tvOpponentReps.setText(String.valueOf(opponentReps));

        // React to status changes
        if ("countdown".equals(room.status) && !battleActive) {
            if (isPlayer1) {
                startCountdown();
            }
        } else if ("active".equals(room.status) && !battleActive) {
            beginBattle(room.durationSeconds);
        } else if ("finished".equals(room.status)) {
            showResult(room.winnerId);
        }
    }

    // FIX #4: save the timer reference so it can be cancelled in onDestroy
    private void startCountdown() {
        binding.tvBattleStatus.setText("Get ready!");
        countdownTimer = new CountDownTimer(3000, 1000) {
            @Override public void onTick(long ms) {
                binding.tvCountdown.setText(String.valueOf(ms / 1000 + 1));
                binding.tvCountdown.setVisibility(View.VISIBLE);
            }
            @Override public void onFinish() {
                binding.tvCountdown.setVisibility(View.GONE);
                firebase.startBattle(roomCode);
            }
        };
        countdownTimer.start();
    }

    private void beginBattle(int durationSecs) {
        battleActive = true;
        binding.tvBattleStatus.setText("FIGHT! Do as many reps as possible!");
        binding.tvFormFeedback.setText("GO! GO! GO!");

        battleTimer = new CountDownTimer((long) durationSecs * 1000, 1000) {
            @Override public void onTick(long ms) {
                int secsLeft = (int)(ms / 1000);
                binding.tvTimer.setText(secsLeft + "s");
                if (secsLeft <= 10) {
                    binding.tvTimer.setTextColor(0xFFFF4444);
                }
            }
            @Override public void onFinish() {
                battleActive = false;
                binding.tvBattleStatus.setText("Time's up!");
                binding.tvFormFeedback.setText("Waiting for result...");
                // FIX #5: use real opponentId, not the string literal "opponent"
                if (isPlayer1 && myProfile != null) {
                    String winnerId = myReps >= opponentReps
                            ? myProfile.getUserId()
                            : opponentId;
                    firebase.finishBattle(roomCode, winnerId);
                }
            }
        };
        battleTimer.start();
    }

    private void showResult(String winnerId) {
        if (winnerId == null) return;
        boolean iWon = myProfile != null && winnerId.equals(myProfile.getUserId());

        long xp = iWon ? BATTLE_XP_WIN : BATTLE_XP_LOSE;
        store.recordBattleResult(iWon, xp);
        rankManager.syncCurrentUser();

        String title = iWon ? "🏆 YOU WIN!" : "💪 Good Fight!";
        String msg = (iWon ? "You win with " : "You got ") + myReps + " reps vs " + opponentReps + " reps.\n"
                + "+" + xp + " XP earned!";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Done", (d, w) -> finish())
                .show();
    }

    // ── ExerciseAnalyzer.RepListener ──────────────────────────────────

    @Override
    public void onRepCompleted(boolean goodForm) {
        if (!battleActive) return;
        myReps++;
        mainHandler.post(() -> {
            binding.tvMyReps.setText(String.valueOf(myReps));
            binding.tvMyReps.animate()
                    .scaleX(1.4f).scaleY(1.4f).setDuration(100)
                    .withEndAction(() -> binding.tvMyReps.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();
        });
        if (myReps % 3 == 0 || myReps == 1) {
            firebase.updateBattleReps(roomCode, isPlayer1, myReps);
        }
    }

    @Override
    public void onPhaseChanged(String phase, double angle) {
        mainHandler.post(() -> binding.tvPhase.setText(
                "DOWN".equals(phase) ? "⬇ " + (int)angle + "°" :
                "UP".equals(phase)   ? "⬆ " + (int)angle + "°" : "→"));
    }

    @Override
    public void onFeedback(String feedback) {
        if (battleActive) mainHandler.post(() -> binding.tvFormFeedback.setText(feedback));
    }

    // ── Lifecycle ─────────────────────────────────────────────────────

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Battle?")
                .setMessage("Leaving will forfeit the battle.")
                .setPositiveButton("Leave", (d, w) -> {
                    firebase.cleanupBattleRoom(roomCode);
                    finish();
                })
                .setNegativeButton("Stay", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProcessor != null) cameraProcessor.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraProcessor != null) cameraProcessor.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) countdownTimer.cancel();  // FIX #4
        if (battleTimer != null) battleTimer.cancel();
        if (cameraProcessor != null) cameraProcessor.stop();
    }
}
