package com.pushgram.app.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

/**
 * Fix #3 + #4 — unified analyzer for push-ups, squats, and crunches.
 * Relaxed form thresholds so reps register reliably during normal movement.
 */
public class ExerciseAnalyzer {

    public enum ExerciseType { PUSHUP, SQUAT, CRUNCH }

    public interface RepListener {
        void onRepCompleted(boolean goodForm);
        void onPhaseChanged(String phase, double angle);
        void onFeedback(String msg);
    }

    private ExerciseType type;
    private final RepListener listener;
    private boolean wasDown = false;
    private String currentPhase = "UNKNOWN";

    // Fix #4: relaxed thresholds — original was too strict (150°/90°)
    // Push-up: arm goes from ~160° (up) to ~70° (down)
    private static final double PU_UP   = 145.0;
    private static final double PU_DOWN = 80.0;
    // Squat: hip/knee angle from ~170° (standing) to ~100° (squatting)
    private static final double SQ_UP   = 160.0;
    private static final double SQ_DOWN = 110.0;
    // Crunch: hip flexion from ~170° (flat) to ~120° (crunched)
    private static final double CR_UP   = 155.0;
    private static final double CR_DOWN = 120.0;

    public ExerciseAnalyzer(ExerciseType type, RepListener listener) {
        this.type = type;
        this.listener = listener;
    }

    public void setExerciseType(ExerciseType type) {
        this.type = type;
        wasDown = false;
        currentPhase = "UNKNOWN";
    }

    public void analyze(Pose pose) {
        if (pose == null) return;
        switch (type) {
            case PUSHUP:  analyzePushup(pose);  break;
            case SQUAT:   analyzeSquat(pose);   break;
            case CRUNCH:  analyzeCrunch(pose);  break;
        }
    }

    // ── Push-up: tracks average elbow angle ────────────────────────────────
    private void analyzePushup(Pose pose) {
        PoseLandmark ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark le = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark re = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark lw = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rw = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        if (ls==null||rs==null||le==null||re==null||lw==null||rw==null) {
            listener.onFeedback("Show full upper body"); return;
        }
        double avg = (angle(ls,le,lw) + angle(rs,re,rw)) / 2.0;
        listener.onPhaseChanged(String.format("%.0f°", avg), avg);
        countRep(avg, PU_UP, PU_DOWN, "Push-up");
    }

    // ── Squat: tracks average knee angle ───────────────────────────────────
    private void analyzeSquat(Pose pose) {
        PoseLandmark lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark lk = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rk = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark la = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark ra = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        if (lh==null||rh==null||lk==null||rk==null||la==null||ra==null) {
            listener.onFeedback("Show full lower body"); return;
        }
        double avg = (angle(lh,lk,la) + angle(rh,rk,ra)) / 2.0;
        listener.onPhaseChanged(String.format("%.0f°", avg), avg);
        countRep(avg, SQ_UP, SQ_DOWN, "Squat");
    }

    // ── Crunch: tracks hip flexion angle ───────────────────────────────────
    private void analyzeCrunch(Pose pose) {
        PoseLandmark ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark lk = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rk = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        if (ls==null||rs==null||lh==null||rh==null||lk==null||rk==null) {
            listener.onFeedback("Show full torso and legs"); return;
        }
        double avg = (angle(ls,lh,lk) + angle(rs,rh,rk)) / 2.0;
        listener.onPhaseChanged(String.format("%.0f°", avg), avg);
        countRep(avg, CR_UP, CR_DOWN, "Crunch");
    }

    // ── Shared rep counting logic ───────────────────────────────────────────
    private void countRep(double angle, double upThresh, double downThresh, String name) {
        if (angle < downThresh) {
            if (!wasDown) {
                wasDown = true;
                currentPhase = "DOWN";
                listener.onFeedback("Good — now " + (name.equals("Crunch") ? "extend" : "push up") + "!");
            }
        } else if (angle > upThresh && wasDown) {
            wasDown = false;
            currentPhase = "UP";
            listener.onRepCompleted(true);
            listener.onFeedback("Rep counted! ✓");
        } else if (!wasDown) {
            listener.onFeedback(name + " — get lower");
        }
    }

    private double angle(PoseLandmark a, PoseLandmark b, PoseLandmark c) {
        double ax = a.getPosition().x - b.getPosition().x;
        double ay = a.getPosition().y - b.getPosition().y;
        double cx = c.getPosition().x - b.getPosition().x;
        double cy = c.getPosition().y - b.getPosition().y;
        double dot = ax*cx + ay*cy;
        double mag = Math.sqrt(ax*ax+ay*ay) * Math.sqrt(cx*cx+cy*cy);
        if (mag == 0) return 180;
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot/mag))));
    }

    public void reset() { wasDown = false; currentPhase = "UNKNOWN"; }
}
