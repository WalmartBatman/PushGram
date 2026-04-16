package com.pushgram.app.progression.model;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private String userId;          // local UUID or Firebase UID
    private String displayName;
    private long totalXp;
    private int totalReps;
    private int battlesWon;
    private int battlesPlayed;
    private long lastActiveMs;

    // Per-exercise best reps (stored as JSON map elsewhere)
    private int pushUpBest;
    private int pullUpBest;
    private int squatBest;
    private int plancheBest;
    private int handstandBestSecs;

    public UserProfile() {}

    public UserProfile(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
        this.totalXp = 0;
        this.lastActiveMs = System.currentTimeMillis();
    }

    public void addXp(long xp) {
        this.totalXp += xp;
        this.lastActiveMs = System.currentTimeMillis();
    }

    public void addReps(int reps) {
        this.totalReps += reps;
    }

    public void recordBattleResult(boolean won) {
        battlesPlayed++;
        if (won) battlesWon++;
    }

    public double getBattleWinRate() {
        if (battlesPlayed == 0) return 0;
        return (double) battlesWon / battlesPlayed * 100;
    }

    // Getters / Setters
    public String getUserId() { return userId; }
    public void setUserId(String id) { this.userId = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String name) { this.displayName = name; }
    public long getTotalXp() { return totalXp; }
    public void setTotalXp(long xp) { this.totalXp = xp; }
    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int r) { this.totalReps = r; }
    public int getBattlesWon() { return battlesWon; }
    public int getBattlesPlayed() { return battlesPlayed; }
    public void setBattlesWon(int w) { this.battlesWon = w; }
    public void setBattlesPlayed(int p) { this.battlesPlayed = p; }
    public long getLastActiveMs() { return lastActiveMs; }
    public void setLastActiveMs(long ms) { this.lastActiveMs = ms; }
    public int getPushUpBest() { return pushUpBest; }
    public void setPushUpBest(int v) { pushUpBest = Math.max(pushUpBest, v); }
    public int getPullUpBest() { return pullUpBest; }
    public void setPullUpBest(int v) { pullUpBest = Math.max(pullUpBest, v); }
    public int getSquatBest() { return squatBest; }
    public void setSquatBest(int v) { squatBest = Math.max(squatBest, v); }
    public int getPlancheBest() { return plancheBest; }
    public void setPlancheBest(int v) { plancheBest = Math.max(plancheBest, v); }
    public int getHandstandBestSecs() { return handstandBestSecs; }
    public void setHandstandBestSecs(int v) { handstandBestSecs = Math.max(handstandBestSecs, v); }
}
