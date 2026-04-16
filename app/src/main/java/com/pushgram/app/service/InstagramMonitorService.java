package com.pushgram.app.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.pushgram.app.model.CreditManager;
import com.pushgram.app.ui.BlockerActivity;

public class InstagramMonitorService extends AccessibilityService {
    private static final String TAG = "InstagramMonitor";
    private static final String INSTAGRAM_PKG = "com.instagram.android";
    private boolean wasInReels = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !pkg.toString().equals(INSTAGRAM_PKG)) return;
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            CharSequence cls = event.getClassName();
            if (cls == null) return;
            String lower = cls.toString().toLowerCase();
            boolean inReels = lower.contains("reels") || lower.contains("clips");
            if (inReels && !wasInReels) {
                Log.d(TAG, "User entered Reels");
                if (!CreditManager.getInstance(this).hasCredits()) {
                    Intent intent = new Intent(this, BlockerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    CreditManager.getInstance(this).spendCredit();
                }
                wasInReels = true;
            } else if (!inReels) {
                wasInReels = false;
            }
        }
    }

    @Override public void onInterrupt() {}
    @Override protected void onServiceConnected() { super.onServiceConnected(); }
}
