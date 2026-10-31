package com.vaibhav.signbridgeai.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.vaibhav.signbridgeai.model.LanguageItem;

/**
 * Manages user settings, accessibility preferences, and language selection.
 */
public class AppPreferences {

    private static final String PREF_NAME = "signbridge_preferences";

    public static final String KEY_LANGUAGE = "pref_language";
    public static final String KEY_FONT_SIZE = "pref_font_size";
    public static final String KEY_THEME = "pref_theme";
    public static final String KEY_HAPTIC = "pref_haptic";
    public static final String KEY_KEEP_SCREEN_ON = "pref_keep_screen_on";
    public static final String KEY_AUTO_SAVE = "pref_auto_save";

    // Supported Locales
    public static final String LANG_EN_US = "en-US";
    public static final String LANG_EN_IN = "en-IN";
    public static final String LANG_HI_IN = "hi-IN";
    public static final String LANG_MR_IN = "mr-IN";

    // Theme options
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_HIGH_CONTRAST = "high_contrast";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANG_EN_US);
    }

    public void setLanguage(String langCode) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    public String getLanguageDisplayName(Context context) {
        LanguageItem item = LanguageHelper.getLanguageByCode(getLanguage());
        return item.getFullDisplayName();
    }

    public String getLanguageShortBadge() {
        LanguageItem item = LanguageHelper.getLanguageByCode(getLanguage());
        return item.getShortBadge();
    }

    public int getCaptionFontSizeSp() {
        // Default 24sp for high legibility
        return prefs.getInt(KEY_FONT_SIZE, 24);
    }

    public void setCaptionFontSizeSp(int sizeSp) {
        // Bounds checking: 16sp to 42sp
        int clamped = Math.max(16, Math.min(sizeSp, 42));
        prefs.edit().putInt(KEY_FONT_SIZE, clamped).apply();
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME, THEME_SYSTEM);
    }

    public void setThemeMode(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean isHighContrast() {
        return THEME_HIGH_CONTRAST.equals(getThemeMode());
    }

    public boolean isHapticFeedbackEnabled() {
        return prefs.getBoolean(KEY_HAPTIC, true);
    }

    public void setHapticFeedbackEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply();
    }

    public boolean isKeepScreenOnEnabled() {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, true);
    }

    public void setKeepScreenOnEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply();
    }

    public boolean isAutoSaveEnabled() {
        return prefs.getBoolean(KEY_AUTO_SAVE, true);
    }

    public void setAutoSaveEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply();
    }
}
