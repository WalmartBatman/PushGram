package com.pushgram.app.progression.model;

public class WorkoutLog {
    private String exerciseId;
    private int reps;
    private int sets;
    private int progressionLevel;
    private long timestampMs;
    private long xpEarned;
    private boolean isPerfectForm;

    public WorkoutLog() {}

    public WorkoutLog(String exerciseId, int reps, int sets,
                       int progressionLevel, long xpEarned, boolean isPerfectForm) {
        this.exerciseId = exerciseId;
        this.reps = reps;
        this.sets = sets;
        this.progressionLevel = progressionLevel;
        this.xpEarned = xpEarned;
        this.isPerfectForm = isPerfectForm;
        this.timestampMs = System.currentTimeMillis();
    }

    public String getExerciseId() { return exerciseId; }
    public int getReps() { return reps; }
    public int getSets() { return sets; }
    public int getProgressionLevel() { return progressionLevel; }
    public long getTimestampMs() { return timestampMs; }
    public long getXpEarned() { return xpEarned; }
    public boolean isPerfectForm() { return isPerfectForm; }

    public void setExerciseId(String id) { this.exerciseId = id; }
    public void setReps(int r) { this.reps = r; }
    public void setSets(int s) { this.sets = s; }
    public void setProgressionLevel(int l) { this.progressionLevel = l; }
    public void setTimestampMs(long t) { this.timestampMs = t; }
    public void setXpEarned(long xp) { this.xpEarned = xp; }
    public void setPerfectForm(boolean pf) { this.isPerfectForm = pf; }
}
