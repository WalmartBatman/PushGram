package com.pushgram.app.rank.model;

public class LeaderboardEntry {
    private int rank;
    private String userId;
    private String displayName;
    private long totalXp;
    private int totalReps;
    private int battlesWon;
    private boolean isCurrentUser;
    private RankTier tier;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String userId, String displayName, long totalXp,
                             int totalReps, int battlesWon) {
        this.userId = userId;
        this.displayName = displayName;
        this.totalXp = totalXp;
        this.totalReps = totalReps;
        this.battlesWon = battlesWon;
        this.tier = RankTier.fromXp(totalXp);
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getUserId() { return userId; }
    public void setUserId(String id) { this.userId = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String name) { this.displayName = name; }
    public long getTotalXp() { return totalXp; }
    public void setTotalXp(long xp) { this.totalXp = xp; this.tier = RankTier.fromXp(xp); }
    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int r) { this.totalReps = r; }
    public int getBattlesWon() { return battlesWon; }
    public void setBattlesWon(int w) { this.battlesWon = w; }
    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean b) { this.isCurrentUser = b; }
    public RankTier getTier() { return tier != null ? tier : RankTier.fromXp(totalXp); }

    public String getRankMedal() {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "#" + rank;
        }
    }
}
