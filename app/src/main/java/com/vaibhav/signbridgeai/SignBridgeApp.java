package com.vaibhav.signbridgeai;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import com.vaibhav.signbridgeai.util.AppPreferences;

/**
 * Custom Application class for SignBridge AI.
 * Initializes default theme and preferences.
 */
public class SignBridgeApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        applyAppTheme();
    }

    public void applyAppTheme() {
        AppPreferences prefs = new AppPreferences(this);
        String theme = prefs.getThemeMode();

        switch (theme) {
            case AppPreferences.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case AppPreferences.THEME_DARK:
            case AppPreferences.THEME_HIGH_CONTRAST:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
