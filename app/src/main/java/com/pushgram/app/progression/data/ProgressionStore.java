package com.pushgram.app.progression.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pushgram.app.progression.model.UserProfile;
import com.pushgram.app.progression.model.WorkoutLog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists all progression data locally via SharedPreferences + Gson.
 * Tracks: current progression level per exercise, workout history, XP, user profile.
 */
public class ProgressionStore {

    private static final String PREFS = "pushgram_progression";
    private static final String KEY_PROFILE     = "user_profile";
    private static final String KEY_LEVELS      = "exercise_levels";    // Map<exerciseId, level>
    private static final String KEY_BEST_REPS   = "best_reps";          // Map<exerciseId, int>
    private static final String KEY_LOGS        = "workout_logs";       // List<WorkoutLog>
    private static final int    MAX_LOGS        = 500;

    private static ProgressionStore instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private ProgressionStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // Init profile if first launch
        if (getProfile() == null) {
            UserProfile profile = new UserProfile(
                    UUID.randomUUID().toString(), "Athlete");
            saveProfile(profile);
        }
    }

    public static synchronized ProgressionStore getInstance(Context ctx) {
        if (instance == null) instance = new ProgressionStore(ctx);
        return instance;
    }

    // ── User Profile ──────────────────────────────────────────────────

    public UserProfile getProfile() {
        String json = prefs.getString(KEY_PROFILE, null);
        if (json == null) return null;
        return gson.fromJson(json, UserProfile.class);
    }

    public void saveProfile(UserProfile profile) {
        prefs.edit().putString(KEY_PROFILE, gson.toJson(profile)).apply();
    }

    public void setDisplayName(String name) {
        UserProfile p = getProfile();
        if (p != null) { p.setDisplayName(name); saveProfile(p); }
    }

    // ── XP & Ranking ─────────────────────────────────────────────────

    public void addXp(long xp) {
        UserProfile p = getProfile();
        if (p != null) { p.addXp(xp); saveProfile(p); }
    }

    public long getTotalXp() {
        UserProfile p = getProfile();
        return p != null ? p.getTotalXp() : 0;
    }

    // ── Progression Levels ────────────────────────────────────────────

    public int getProgressionLevel(String exerciseId) {
        Map<String, Integer> levels = getLevelsMap();
        return levels.getOrDefault(exerciseId, 1);
    }

    public void setProgressionLevel(String exerciseId, int level) {
        Map<String, Integer> levels = getLevelsMap();
        levels.put(exerciseId, level);
        prefs.edit().putString(KEY_LEVELS, gson.toJson(levels)).apply();
    }

    public void advanceProgressionLevel(String exerciseId) {
        int current = getProgressionLevel(exerciseId);
        if (current < 5) setProgressionLevel(exerciseId, current + 1);
    }

    private Map<String, Integer> getLevelsMap() {
        String json = prefs.getString(KEY_LEVELS, null);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        Map<String, Integer> map = gson.fromJson(json, type);
        return map != null ? map : new HashMap<>();
    }

    // ── Best Reps ─────────────────────────────────────────────────────

    public int getBestReps(String exerciseId) {
        Map<String, Integer> best = getBestRepsMap();
        return best.getOrDefault(exerciseId, 0);
    }

    public void updateBestReps(String exerciseId, int reps) {
        Map<String, Integer> best = getBestRepsMap();
        int current = best.getOrDefault(exerciseId, 0);
        if (reps > current) {
            best.put(exerciseId, reps);
            prefs.edit().putString(KEY_BEST_REPS, gson.toJson(best)).apply();
        }
    }

    private Map<String, Integer> getBestRepsMap() {
        String json = prefs.getString(KEY_BEST_REPS, null);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        Map<String, Integer> map = gson.fromJson(json, type);
        return map != null ? map : new HashMap<>();
    }

    // ── Workout Logs ──────────────────────────────────────────────────

    public void logWorkout(WorkoutLog log) {
        List<WorkoutLog> logs = getLogs();
        logs.add(0, log); // newest first
        if (logs.size() > MAX_LOGS) logs = logs.subList(0, MAX_LOGS);

        // Update profile stats
        UserProfile p = getProfile();
        if (p != null) {
            p.addXp(log.getXpEarned());
            p.addReps(log.getReps() * log.getSets());
            // Update best reps
            String eid = log.getExerciseId();
            updateBestReps(eid, log.getReps());
            saveProfile(p);
        }

        prefs.edit().putString(KEY_LOGS, gson.toJson(logs)).apply();
    }

    public List<WorkoutLog> getLogs() {
        String json = prefs.getString(KEY_LOGS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<WorkoutLog>>() {}.getType();
        List<WorkoutLog> logs = gson.fromJson(json, type);
        return logs != null ? logs : new ArrayList<>();
    }

    public List<WorkoutLog> getLogsForExercise(String exerciseId) {
        List<WorkoutLog> all = getLogs();
        List<WorkoutLog> result = new ArrayList<>();
        for (WorkoutLog log : all)
            if (exerciseId.equals(log.getExerciseId())) result.add(log);
        return result;
    }

    public int getTotalRepsForExercise(String exerciseId) {
        int total = 0;
        for (WorkoutLog log : getLogsForExercise(exerciseId))
            total += log.getReps() * log.getSets();
        return total;
    }

    // ── Battle Records ────────────────────────────────────────────────

    public void recordBattleResult(boolean won, long bonusXp) {
        UserProfile p = getProfile();
        if (p != null) {
            p.recordBattleResult(won);
            if (won) p.addXp(bonusXp);
            saveProfile(p);
        }
    }
}
