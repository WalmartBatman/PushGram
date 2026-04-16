package com.pushgram.app.rank.model;

public enum RankTier {
    ROOKIE   ("Rookie",   "🥉", 0,      499,    "#8B6914", 0),
    BRONZE   ("Bronze",   "🥉", 500,    1499,   "#CD7F32", 1),
    SILVER   ("Silver",   "🥈", 1500,   3999,   "#C0C0C0", 2),
    GOLD     ("Gold",     "🥇", 4000,   9999,   "#FFD700", 3),
    PLATINUM ("Platinum", "💎", 10000,  24999,  "#E5E4E2", 4),
    DIAMOND  ("Diamond",  "💠", 25000,  59999,  "#B9F2FF", 5),
    LEGEND   ("Legend",   "👑", 60000,  Long.MAX_VALUE, "#FF6B35", 6);

    public final String name;
    public final String emoji;
    public final long minXp;
    public final long maxXp;
    public final String colorHex;
    public final int tier;  // 0 = lowest

    RankTier(String name, String emoji, long minXp, long maxXp, String colorHex, int tier) {
        this.name = name;
        this.emoji = emoji;
        this.minXp = minXp;
        this.maxXp = maxXp;
        this.colorHex = colorHex;
        this.tier = tier;
    }

    public static RankTier fromXp(long xp) {
        for (RankTier rt : values()) {
            if (xp >= rt.minXp && xp <= rt.maxXp) return rt;
        }
        return LEGEND;
    }

    /** Progress within this tier 0.0–1.0 */
    public float progressInTier(long xp) {
        if (maxXp == Long.MAX_VALUE) return 1f;
        long range = maxXp - minXp;
        long progress = xp - minXp;
        return Math.min(1f, Math.max(0f, (float) progress / range));
    }

    public long xpToNextTier(long xp) {
        if (maxXp == Long.MAX_VALUE) return 0;
        return Math.max(0, maxXp + 1 - xp);
    }

    public String getDisplayName() { return emoji + " " + name; }
}
