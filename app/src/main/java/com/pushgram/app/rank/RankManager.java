package com.pushgram.app.rank;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pushgram.app.progression.data.ProgressionStore;
import com.pushgram.app.progression.model.UserProfile;
import com.pushgram.app.rank.model.LeaderboardEntry;
import com.pushgram.app.rank.model.RankTier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages local leaderboard (multiple profiles on same device)
 * and rank tier calculations.
 */
public class RankManager {

    private static final String PREFS = "pushgram_leaderboard";
    private static final String KEY_LOCAL_BOARD = "local_board";

    private static RankManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final ProgressionStore store;

    private RankManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        store = ProgressionStore.getInstance(ctx);
    }

    public static synchronized RankManager getInstance(Context ctx) {
        if (instance == null) instance = new RankManager(ctx);
        return instance;
    }

    // ── Rank Tier ─────────────────────────────────────────────────────

    public RankTier getCurrentRank() {
        return RankTier.fromXp(store.getTotalXp());
    }

    public RankTier getRankForXp(long xp) {
        return RankTier.fromXp(xp);
    }

    public long getXpToNextRank() {
        RankTier current = getCurrentRank();
        return current.xpToNextTier(store.getTotalXp());
    }

    public float getRankProgress() {
        RankTier current = getCurrentRank();
        return current.progressInTier(store.getTotalXp());
    }

    // ── Local Leaderboard ─────────────────────────────────────────────

    /**
     * Sync the current user's stats into the local leaderboard.
     * Call this after any XP-earning event.
     */
    public void syncCurrentUser() {
        UserProfile profile = store.getProfile();
        if (profile == null) return;

        List<LeaderboardEntry> board = getLocalBoard();

        // Find existing entry or create new
        LeaderboardEntry existing = null;
        for (LeaderboardEntry e : board) {
            if (e.getUserId().equals(profile.getUserId())) {
                existing = e;
                break;
            }
        }

        if (existing != null) {
            existing.setTotalXp(profile.getTotalXp());
            existing.setTotalReps(profile.getTotalReps());
            existing.setBattlesWon(profile.getBattlesWon());
        } else {
            board.add(new LeaderboardEntry(
                    profile.getUserId(),
                    profile.getDisplayName(),
                    profile.getTotalXp(),
                    profile.getTotalReps(),
                    profile.getBattlesWon()));
        }

        saveLocalBoard(board);
    }

    public List<LeaderboardEntry> getLocalBoard() {
        String json = prefs.getString(KEY_LOCAL_BOARD, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<LeaderboardEntry>>() {}.getType();
        List<LeaderboardEntry> board = gson.fromJson(json, type);
        return board != null ? board : new ArrayList<>();
    }

    public List<LeaderboardEntry> getRankedLocalBoard() {
        List<LeaderboardEntry> board = getLocalBoard();
        // Sort by XP descending
        Collections.sort(board, (a, b) -> Long.compare(b.getTotalXp(), a.getTotalXp()));

        String currentUserId = "";
        UserProfile p = store.getProfile();
        if (p != null) currentUserId = p.getUserId();

        for (int i = 0; i < board.size(); i++) {
            board.get(i).setRank(i + 1);
            board.get(i).setCurrentUser(board.get(i).getUserId().equals(currentUserId));
        }
        return board;
    }

    private void saveLocalBoard(List<LeaderboardEntry> board) {
        prefs.edit().putString(KEY_LOCAL_BOARD, gson.toJson(board)).apply();
    }

    /** Add a demo opponent (for testing leaderboard appearance) */
    public void addDemoEntries() {
        List<LeaderboardEntry> board = getLocalBoard();
        if (board.size() > 1) return; // already has entries

        String[][] demos = {
            {"demo_1", "IronMike",    "8420",  "2100", "12"},
            {"demo_2", "FitQueen",    "5300",  "1450", "8"},
            {"demo_3", "GymRat99",    "3100",  "980",  "5"},
            {"demo_4", "PushUpKing",  "1800",  "550",  "3"},
            {"demo_5", "GainzMode",   "750",   "300",  "1"},
        };
        for (String[] d : demos) {
            board.add(new LeaderboardEntry(d[0], d[1],
                    Long.parseLong(d[2]), Integer.parseInt(d[3]), Integer.parseInt(d[4])));
        }
        saveLocalBoard(board);
    }
}
