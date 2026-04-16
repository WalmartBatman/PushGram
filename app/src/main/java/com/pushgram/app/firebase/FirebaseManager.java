package com.pushgram.app.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.UserProfile;
import com.pushgram.app.rank.model.LeaderboardEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Manages Firebase Realtime Database for:
 *  1. Global leaderboard (top players worldwide)
 *  2. Multiplayer battle rooms (real-time rep counting)
 *
 * ── Setup ────────────────────────────────────────────────────────────
 *  1. Create a Firebase project at console.firebase.google.com
 *  2. Add an Android app with package name "com.pushgram.app"
 *  3. Download google-services.json and place it in the app/ folder
 *  4. Enable Anonymous Authentication in Firebase Console
 *  5. Enable Realtime Database (start in test mode for development)
 *
 * The app gracefully degrades to local-only mode if Firebase is unavailable.
 */
public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    // Realtime DB paths
    private static final String PATH_LEADERBOARD = "leaderboard";
    private static final String PATH_BATTLES     = "battles";

    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private DatabaseReference db;
    private boolean isAvailable = false;

    private FirebaseManager() {
        try {
            auth = FirebaseAuth.getInstance();
            db   = FirebaseDatabase.getInstance().getReference();
            isAvailable = true;
            signInAnonymously();
        } catch (Exception e) {
            Log.w(TAG, "Firebase not configured — running in local-only mode");
        }
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    public boolean isAvailable() { return isAvailable; }

    // ── Auth ──────────────────────────────────────────────────────────

    private void signInAnonymously() {
        if (auth.getCurrentUser() != null) return;
        auth.signInAnonymously()
                .addOnSuccessListener(r -> Log.d(TAG, "Firebase anon sign-in OK"))
                .addOnFailureListener(e -> Log.w(TAG, "Firebase sign-in failed: " + e.getMessage()));
    }

    public String getFirebaseUid() {
        FirebaseUser user = auth != null ? auth.getCurrentUser() : null;
        return user != null ? user.getUid() : null;
    }

    // ── Global Leaderboard ────────────────────────────────────────────

    public void pushToGlobalLeaderboard(UserProfile profile) {
        if (profile == null) return;
        if (!isAvailable || getFirebaseUid() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("displayName", profile.getDisplayName());
        data.put("totalXp", profile.getTotalXp());
        data.put("totalReps", profile.getTotalReps());
        data.put("battlesWon", profile.getBattlesWon());
        data.put("updatedAt", System.currentTimeMillis());

        db.child(PATH_LEADERBOARD).child(getFirebaseUid()).setValue(data)
                .addOnFailureListener(e -> Log.w(TAG, "Leaderboard push failed: " + e));
    }

    public interface LeaderboardCallback {
        void onLoaded(List<LeaderboardEntry> entries);
        void onError(String message);
    }

    public void fetchGlobalLeaderboard(String currentUserId, LeaderboardCallback callback) {
        if (!isAvailable) {
            callback.onError("Firebase not configured");
            return;
        }

        db.child(PATH_LEADERBOARD)
                .orderByChild("totalXp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<LeaderboardEntry> entries = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            try {
                                String uid = child.getKey();
                                String name = child.child("displayName").getValue(String.class);
                                Long xp    = child.child("totalXp").getValue(Long.class);
                                Integer reps = child.child("totalReps").getValue(Integer.class);
                                Integer bw = child.child("battlesWon").getValue(Integer.class);

                                LeaderboardEntry e = new LeaderboardEntry(
                                        uid,
                                        name != null ? name : "Unknown",
                                        xp != null ? xp : 0,
                                        reps != null ? reps : 0,
                                        bw != null ? bw : 0);
                                e.setCurrentUser(uid.equals(currentUserId));
                                entries.add(e);
                            } catch (Exception ex) {
                                Log.w(TAG, "Parse error: " + ex.getMessage());
                            }
                        }
                        // Sort descending by XP
                        Collections.sort(entries, (a, b) ->
                                Long.compare(b.getTotalXp(), a.getTotalXp()));
                        for (int i = 0; i < entries.size(); i++)
                            entries.get(i).setRank(i + 1);

                        callback.onLoaded(entries);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ── Battle Rooms ──────────────────────────────────────────────────

    public static class BattleRoom {
        public String roomCode;
        public String player1Id;
        public String player1Name;
        public int player1Reps;
        public String player2Id;
        public String player2Name;
        public int player2Reps;
        public String exerciseId;
        public int durationSeconds;
        public String status; // "waiting", "countdown", "active", "finished"
        public long startTimeMs;
        public String winnerId;
    }

    public interface BattleCallback {
        void onRoomCreated(String roomCode);
        void onRoomJoined(BattleRoom room);
        void onRoomUpdated(BattleRoom room);
        void onError(String message);
    }

    /** Generate a 6-char room code */
    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public void createBattleRoom(String playerId, String playerName,
                                  String exerciseId, int durationSeconds,
                                  BattleCallback callback) {
        if (!isAvailable) { callback.onError("Firebase not available"); return; }

        String code = generateRoomCode();
        Map<String, Object> room = new HashMap<>();
        room.put("roomCode", code);
        room.put("player1Id", playerId);
        room.put("player1Name", playerName);
        room.put("player1Reps", 0);
        room.put("exerciseId", exerciseId);
        room.put("durationSeconds", durationSeconds);
        room.put("status", "waiting");
        room.put("startTimeMs", 0);

        db.child(PATH_BATTLES).child(code).setValue(room)
                .addOnSuccessListener(v -> callback.onRoomCreated(code))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void joinBattleRoom(String roomCode, String playerId, String playerName,
                                BattleCallback callback) {
        if (!isAvailable) { callback.onError("Firebase not available"); return; }

        DatabaseReference ref = db.child(PATH_BATTLES).child(roomCode);
        ref.child("player2Id").setValue(playerId);
        ref.child("player2Name").setValue(playerName);
        ref.child("player2Reps").setValue(0);
        ref.child("status").setValue("countdown")
                .addOnSuccessListener(v -> listenToBattleRoom(roomCode, callback))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void listenToBattleRoom(String roomCode, BattleCallback callback) {
        if (!isAvailable) return;
        db.child(PATH_BATTLES).child(roomCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                try {
                    BattleRoom room = new BattleRoom();
                    room.roomCode     = roomCode;
                    room.player1Id    = snap.child("player1Id").getValue(String.class);
                    room.player1Name  = snap.child("player1Name").getValue(String.class);
                    room.player2Id    = snap.child("player2Id").getValue(String.class);
                    room.player2Name  = snap.child("player2Name").getValue(String.class);
                    room.exerciseId   = snap.child("exerciseId").getValue(String.class);
                    room.status       = snap.child("status").getValue(String.class);
                    room.winnerId     = snap.child("winnerId").getValue(String.class);

                    Integer p1r = snap.child("player1Reps").getValue(Integer.class);
                    Integer p2r = snap.child("player2Reps").getValue(Integer.class);
                    Long dur    = snap.child("durationSeconds").getValue(Long.class);
                    Long start  = snap.child("startTimeMs").getValue(Long.class);
                    room.player1Reps    = p1r != null ? p1r : 0;
                    room.player2Reps    = p2r != null ? p2r : 0;
                    room.durationSeconds = dur != null ? dur.intValue() : 60;
                    room.startTimeMs    = start != null ? start : 0;

                    callback.onRoomUpdated(room);
                } catch (Exception e) {
                    Log.w(TAG, "Room parse error: " + e);
                }
            }
            @Override
            public void onCancelled(DatabaseError e) { callback.onError(e.getMessage()); }
        });
    }

    public void updateBattleReps(String roomCode, boolean isPlayer1, int reps) {
        if (!isAvailable) return;
        String field = isPlayer1 ? "player1Reps" : "player2Reps";
        db.child(PATH_BATTLES).child(roomCode).child(field).setValue(reps);
    }

    public void startBattle(String roomCode) {
        if (!isAvailable) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "active");
        updates.put("startTimeMs", System.currentTimeMillis());
        db.child(PATH_BATTLES).child(roomCode).updateChildren(updates);
    }

    public void finishBattle(String roomCode, String winnerId) {
        if (!isAvailable) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "finished");
        updates.put("winnerId", winnerId);
        db.child(PATH_BATTLES).child(roomCode).updateChildren(updates);
    }

    public void cleanupBattleRoom(String roomCode) {
        if (!isAvailable || roomCode == null) return;
        db.child(PATH_BATTLES).child(roomCode).removeValue();
    }
}
