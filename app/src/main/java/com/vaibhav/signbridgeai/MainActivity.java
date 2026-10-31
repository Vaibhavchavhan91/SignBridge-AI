package com.vaibhav.signbridgeai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.adapter.SessionAdapter;
import com.vaibhav.signbridgeai.db.SessionRepository;
import com.vaibhav.signbridgeai.model.ConversationSession;
import com.vaibhav.signbridgeai.util.AppPreferences;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * Main Home Activity for SignBridge AI.
 * Provides manual listening control, language selection, usage stats, and recent history.
 */
public class MainActivity extends AppCompatActivity implements SessionAdapter.OnSessionClickListener {

    private AppPreferences preferences;
    private SessionRepository sessionRepository;
    private SessionAdapter recentSessionAdapter;

    private TextView tvCurrentLanguagePill;
    private TextView tvStatSessionsCount;
    private TextView tvStatCaptionsCount;
    private TextView tvEmptyRecent;
    private RecyclerView rvRecentSessions;
    private ChipGroup chipGroupLanguages;

    private final ActivityResultLauncher<String> requestMicPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchLiveCaptions();
                } else {
                    Toast.makeText(this, R.string.captions_error_mic_perm, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new AppPreferences(this);
        sessionRepository = new SessionRepository(this);

        initViews();
        setupLanguageSelection();
        setupNavigation();
        setupRecentRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsAndRecent();
    }

    private TextView tvCurrentFlagEmoji;

    private void initViews() {
        tvCurrentFlagEmoji = findViewById(R.id.tvCurrentFlagEmoji);
        tvCurrentLanguagePill = findViewById(R.id.tvCurrentLanguagePill);
        tvStatSessionsCount = findViewById(R.id.tvStatSessionsCount);
        tvStatCaptionsCount = findViewById(R.id.tvStatCaptionsCount);
        tvEmptyRecent = findViewById(R.id.tvEmptyRecent);
        rvRecentSessions = findViewById(R.id.rvRecentSessions);
        chipGroupLanguages = findViewById(R.id.chipGroupLanguages);

        // Start listening button (Card + Big Mic)
        View.OnClickListener startListener = v -> checkPermissionAndStart();
        findViewById(R.id.cardHeroStart).setOnClickListener(startListener);
        findViewById(R.id.btnBigMic).setOnClickListener(startListener);

        // Open full country/language selector
        View.OnClickListener openLangSelector = v -> openCountryLanguageBottomSheet();
        findViewById(R.id.cardSelectCountryLanguage).setOnClickListener(openLangSelector);
        findViewById(R.id.btnChangeLanguage).setOnClickListener(openLangSelector);

        findViewById(R.id.tvViewAllHistory).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void setupLanguageSelection() {
        String currentLang = preferences.getLanguage();
        updateLanguageDisplay(currentLang);

        syncChipSelection(currentLang);

        chipGroupLanguages.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipLangMore) {
                openCountryLanguageBottomSheet();
                return;
            }

            String selectedLang = "en-US";
            if (checkedId == R.id.chipLangInHi) {
                selectedLang = "hi-IN";
            } else if (checkedId == R.id.chipLangInMr) {
                selectedLang = "mr-IN";
            } else if (checkedId == R.id.chipLangGb) {
                selectedLang = "en-GB";
            } else if (checkedId == R.id.chipLangEs) {
                selectedLang = "es-ES";
            } else if (checkedId == R.id.chipLangFr) {
                selectedLang = "fr-FR";
            } else if (checkedId == R.id.chipLangEn) {
                selectedLang = "en-US";
            }

            preferences.setLanguage(selectedLang);
            updateLanguageDisplay(selectedLang);
        });
    }

    private void syncChipSelection(String langCode) {
        if ("hi-IN".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangInHi);
        } else if ("mr-IN".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangInMr);
        } else if ("en-GB".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangGb);
        } else if ("es-ES".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangEs);
        } else if ("fr-FR".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangFr);
        } else if ("en-US".equalsIgnoreCase(langCode)) {
            chipGroupLanguages.check(R.id.chipLangEn);
        } else {
            chipGroupLanguages.clearCheck();
        }
    }

    private void updateLanguageDisplay(String langCode) {
        com.vaibhav.signbridgeai.model.LanguageItem item = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode(langCode);
        if (tvCurrentFlagEmoji != null) {
            tvCurrentFlagEmoji.setText(item.getFlagEmoji());
        }
        if (tvCurrentLanguagePill != null) {
            tvCurrentLanguagePill.setText(item.getFlagEmoji() + " " + item.getCountryName() + " • " + item.getLanguageName());
        }
    }

    private void openCountryLanguageBottomSheet() {
        com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet sheet = com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet.newInstance(
                preferences.getLanguage(),
                selectedItem -> {
                    preferences.setLanguage(selectedItem.getCode());
                    updateLanguageDisplay(selectedItem.getCode());
                    syncChipSelection(selectedItem.getCode());
                    Toast.makeText(this, "Language set to " + selectedItem.getFullDisplayName(), Toast.LENGTH_SHORT).show();
                }
        );
        sheet.show(getSupportFragmentManager(), "language_select");
    }

    private void setupNavigation() {
        findViewById(R.id.btnNavHistory).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });

        findViewById(R.id.btnNavSettings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.btnNavAbout).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        });
    }

    private void setupRecentRecyclerView() {
        recentSessionAdapter = new SessionAdapter(this, false);
        rvRecentSessions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentSessions.setAdapter(recentSessionAdapter);
    }

    private void loadStatsAndRecent() {
        int totalSessions = sessionRepository.getTotalSessionsCount();
        int totalCaptions = sessionRepository.getTotalCaptionsCount();

        tvStatSessionsCount.setText(String.valueOf(totalSessions));
        tvStatCaptionsCount.setText(String.valueOf(totalCaptions));

        List<ConversationSession> recent = sessionRepository.getRecentSessions(3);
        if (recent.isEmpty()) {
            tvEmptyRecent.setVisibility(View.VISIBLE);
            rvRecentSessions.setVisibility(View.GONE);
        } else {
            tvEmptyRecent.setVisibility(View.GONE);
            rvRecentSessions.setVisibility(View.VISIBLE);
            recentSessionAdapter.setSessions(recent);
        }
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            launchLiveCaptions();
        } else {
            requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void launchLiveCaptions() {
        Intent intent = new Intent(MainActivity.this, LiveCaptionsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSessionClick(ConversationSession session) {
        Intent intent = new Intent(this, TranscriptDetailActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    @Override
    public void onSessionDelete(ConversationSession session) {
        // Not used on Home recent cards
    }
}
