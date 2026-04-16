package com.pushgram.app.progression.data;

import com.pushgram.app.progression.model.Exercise;
import com.pushgram.app.progression.model.Exercise.Category;
import com.pushgram.app.progression.model.Exercise.MuscleGroup;
import com.pushgram.app.progression.model.Exercise.ProgressionLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete exercise library with muscle targeting and 5-level progression for each.
 *
 * XP per rep scales with difficulty:
 *   Difficulty 1 = 2 XP   Difficulty 2 = 4 XP   Difficulty 3 = 7 XP
 *   Difficulty 4 = 12 XP  Difficulty 5 = 20 XP
 */
public class ExerciseLibrary {

    private static ExerciseLibrary instance;
    private final Map<String, Exercise> exercises = new LinkedHashMap<>();

    private ExerciseLibrary() { buildLibrary(); }

    public static synchronized ExerciseLibrary getInstance() {
        if (instance == null) instance = new ExerciseLibrary();
        return instance;
    }

    public Exercise get(String id) { return exercises.get(id); }
    public List<Exercise> getAll() { return new ArrayList<>(exercises.values()); }

    public List<Exercise> getByCategory(Category category) {
        List<Exercise> result = new ArrayList<>();
        for (Exercise e : exercises.values())
            if (e.getCategory() == category) result.add(e);
        return result;
    }

    // ── Exercise Definitions ──────────────────────────────────────────

    private void buildLibrary() {
        add(buildPushUp());
        add(buildDiamondPushUp());
        add(buildPikePushUp());
        add(buildPlanche());
        add(buildPullUp());
        add(buildMuscleUp());
        add(buildAustrianPullUp());
        add(buildSquat());
        add(buildPistolSquat());
        add(buildLunge());
        add(buildHandstand());
        add(buildLSit());
        add(buildHollowBody());
        add(buildDip());
    }

    private void add(Exercise e) { exercises.put(e.getId(), e); }

    // ══════════════════════════════════════════════════════════════════
    // PUSH EXERCISES
    // ══════════════════════════════════════════════════════════════════

    private Exercise buildPushUp() {
        return new Exercise(
            "push_up",
            "Push-Up",
            "The king of bodyweight pressing. Builds chest, triceps and shoulder strength with zero equipment. Your foundation for all pushing progressions.",
            Category.PUSH, 2,
            Arrays.asList(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            Arrays.asList(MuscleGroup.FRONT_DELTS, MuscleGroup.SERRATUS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Wall Push-Up",
                    "Hands on wall, feet back. Perfect for beginners building initial strength.",
                    "Keep body straight like a plank. Chest touches wall.", 15, 3, 2),
                new ProgressionLevel(2, "Knee Push-Up",
                    "Knees on ground. Builds strength before full push-ups.",
                    "Hands shoulder-width. Lower chest to floor.", 12, 3, 3),
                new ProgressionLevel(3, "Standard Push-Up",
                    "Full push-up with perfect form. The benchmark.",
                    "Straight body. Full range — chest touches floor.", 15, 4, 4),
                new ProgressionLevel(4, "Archer Push-Up",
                    "One arm extends wide to the side while the other does the work.",
                    "Shift weight to working arm. Extended arm stays straight.", 8, 4, 7),
                new ProgressionLevel(5, "One-Arm Push-Up",
                    "Elite-level unilateral pressing strength.",
                    "Feet wide for balance. Core tight. Full depth.", 5, 3, 12)
            ),
            "None", "💪"
        );
    }

    private Exercise buildDiamondPushUp() {
        return new Exercise(
            "diamond_push_up",
            "Diamond Push-Up",
            "Hands form a diamond under your chest. Brutal tricep isolation and inner chest activation.",
            Category.PUSH, 3,
            Arrays.asList(MuscleGroup.TRICEPS, MuscleGroup.CHEST),
            Arrays.asList(MuscleGroup.FRONT_DELTS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.WRIST_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Wide Diamond (Knee)",
                    "Knees down, hands slightly closer than standard push-up.",
                    "Keep elbows from flaring too much.", 10, 3, 3),
                new ProgressionLevel(2, "Diamond Push-Up",
                    "Standard diamond position. Index fingers and thumbs touching.",
                    "Lower slowly. Elbows track back not out.", 10, 3, 5),
                new ProgressionLevel(3, "Elevated Diamond",
                    "Feet elevated on a chair. Increases load on triceps.",
                    "Form a strict diamond with your hands.", 8, 4, 7),
                new ProgressionLevel(4, "Close Grip Planche Lean",
                    "Lean forward in diamond position. Prepares for planche.",
                    "Protract scapula. Lean forward 30–45°.", 6, 4, 10),
                new ProgressionLevel(5, "Tricep Extension Push-Up",
                    "Hands close, lower forearms nearly to floor.",
                    "Control descent. Explosive push.", 5, 3, 13)
            ),
            "None", "💎"
        );
    }

    private Exercise buildPikePushUp() {
        return new Exercise(
            "pike_push_up",
            "Pike Push-Up",
            "Hips high, body in an inverted-V. Primary shoulder builder — the stepping stone to handstand push-ups.",
            Category.PUSH, 3,
            Arrays.asList(MuscleGroup.FRONT_DELTS, MuscleGroup.SIDE_DELTS),
            Arrays.asList(MuscleGroup.TRICEPS, MuscleGroup.UPPER_BACK),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Wide-Stance Pike",
                    "Feet wide, hips high. Head to floor.",
                    "Look back between feet. Straight arms at top.", 10, 3, 4),
                new ProgressionLevel(2, "Pike Push-Up",
                    "Standard pike — feet hip width, head nearly touches floor.",
                    "Drive through heels. Full lockout.", 8, 3, 6),
                new ProgressionLevel(3, "Elevated Pike",
                    "Feet on a chair. Closer to vertical press.",
                    "Greater vertical emphasis = more deltoid.", 8, 4, 8),
                new ProgressionLevel(4, "Decline Pike",
                    "Feet high on wall. Near handstand push-up difficulty.",
                    "Keep core braced. Controlled descent.", 6, 4, 11),
                new ProgressionLevel(5, "Wall Handstand Push-Up",
                    "Full handstand against wall. Pure overhead pressing.",
                    "Lock out hard. Head forward of hands slightly.", 5, 3, 15)
            ),
            "None", "🤸"
        );
    }

    private Exercise buildPlanche() {
        return new Exercise(
            "planche",
            "Planche",
            "The ultimate calisthenics skill. Body parallel to floor supported only by hands — requires extraordinary pushing strength, core tension and balance.",
            Category.PUSH, 5,
            Arrays.asList(MuscleGroup.FRONT_DELTS, MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            Arrays.asList(MuscleGroup.SERRATUS, MuscleGroup.SIDE_DELTS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.LOWER_BACK, MuscleGroup.SCAPULAR_STABILIZERS, MuscleGroup.WRIST_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Planche Lean",
                    "In push-up position, lean body forward over hands.",
                    "Protract scapula. Lean 20–30° forward. Hold 10–30s.", 1, 5, 8),
                new ProgressionLevel(2, "Tuck Planche",
                    "Knees tucked to chest, body horizontal. First true planche.",
                    "Round lower back. Push floor away. Aim for 5s hold.", 1, 5, 14),
                new ProgressionLevel(3, "Advanced Tuck Planche",
                    "Hips raised, back more horizontal. Harder tuck.",
                    "Flatten back progressively. Hold 5s.", 1, 5, 17),
                new ProgressionLevel(4, "Straddle Planche",
                    "Legs wide apart, body parallel. Near-full planche.",
                    "Squeeze glutes. Keep hips level. 3s hold.", 1, 5, 19),
                new ProgressionLevel(5, "Full Planche",
                    "Legs together, body fully parallel to floor. Elite skill.",
                    "Perfect protraction. Tight everything. 2s hold.", 1, 5, 20)
            ),
            "Parallettes (recommended)", "⚡"
        );
    }

    private Exercise buildDip() {
        return new Exercise(
            "dip",
            "Tricep Dip",
            "Vertical push compound. Lower body between bars for deep chest and tricep activation.",
            Category.PUSH, 3,
            Arrays.asList(MuscleGroup.TRICEPS, MuscleGroup.CHEST),
            Arrays.asList(MuscleGroup.FRONT_DELTS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Bench Dip",
                    "Hands on bench behind you, feet on floor.",
                    "Dip until elbows 90°. Keep back close to bench.", 12, 3, 3),
                new ProgressionLevel(2, "Parallel Bar Dip",
                    "Full dip on parallel bars. Bodyweight only.",
                    "Slight forward lean for chest. Straight body.", 10, 3, 5),
                new ProgressionLevel(3, "Ring Dip",
                    "Gymnastics rings add instability. Much harder.",
                    "Turn rings out at top. Control wobble.", 8, 4, 8),
                new ProgressionLevel(4, "Korean Dip",
                    "Hands behind hips on rings. Shoulder-destroying variation.",
                    "Elbows back. Extreme posterior shoulder stretch.", 6, 3, 12),
                new ProgressionLevel(5, "Weighted Dip",
                    "Added weight or progressing to ring support.",
                    "Slow negative. Full lockout.", 6, 4, 15)
            ),
            "Parallel bars / Chairs", "🏋️"
        );
    }

    // ══════════════════════════════════════════════════════════════════
    // PULL EXERCISES
    // ══════════════════════════════════════════════════════════════════

    private Exercise buildPullUp() {
        return new Exercise(
            "pull_up",
            "Pull-Up",
            "The ultimate back builder. Develops lats, biceps and grip strength. Foundation of all pulling progressions.",
            Category.PULL, 3,
            Arrays.asList(MuscleGroup.LATS, MuscleGroup.BICEPS),
            Arrays.asList(MuscleGroup.REAR_DELTS, MuscleGroup.UPPER_BACK, MuscleGroup.FOREARMS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Dead Hang",
                    "Simply hang from the bar. Builds grip and decompresses spine.",
                    "Shoulders packed down. Breathe. 30–60s holds.", 1, 5, 3),
                new ProgressionLevel(2, "Negative Pull-Up",
                    "Jump to top position, lower yourself slowly over 5 seconds.",
                    "3–5 second descent. Full elbow extension at bottom.", 5, 4, 5),
                new ProgressionLevel(3, "Standard Pull-Up",
                    "Full pull-up: chin over bar, full arm extension.",
                    "Drive elbows to hips. Chest to bar.", 8, 4, 7),
                new ProgressionLevel(4, "L-Sit Pull-Up",
                    "Legs straight out in front while pulling up.",
                    "Maintain L-sit throughout. Core devastation.", 5, 4, 11),
                new ProgressionLevel(5, "Archer Pull-Up",
                    "One arm does the work while the other extends straight.",
                    "Keep extended arm fully straight. Full ROM.", 4, 4, 14)
            ),
            "Pull-up bar", "🔝"
        );
    }

    private Exercise buildMuscleUp() {
        return new Exercise(
            "muscle_up",
            "Muscle-Up",
            "The holy grail of bar work. Combines a pull-up with a dip — going from below to above the bar in one explosive movement.",
            Category.PULL, 5,
            Arrays.asList(MuscleGroup.LATS, MuscleGroup.CHEST, MuscleGroup.TRICEPS),
            Arrays.asList(MuscleGroup.BICEPS, MuscleGroup.FRONT_DELTS, MuscleGroup.UPPER_BACK),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.FOREARMS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "High Pull-Up",
                    "Pull chest to bar — not just chin.",
                    "Explosive pull. Lean back slightly at top.", 5, 4, 8),
                new ProgressionLevel(2, "Chest-to-Bar Pull-Up",
                    "Pull until chest makes contact with bar every rep.",
                    "Full power pull. Straight legs.", 5, 4, 12),
                new ProgressionLevel(3, "Kipping Muscle-Up",
                    "Use leg swing for momentum. Learn the transition.",
                    "Hip snap, pull, transition fast. Don't hang at top.", 3, 4, 15),
                new ProgressionLevel(4, "Strict Muscle-Up",
                    "No kip. Pure strength. The real deal.",
                    "Slow through transition. Controlled dip down.", 3, 4, 18),
                new ProgressionLevel(5, "Ring Muscle-Up",
                    "Harder on rings. Rings turn out at top.",
                    "False grip. Explosive. Turn rings at transition.", 2, 3, 20)
            ),
            "Pull-up bar / Rings", "👑"
        );
    }

    private Exercise buildAustrianPullUp() {
        return new Exercise(
            "australian_pull_up",
            "Inverted Row",
            "Horizontal pull. Bar at waist height, body under it. Perfect bridge to full pull-ups.",
            Category.PULL, 2,
            Arrays.asList(MuscleGroup.UPPER_BACK, MuscleGroup.REAR_DELTS),
            Arrays.asList(MuscleGroup.BICEPS, MuscleGroup.LATS, MuscleGroup.FOREARMS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.GLUTES),
            Arrays.asList(
                new ProgressionLevel(1, "Incline Row",
                    "Body at 45°. Easier angle for beginners.",
                    "Pull chest to bar. Squeeze shoulder blades.", 12, 3, 2),
                new ProgressionLevel(2, "Flat Inverted Row",
                    "Body horizontal under waist-height bar.",
                    "Body perfectly straight. Pull chest to bar.", 10, 3, 4),
                new ProgressionLevel(3, "Feet-Elevated Row",
                    "Feet up on chair. Body below horizontal.",
                    "Harder than flat. Full scapular retraction.", 8, 4, 6),
                new ProgressionLevel(4, "Ring Row",
                    "Rings swing — adds instability.",
                    "Rings stay close to body. Turn out at top.", 8, 4, 8),
                new ProgressionLevel(5, "Archer Row",
                    "One arm rows while the other extends.",
                    "Extended arm fully straight. Slow and controlled.", 5, 4, 11)
            ),
            "Low bar / Table / Rings", "🏹"
        );
    }

    // ══════════════════════════════════════════════════════════════════
    // LEG EXERCISES
    // ══════════════════════════════════════════════════════════════════

    private Exercise buildSquat() {
        return new Exercise(
            "squat",
            "Squat",
            "The king of leg exercises. Full lower body development — quads, hamstrings, glutes. Foundation for all leg progressions.",
            Category.LEGS, 2,
            Arrays.asList(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            Arrays.asList(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.LOWER_BACK),
            Arrays.asList(
                new ProgressionLevel(1, "Box Squat",
                    "Squat to a chair or box. Builds confidence and depth.",
                    "Sit back to box. Pause. Drive through heels.", 15, 3, 2),
                new ProgressionLevel(2, "Bodyweight Squat",
                    "Standard squat. Thighs parallel to floor minimum.",
                    "Knees track toes. Chest up. Full depth.", 20, 3, 3),
                new ProgressionLevel(3, "Pause Squat",
                    "3-second pause at the bottom. Eliminates bounce.",
                    "Stay tight at bottom. Controlled drive up.", 12, 4, 5),
                new ProgressionLevel(4, "Bulgarian Split Squat",
                    "Rear foot elevated. Unilateral quad/glute torture.",
                    "Front knee doesn't pass toes. Deep stretch.", 8, 4, 7),
                new ProgressionLevel(5, "Shrimp Squat",
                    "Knee touches floor behind you. Single leg mastery.",
                    "Control descent. Drive knee back not out.", 5, 4, 10)
            ),
            "None", "🦵"
        );
    }

    private Exercise buildPistolSquat() {
        return new Exercise(
            "pistol_squat",
            "Pistol Squat",
            "Single-leg squat to full depth with the other leg extended forward. Elite leg strength, balance and mobility.",
            Category.LEGS, 5,
            Arrays.asList(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            Arrays.asList(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.HIP_FLEXORS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.LOWER_BACK),
            Arrays.asList(
                new ProgressionLevel(1, "Assisted Pistol",
                    "Hold a post or band for balance while descending.",
                    "Sit back. Extended leg straight. Touch bottom.", 5, 4, 8),
                new ProgressionLevel(2, "Box Pistol Squat",
                    "Squat to a low box on one leg.",
                    "Control descent to box. Full balance challenge.", 5, 4, 10),
                new ProgressionLevel(3, "Negative Pistol",
                    "Lower yourself slowly without assistance.",
                    "5-second descent. Step up to reset.", 4, 4, 13),
                new ProgressionLevel(4, "Full Pistol Squat",
                    "Complete unassisted pistol — all the way down.",
                    "Heel on floor. Extended leg level. Stand tall.", 5, 4, 16),
                new ProgressionLevel(5, "Weighted Pistol",
                    "Hold weight while performing pistol squat.",
                    "Perfect form maintained. Slow eccentric.", 4, 4, 20)
            ),
            "None", "🎯"
        );
    }

    private Exercise buildLunge() {
        return new Exercise(
            "lunge",
            "Lunge",
            "Unilateral leg exercise targeting quads and glutes. Great for fixing imbalances and building single-leg strength.",
            Category.LEGS, 2,
            Arrays.asList(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            Arrays.asList(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.HIP_FLEXORS),
            Arrays.asList(
                new ProgressionLevel(1, "Static Lunge",
                    "Stay in split stance. Just go up and down.",
                    "Back knee hovers 1cm off floor. Upright torso.", 12, 3, 2),
                new ProgressionLevel(2, "Walking Lunge",
                    "Step forward into each lunge alternating legs.",
                    "Long step. Knee at 90°. Drive through front heel.", 10, 3, 3),
                new ProgressionLevel(3, "Reverse Lunge",
                    "Step backward. More quad emphasis.",
                    "Step back far enough for 90° front knee.", 10, 4, 5),
                new ProgressionLevel(4, "Lateral Lunge",
                    "Step wide to the side. Hits inner thigh and glutes.",
                    "Keep stepped foot flat. Sink deep.", 8, 4, 6),
                new ProgressionLevel(5, "Jump Lunge",
                    "Explosive split jump switching legs.",
                    "Soft landing. Immediate next rep.", 8, 4, 9)
            ),
            "None", "🦿"
        );
    }

    // ══════════════════════════════════════════════════════════════════
    // SKILLS / STATIC HOLDS
    // ══════════════════════════════════════════════════════════════════

    private Exercise buildHandstand() {
        return new Exercise(
            "handstand",
            "Handstand",
            "Full body inversion skill. Requires shoulder strength, core tension and balance. Foundation for handstand push-ups and advanced presses.",
            Category.FULL_BODY, 4,
            Arrays.asList(MuscleGroup.FRONT_DELTS, MuscleGroup.SIDE_DELTS, MuscleGroup.TRICEPS),
            Arrays.asList(MuscleGroup.UPPER_BACK, MuscleGroup.SERRATUS),
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.GLUTES, MuscleGroup.WRIST_STABILIZERS, MuscleGroup.SCAPULAR_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Wall Plank",
                    "Hands on floor, feet on wall at hip height. Face-down.",
                    "Straight body. Push floor away. Hold 20–30s.", 1, 5, 5),
                new ProgressionLevel(2, "Wall Handstand",
                    "Kick up to wall. Back against wall. Face down.",
                    "Press through shoulders. Point toes. 20–30s hold.", 1, 5, 9),
                new ProgressionLevel(3, "Chest-to-Wall Handstand",
                    "Chest faces wall. Better alignment than back-to-wall.",
                    "Arms straight. Body hollow. Hold 15–30s.", 1, 5, 13),
                new ProgressionLevel(4, "Freestanding Kick-Up",
                    "Kick up and try to hold without wall.",
                    "Find balance point. Fingertip pressure. 3–5s.", 1, 5, 16),
                new ProgressionLevel(5, "Freestanding Handstand",
                    "Full freestanding handstand hold.",
                    "Hollow body. Eyes on floor. 10s+ hold.", 1, 5, 20)
            ),
            "Wall (for practice)", "🙃"
        );
    }

    private Exercise buildLSit() {
        return new Exercise(
            "l_sit",
            "L-Sit",
            "Body held in L-shape between parallel bars or floor. Extreme hip flexor and core strength. Foundation for advanced skills.",
            Category.CORE, 4,
            Arrays.asList(MuscleGroup.HIP_FLEXORS, MuscleGroup.ABS),
            Arrays.asList(MuscleGroup.TRICEPS, MuscleGroup.SERRATUS, MuscleGroup.QUADS),
            Arrays.asList(MuscleGroup.SCAPULAR_STABILIZERS, MuscleGroup.WRIST_STABILIZERS),
            Arrays.asList(
                new ProgressionLevel(1, "Tuck Hold",
                    "Knees tucked to chest between bars. Feet off floor.",
                    "Push bars down hard. Shoulders depressed. 5–10s.", 1, 5, 5),
                new ProgressionLevel(2, "One Leg Extended",
                    "One leg straight, one tucked.",
                    "Alternate legs. Hold 5s each side.", 1, 5, 8),
                new ProgressionLevel(3, "Full L-Sit",
                    "Both legs straight out in front. Classic L-Sit.",
                    "Toes pointed. Arms locked. Hold 10s.", 1, 5, 12),
                new ProgressionLevel(4, "L-Sit to V-Sit",
                    "Raise legs above horizontal — V-sit.",
                    "Compress hard. Straight legs above parallel.", 1, 4, 16),
                new ProgressionLevel(5, "L-Sit Pull-Up",
                    "Maintain L-sit while doing pull-ups.",
                    "Don't lose L-position throughout pull.", 3, 4, 20)
            ),
            "Parallel bars / Chairs / Floor", "🎖️"
        );
    }

    private Exercise buildHollowBody() {
        return new Exercise(
            "hollow_body",
            "Hollow Body",
            "Foundation of gymnastics core strength. Lower back pressed to floor, body in a dish shape. Essential for handstands and planche.",
            Category.CORE, 2,
            Arrays.asList(MuscleGroup.ABS, MuscleGroup.HIP_FLEXORS),
            Arrays.asList(MuscleGroup.OBLIQUES, MuscleGroup.SERRATUS),
            Collections.emptyList(),
            Arrays.asList(
                new ProgressionLevel(1, "Dead Bug",
                    "Back flat, knees to chest, arms up. Lower one leg.",
                    "Lower back must not leave floor. Ever.", 8, 3, 2),
                new ProgressionLevel(2, "Tuck Hollow Hold",
                    "Knees tucked, arms overhead, shoulders off floor.",
                    "Ribs down. Lower back flat. Hold 20s.", 1, 5, 3),
                new ProgressionLevel(3, "Hollow Body Hold",
                    "Legs straight and low, arms overhead. Full hollow.",
                    "Chin tucked. Point toes. Hold 30s.", 1, 5, 5),
                new ProgressionLevel(4, "Hollow Body Rock",
                    "Rock forward and back maintaining perfect hollow.",
                    "Shape doesn't change during rocking. 10 rocks.", 10, 4, 7),
                new ProgressionLevel(5, "Hollow Body + Weighted",
                    "Hold weight overhead while maintaining hollow.",
                    "Extra challenge on obliques. Hold 20s.", 1, 4, 10)
            ),
            "None", "🌟"
        );
    }
}
