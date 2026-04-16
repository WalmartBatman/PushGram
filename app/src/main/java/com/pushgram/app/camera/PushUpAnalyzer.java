package com.pushgram.app.camera;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PushUpAnalyzer {

    private static final double ANGLE_UP   = 150.0;
    private static final double ANGLE_DOWN = 90.0;

    public enum Phase { UNKNOWN, UP, DOWN }

    public interface PushUpListener {
        void onRepCompleted(boolean isPerfectForm);
        void onPhaseChanged(Phase phase, double elbowAngle);
        void onFormFeedback(String feedback);
    }

    private Phase currentPhase = Phase.UNKNOWN;
    private boolean wasDown = false;
    private final PushUpListener listener;

    public PushUpAnalyzer(PushUpListener listener) { this.listener = listener; }

    public void analyze(Pose pose) {
        if (pose == null) return;

        PoseLandmark ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark le = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark re = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark lw = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rw = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark la = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark ra = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        if (ls == null || rs == null || le == null || re == null || lw == null || rw == null) {
            listener.onFormFeedback("Position your full body in frame");
            return;
        }

        double avgElbow = (angle(ls, le, lw) + angle(rs, re, rw)) / 2.0;
        boolean goodForm = true;

        if (lh != null && rh != null && la != null && ra != null) {
            if (!isBodyAligned(ls, rs, lh, rh, la, ra)) {
                goodForm = false;
                listener.onFormFeedback("Keep your body straight!");
            }
        }
        if (!checkElbows(ls, rs, le, re)) {
            goodForm = false;
            listener.onFormFeedback("Keep elbows closer to body");
        }
        if (goodForm) listener.onFormFeedback("Great form! Keep going!");

        Phase newPhase = avgElbow > ANGLE_UP ? Phase.UP
                       : avgElbow < ANGLE_DOWN ? Phase.DOWN
                       : Phase.UNKNOWN;

        listener.onPhaseChanged(newPhase, avgElbow);

        if (newPhase == Phase.DOWN && currentPhase != Phase.DOWN) {
            wasDown = true; currentPhase = Phase.DOWN;
        } else if (newPhase == Phase.UP && wasDown) {
            wasDown = false; currentPhase = Phase.UP;
            listener.onRepCompleted(goodForm);
        } else if (newPhase != Phase.UNKNOWN) {
            currentPhase = newPhase;
        }
    }

    private double angle(PoseLandmark a, PoseLandmark b, PoseLandmark c) {
        double ax = a.getPosition().x - b.getPosition().x;
        double ay = a.getPosition().y - b.getPosition().y;
        double cx = c.getPosition().x - b.getPosition().x;
        double cy = c.getPosition().y - b.getPosition().y;
        double dot = ax*cx + ay*cy;
        double mag = Math.sqrt(ax*ax+ay*ay) * Math.sqrt(cx*cx+cy*cy);
        if (mag == 0) return 0;
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot/mag))));
    }

    private boolean isBodyAligned(PoseLandmark ls, PoseLandmark rs,
                                   PoseLandmark lh, PoseLandmark rh,
                                   PoseLandmark la, PoseLandmark ra) {
        float sy = (ls.getPosition().y + rs.getPosition().y) / 2f;
        float hy = (lh.getPosition().y + rh.getPosition().y) / 2f;
        float ay = (la.getPosition().y + ra.getPosition().y) / 2f;
        float sx = (ls.getPosition().x + rs.getPosition().x) / 2f;
        float hx = (lh.getPosition().x + rh.getPosition().x) / 2f;
        float ax = (la.getPosition().x + ra.getPosition().x) / 2f;
        double vx = sx-ax, vy = sy-ay;
        double hvx = hx-ax, hvy = hy-ay;
        double len = Math.sqrt(vx*vx+vy*vy);
        if (len == 0) return true;
        double cross = Math.abs(hvx*vy - hvy*vx);
        return cross < len * 0.2;
    }

    private boolean checkElbows(PoseLandmark ls, PoseLandmark rs,
                                  PoseLandmark le, PoseLandmark re) {
        float sw = Math.abs(rs.getPosition().x - ls.getPosition().x);
        float ew = Math.abs(re.getPosition().x - le.getPosition().x);
        return ew <= sw * 1.8f;
    }

    public void reset() { currentPhase = Phase.UNKNOWN; wasDown = false; }
}
