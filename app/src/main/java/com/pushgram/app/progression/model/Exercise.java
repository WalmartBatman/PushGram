package com.pushgram.app.progression.model;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a calisthenic exercise with full muscle targeting info
 * and a 5-stage progression path.
 */
public class Exercise implements Serializable {

    public enum Category {
        PUSH("Push"),
        PULL("Pull"),
        LEGS("Legs"),
        CORE("Core"),
        FULL_BODY("Full Body");

        public final String label;
        Category(String label) { this.label = label; }
    }

    public enum MuscleGroup {
        // Upper Push
        CHEST("Chest", "💪"),
        FRONT_DELTS("Front Delts", "💪"),
        SIDE_DELTS("Side Delts", "💪"),
        TRICEPS("Triceps", "💪"),

        // Upper Pull
        LATS("Lats", "🔙"),
        BICEPS("Biceps", "💪"),
        REAR_DELTS("Rear Delts", "🔙"),
        UPPER_BACK("Upper Back", "🔙"),
        FOREARMS("Forearms", "💪"),

        // Core
        ABS("Abs", "🎯"),
        OBLIQUES("Obliques", "🎯"),
        LOWER_BACK("Lower Back", "🎯"),
        SERRATUS("Serratus", "🎯"),

        // Legs
        QUADS("Quads", "🦵"),
        HAMSTRINGS("Hamstrings", "🦵"),
        GLUTES("Glutes", "🦵"),
        CALVES("Calves", "🦵"),
        HIP_FLEXORS("Hip Flexors", "🦵"),

        // Stabilizers
        WRIST_STABILIZERS("Wrist Stabilizers", "✋"),
        SCAPULAR_STABILIZERS("Scapular Stabilizers", "🔙");

        public final String name;
        public final String emoji;
        MuscleGroup(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }
    }

    public static class ProgressionLevel implements Serializable {
        public final int level;           // 1-5
        public final String name;
        public final String description;
        public final String formCue;
        public final int targetReps;
        public final int targetSets;
        public final int xpPerRep;

        public ProgressionLevel(int level, String name, String description,
                                 String formCue, int targetReps, int targetSets, int xpPerRep) {
            this.level = level;
            this.name = name;
            this.description = description;
            this.formCue = formCue;
            this.targetReps = targetReps;
            this.targetSets = targetSets;
            this.xpPerRep = xpPerRep;
        }

        public String getGoalText() {
            return targetSets + " × " + targetReps + " reps";
        }
    }

    // ── Fields ────────────────────────────────────────────────────────
    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final int difficulty;               // 1–5
    private final List<MuscleGroup> primaryMuscles;
    private final List<MuscleGroup> secondaryMuscles;
    private final List<MuscleGroup> stabilizers;
    private final List<ProgressionLevel> progressionLevels;
    private final String equipment;             // "None", "Pull-up bar", etc.
    private final String emoji;

    public Exercise(String id, String name, String description, Category category,
                    int difficulty, List<MuscleGroup> primary,
                    List<MuscleGroup> secondary, List<MuscleGroup> stabilizers,
                    List<ProgressionLevel> levels, String equipment, String emoji) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.primaryMuscles = primary;
        this.secondaryMuscles = secondary;
        this.stabilizers = stabilizers;
        this.progressionLevels = levels;
        this.equipment = equipment;
        this.emoji = emoji;
    }

    // ── Getters ───────────────────────────────────────────────────────
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getDifficulty() { return difficulty; }
    public List<MuscleGroup> getPrimaryMuscles() { return primaryMuscles; }
    public List<MuscleGroup> getSecondaryMuscles() { return secondaryMuscles; }
    public List<MuscleGroup> getStabilizers() { return stabilizers; }
    public List<ProgressionLevel> getProgressionLevels() { return progressionLevels; }
    public String getEquipment() { return equipment; }
    public String getEmoji() { return emoji; }

    public String getDifficultyStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < difficulty ? "★" : "☆");
        return sb.toString();
    }

    public ProgressionLevel getLevel(int level) {
        for (ProgressionLevel pl : progressionLevels) {
            if (pl.level == level) return pl;
        }
        return progressionLevels.get(0);
    }
}
