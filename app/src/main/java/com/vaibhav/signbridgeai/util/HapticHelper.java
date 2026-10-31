package com.vaibhav.signbridgeai.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Provides gentle tactile vibrations to inform deaf and hard-of-hearing users
 * about recognition state transitions (listening started, voice detected, paused).
 */
public class HapticHelper {

    private final Vibrator vibrator;
    private final AppPreferences preferences;

    public HapticHelper(Context context) {
        this.preferences = new AppPreferences(context);
        Context appCtx = context.getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) appCtx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
        } else {
            this.vibrator = (Vibrator) appCtx.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public void vibrateStart() {
        if (!preferences.isHapticFeedbackEnabled() || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrate(new long[]{0, 50, 60, 50}, -1);
    }

    public void vibrateVoiceDetected() {
        if (!preferences.isHapticFeedbackEnabled() || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrate(new long[]{0, 40}, -1);
    }

    public void vibratePause() {
        if (!preferences.isHapticFeedbackEnabled() || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrate(new long[]{0, 80}, -1);
    }

    public void vibrateError() {
        if (!preferences.isHapticFeedbackEnabled() || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        vibrate(new long[]{0, 100, 100, 100}, -1);
    }

    private void vibrate(long[] timings, int repeat) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, repeat));
            } else {
                vibrator.vibrate(timings, repeat);
            }
        } catch (Exception ignored) {
        }
    }
}
