package com.vaibhav.signbridgeai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.vaibhav.signbridgeai.util.AppPreferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings Activity providing accessibility configuration (fonts, high-contrast, theme, language).
 */
public class SettingsActivity extends AppCompatActivity {

    private AppPreferences preferences;

    private TextView tvLanguageSub;
    private TextView tvFontSizeSub;
    private TextView tvThemeSub;
    private TextView tvMicStatusSub;

    private MaterialSwitch switchHaptic;
    private MaterialSwitch switchKeepScreenOn;
    private MaterialSwitch switchAutoSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = new AppPreferences(this);

        initViews();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMicStatus();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvLanguageSub = findViewById(R.id.tvSettingLanguageSub);
        tvFontSizeSub = findViewById(R.id.tvSettingFontSizeSub);
        tvThemeSub = findViewById(R.id.tvSettingThemeSub);
        tvMicStatusSub = findViewById(R.id.tvSettingMicStatusSub);

        switchHaptic = findViewById(R.id.switchHaptic);
        switchKeepScreenOn = findViewById(R.id.switchKeepScreenOn);
        switchAutoSave = findViewById(R.id.switchAutoSave);

        findViewById(R.id.cardSettingLanguage).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.cardSettingFontSize).setOnClickListener(v -> showFontSizeDialog());
        findViewById(R.id.cardSettingTheme).setOnClickListener(v -> showThemeDialog());

        findViewById(R.id.cardSettingMicStatus).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Microphone permission is already granted", Toast.LENGTH_SHORT).show();
            }
        });

        switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setHapticFeedbackEnabled(isChecked);
        });

        switchKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setKeepScreenOnEnabled(isChecked);
        });

        switchAutoSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setAutoSaveEnabled(isChecked);
        });
    }

    private void updateUI() {
        // Language
        tvLanguageSub.setText(preferences.getLanguageDisplayName(this));

        // Font size
        int size = preferences.getCaptionFontSizeSp();
        tvFontSizeSub.setText(size + "sp (" + getFontSizeLabel(size) + ")");

        // Theme
        String theme = preferences.getThemeMode();
        switch (theme) {
            case AppPreferences.THEME_LIGHT:
                tvThemeSub.setText(R.string.theme_light);
                break;
            case AppPreferences.THEME_DARK:
                tvThemeSub.setText(R.string.theme_dark);
                break;
            case AppPreferences.THEME_HIGH_CONTRAST:
                tvThemeSub.setText(R.string.theme_high_contrast);
                break;
            default:
                tvThemeSub.setText(R.string.theme_system);
                break;
        }

        switchHaptic.setChecked(preferences.isHapticFeedbackEnabled());
        switchKeepScreenOn.setChecked(preferences.isKeepScreenOnEnabled());
        switchAutoSave.setChecked(preferences.isAutoSaveEnabled());

        updateMicStatus();
    }

    private String getFontSizeLabel(int sizeSp) {
        if (sizeSp <= 18) return "Regular";
        if (sizeSp <= 24) return "Large";
        if (sizeSp <= 30) return "Extra Large";
        return "Jumbo";
    }

    private void updateMicStatus() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            tvMicStatusSub.setText("Permission Granted (Ready to listen)");
            tvMicStatusSub.setTextColor(ContextCompat.getColor(this, R.color.status_ready));
        } else {
            tvMicStatusSub.setText("Permission Denied (Tap to open App Settings)");
            tvMicStatusSub.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        }
    }

    private void showLanguageDialog() {
        com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet sheet = com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet.newInstance(
                preferences.getLanguage(),
                selectedItem -> {
                    preferences.setLanguage(selectedItem.getCode());
                    updateUI();
                    Toast.makeText(this, "Language set to " + selectedItem.getFullDisplayName(), Toast.LENGTH_SHORT).show();
                }
        );
        sheet.show(getSupportFragmentManager(), "settings_lang_select");
    }

    private void showFontSizeDialog() {
        String[] options = {"Regular (18sp)", "Large (24sp) - Recommended", "Extra Large (30sp)", "Jumbo (38sp)"};
        int[] sizes = {18, 24, 30, 38};

        int current = preferences.getCaptionFontSizeSp();
        int selectedIndex = 1; // default 24
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] == current) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_fontsize_title)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    preferences.setCaptionFontSizeSp(sizes[which]);
                    updateUI();
                    dialog.dismiss();
                    Toast.makeText(this, "Caption font size updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showThemeDialog() {
        String[] options = {"System Default", "Light Mode (Deep Blue)", "Dark Mode (Deep Navy)", "High Contrast (Yellow on Black)"};
        String[] codes = {AppPreferences.THEME_SYSTEM, AppPreferences.THEME_LIGHT, AppPreferences.THEME_DARK, AppPreferences.THEME_HIGH_CONTRAST};

        String current = preferences.getThemeMode();
        int selectedIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme_title)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    String chosen = codes[which];
                    preferences.setThemeMode(chosen);
                    updateUI();
                    dialog.dismiss();

                    // Apply night mode
                    if (AppPreferences.THEME_LIGHT.equals(chosen)) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (AppPreferences.THEME_DARK.equals(chosen) || AppPreferences.THEME_HIGH_CONTRAST.equals(chosen)) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
