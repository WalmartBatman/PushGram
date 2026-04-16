package com.pushgram.app.battle.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pushgram.app.databinding.ActivityBattleLobbyBinding;
import com.pushgram.app.firebase.FirebaseManager;
import com.pushgram.app.progression.data.ExerciseLibrary;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.Exercise;
import com.pushgram.app.progression.model.UserProfile;

import java.util.List;

public class BattleLobbyActivity extends AppCompatActivity {

    private ActivityBattleLobbyBinding binding;
    private FirebaseManager firebase;
    private ProgressionStore store;
    private String selectedExerciseId = "push_up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBattleLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebase = FirebaseManager.getInstance();
        store    = ProgressionStore.getInstance(this);

        binding.btnBack.setOnClickListener(v -> finish());

        setupExercisePicker();
        setupCreateJoin();

        if (!firebase.isAvailable()) {
            binding.tvFirebaseWarning.setVisibility(View.VISIBLE);
            binding.btnCreate.setEnabled(false);
            binding.btnJoin.setEnabled(false);
        }
    }

    private void setupExercisePicker() {
        List<Exercise> exercises = ExerciseLibrary.getInstance().getAll();
        String[] names = exercises.stream().map(e -> e.getEmoji() + " " + e.getName())
                .toArray(String[]::new);
        String[] ids   = exercises.stream().map(Exercise::getId).toArray(String[]::new);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerExercise.setAdapter(adapter);

        binding.spinnerExercise.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                selectedExerciseId = ids[pos];
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    private void setupCreateJoin() {
        // Create room
        binding.btnCreate.setOnClickListener(v -> {
            UserProfile profile = store.getProfile();
            if (profile == null) return;

            binding.progressBattle.setVisibility(View.VISIBLE);
            binding.btnCreate.setEnabled(false);

            firebase.createBattleRoom(
                    profile.getUserId(),
                    profile.getDisplayName(),
                    selectedExerciseId,
                    60,
                    new FirebaseManager.BattleCallback() {
                        @Override public void onRoomCreated(String roomCode) {
                            runOnUiThread(() -> {
                                binding.progressBattle.setVisibility(View.GONE);
                                binding.btnCreate.setEnabled(true);
                                // Show share code UI
                                binding.tvShareCode.setText("Share this code with your opponent:\n\n" + roomCode);
                                binding.tvShareCode.setVisibility(View.VISIBLE);
                                // Launch battle as player 1
                                launchBattle(roomCode, true);
                            });
                        }
                        @Override public void onRoomJoined(FirebaseManager.BattleRoom room) {}
                        @Override public void onRoomUpdated(FirebaseManager.BattleRoom room) {}
                        @Override public void onError(String message) {
                            runOnUiThread(() -> {
                                binding.progressBattle.setVisibility(View.GONE);
                                binding.btnCreate.setEnabled(true);
                                Toast.makeText(BattleLobbyActivity.this,
                                        "Error: " + message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

        // Join room
        binding.btnJoin.setOnClickListener(v -> {
            String code = binding.etRoomCode.getText().toString().trim().toUpperCase();
            if (code.length() != 6) {
                Toast.makeText(this, "Enter a 6-character room code", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile profile = store.getProfile();
            if (profile == null) return;

            binding.progressBattle.setVisibility(View.VISIBLE);
            binding.btnJoin.setEnabled(false);

            firebase.joinBattleRoom(code,
                    profile.getUserId(),
                    profile.getDisplayName(),
                    new FirebaseManager.BattleCallback() {
                        @Override public void onRoomCreated(String c) {}
                        @Override public void onRoomJoined(FirebaseManager.BattleRoom room) {}
                        @Override public void onRoomUpdated(FirebaseManager.BattleRoom room) {
                            runOnUiThread(() -> {
                                binding.progressBattle.setVisibility(View.GONE);
                                binding.btnJoin.setEnabled(true);
                                launchBattle(code, false);
                            });
                        }
                        @Override public void onError(String message) {
                            runOnUiThread(() -> {
                                binding.progressBattle.setVisibility(View.GONE);
                                binding.btnJoin.setEnabled(true);
                                Toast.makeText(BattleLobbyActivity.this,
                                        "Room not found or error: " + message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });
    }

    private void launchBattle(String roomCode, boolean isPlayer1) {
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("room_code", roomCode);
        intent.putExtra("is_player1", isPlayer1);
        startActivity(intent);
    }
}
