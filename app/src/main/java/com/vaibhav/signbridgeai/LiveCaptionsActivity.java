package com.vaibhav.signbridgeai;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.adapter.CaptionAdapter;
import com.vaibhav.signbridgeai.db.SessionRepository;
import com.vaibhav.signbridgeai.model.CaptionItem;
import com.vaibhav.signbridgeai.speech.SpeechManager;
import com.vaibhav.signbridgeai.tts.TtsManager;
import com.vaibhav.signbridgeai.util.AppPreferences;
import com.vaibhav.signbridgeai.util.HapticHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Live Captions Activity providing real-time speech-to-text, audio volume visualizer,
 * dynamic accessibility font controls, and Text-to-Speech quick replies.
 */
public class LiveCaptionsActivity extends AppCompatActivity {

    private SpeechManager speechManager;
    private TtsManager ttsManager;
    private SessionRepository sessionRepository;
    private AppPreferences preferences;
    private HapticHelper hapticHelper;

    private CaptionAdapter captionAdapter;
    private RecyclerView rvCaptions;
    private TextView tvEmptyPlaceholder;
    private TextView tvLiveStatus;
    private View viewStatusDot;
    private TextView tvLiveLanguagePill;

    // Partial results
    private View cardPartialResult;
    private TextView tvPartialText;

    // Sound bars
    private View bar1, bar2, bar3, bar4, bar5;

    // Bottom controls
    private MaterialButton btnPauseResume;
    private MaterialButton btnStop;
    private EditText etTtsInput;

    private long sessionStartTime = 0;
    private boolean isSessionSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_captions);

        preferences = new AppPreferences(this);
        sessionRepository = new SessionRepository(this);
        hapticHelper = new HapticHelper(this);
        sessionStartTime = System.currentTimeMillis();

        if (preferences.isKeepScreenOnEnabled()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        initViews();
        setupToolbar();
        setupSpeechManager();
        setupTts();

        // Start listening immediately as user selected Start from Home
        speechManager.startListening();
        hapticHelper.vibrateStart();
    }

    private void initViews() {
        rvCaptions = findViewById(R.id.rvCaptions);
        tvEmptyPlaceholder = findViewById(R.id.tvEmptyCaptionsPlaceholder);
        tvLiveStatus = findViewById(R.id.tvLiveStatus);
        viewStatusDot = findViewById(R.id.viewStatusDot);
        tvLiveLanguagePill = findViewById(R.id.tvLiveLanguagePill);

        cardPartialResult = findViewById(R.id.cardPartialResult);
        tvPartialText = findViewById(R.id.tvPartialText);

        bar1 = findViewById(R.id.bar1);
        bar2 = findViewById(R.id.bar2);
        bar3 = findViewById(R.id.bar3);
        bar4 = findViewById(R.id.bar4);
        bar5 = findViewById(R.id.bar5);

        btnPauseResume = findViewById(R.id.btnLivePauseResume);
        btnStop = findViewById(R.id.btnLiveStop);
        etTtsInput = findViewById(R.id.etTtsInput);

        // Language pill
        updateLanguagePillDisplay();
        tvLiveLanguagePill.setOnClickListener(v -> openLanguageSwitcher());

        // Setup RecyclerView
        captionAdapter = new CaptionAdapter();
        captionAdapter.setFontSizeSp(preferences.getCaptionFontSizeSp());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvCaptions.setLayoutManager(layoutManager);
        rvCaptions.setAdapter(captionAdapter);

        // Font size buttons
        findViewById(R.id.btnFontDecrease).setOnClickListener(v -> adjustFontSize(-3));
        findViewById(R.id.btnFontIncrease).setOnClickListener(v -> adjustFontSize(3));

        // Clear button
        findViewById(R.id.btnClearCaptions).setOnClickListener(v -> showClearConfirmationDialog());

        // Save session button
        findViewById(R.id.btnSaveSession).setOnClickListener(v -> saveCurrentSession(true));

        // Pause/Resume button
        btnPauseResume.setOnClickListener(v -> togglePauseResume());

        // Stop button
        btnStop.setOnClickListener(v -> finishSessionWithPrompt());

        // Quick phrases
        findViewById(R.id.btnQuickPhrase1).setOnClickListener(v -> speakQuickPhrase(getString(R.string.tts_phrase_yes)));
        findViewById(R.id.btnQuickPhrase2).setOnClickListener(v -> speakQuickPhrase(getString(R.string.tts_phrase_no)));
        findViewById(R.id.btnQuickPhrase3).setOnClickListener(v -> speakQuickPhrase(getString(R.string.tts_phrase_repeat)));
        findViewById(R.id.btnQuickPhrase4).setOnClickListener(v -> speakQuickPhrase(getString(R.string.tts_phrase_deaf)));

        // Speak custom typed text
        findViewById(R.id.btnSpeakReply).setOnClickListener(v -> {
            String text = etTtsInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                speakQuickPhrase(text);
                etTtsInput.setText("");
            }
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarLive);
        toolbar.setNavigationOnClickListener(v -> finishSessionWithPrompt());
    }

    private void setupSpeechManager() {
        speechManager = new SpeechManager(this);
        speechManager.setLanguage(preferences.getLanguage());

        speechManager.setCallback(new SpeechManager.SpeechCallback() {
            @Override
            public void onStateChanged(SpeechManager.State state, String statusMessage) {
                runOnUiThread(() -> handleStateChange(state, statusMessage));
            }

            @Override
            public void onPartialResult(String partialText) {
                runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(partialText)) {
                        cardPartialResult.setVisibility(View.VISIBLE);
                        tvPartialText.setText(partialText);
                    }
                });
            }

            @Override
            public void onFinalResult(String finalText) {
                runOnUiThread(() -> {
                    cardPartialResult.setVisibility(View.GONE);
                    tvPartialText.setText("");

                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    String formattedTime = timeFormat.format(new Date());

                    CaptionItem item = new CaptionItem(finalText, formattedTime, false);
                    captionAdapter.addCaption(item);
                    tvEmptyPlaceholder.setVisibility(View.GONE);

                    rvCaptions.smoothScrollToPosition(captionAdapter.getItemCount() - 1);
                    hapticHelper.vibrateVoiceDetected();
                });
            }

            @Override
            public void onRmsChanged(float rmsDb, int level) {
                runOnUiThread(() -> updateSoundBars(level));
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                runOnUiThread(() -> {
                    cardPartialResult.setVisibility(View.GONE);
                    tvLiveStatus.setText(errorMessage);
                    viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(LiveCaptionsActivity.this, R.color.status_error)));
                });
            }
        });
    }

    private void setupTts() {
        ttsManager = new TtsManager(this);
        ttsManager.setLanguage(preferences.getLanguage());
    }

    private void updateLanguagePillDisplay() {
        com.vaibhav.signbridgeai.model.LanguageItem item = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode(preferences.getLanguage());
        tvLiveLanguagePill.setText(item.getFlagEmoji() + " " + item.getCountryName());
    }

    private void openLanguageSwitcher() {
        com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet sheet = com.vaibhav.signbridgeai.dialog.LanguageSelectBottomSheet.newInstance(
                preferences.getLanguage(),
                selectedItem -> {
                    preferences.setLanguage(selectedItem.getCode());
                    updateLanguagePillDisplay();
                    if (speechManager != null) {
                        speechManager.setLanguage(selectedItem.getCode());
                        if (speechManager.getCurrentState() == SpeechManager.State.LISTENING ||
                            speechManager.getCurrentState() == SpeechManager.State.HEARING_SPEECH) {
                            speechManager.pauseListening();
                            speechManager.resumeListening();
                        }
                    }
                    if (ttsManager != null) {
                        ttsManager.setLanguage(selectedItem.getCode());
                    }
                    Toast.makeText(this, "Language set to " + selectedItem.getFullDisplayName(), Toast.LENGTH_SHORT).show();
                }
        );
        sheet.show(getSupportFragmentManager(), "live_lang_switch");
    }

    private void handleStateChange(SpeechManager.State state, String statusMessage) {
        tvLiveStatus.setText(statusMessage);

        int dotColor;
        switch (state) {
            case LISTENING:
                dotColor = ContextCompat.getColor(this, R.color.status_ready);
                btnPauseResume.setText(R.string.captions_btn_pause);
                btnPauseResume.setIconResource(R.drawable.ic_pause_24dp);
                btnPauseResume.setEnabled(true);
                break;
            case HEARING_SPEECH:
                dotColor = ContextCompat.getColor(this, R.color.mic_active);
                btnPauseResume.setText(R.string.captions_btn_pause);
                btnPauseResume.setIconResource(R.drawable.ic_pause_24dp);
                break;
            case PAUSED:
                dotColor = ContextCompat.getColor(this, R.color.mic_paused);
                btnPauseResume.setText(R.string.captions_btn_resume);
                btnPauseResume.setIconResource(R.drawable.ic_play_24dp);
                btnPauseResume.setEnabled(true);
                updateSoundBars(0);
                break;
            case STOPPED:
            case IDLE:
                dotColor = ContextCompat.getColor(this, R.color.text_secondary_light);
                btnPauseResume.setText(R.string.home_start_listening);
                btnPauseResume.setIconResource(R.drawable.ic_play_24dp);
                updateSoundBars(0);
                break;
            case ERROR:
            default:
                dotColor = ContextCompat.getColor(this, R.color.status_error);
                updateSoundBars(0);
                break;
        }

        viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(dotColor));
    }

    private void updateSoundBars(int level) {
        int baseHeightDp = 6;
        int maxExtraDp = 20;

        int h1 = (int) ((baseHeightDp + (level * 1.2f)) * getResources().getDisplayMetrics().density);
        int h2 = (int) ((baseHeightDp + (level * 1.8f)) * getResources().getDisplayMetrics().density);
        int h3 = (int) ((baseHeightDp + (level * 2.2f)) * getResources().getDisplayMetrics().density);
        int h4 = (int) ((baseHeightDp + (level * 1.8f)) * getResources().getDisplayMetrics().density);
        int h5 = (int) ((baseHeightDp + (level * 1.2f)) * getResources().getDisplayMetrics().density);

        setBarHeight(bar1, h1);
        setBarHeight(bar2, h2);
        setBarHeight(bar3, h3);
        setBarHeight(bar4, h4);
        setBarHeight(bar5, h5);
    }

    private void setBarHeight(View bar, int heightPx) {
        if (bar != null) {
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            if (lp != null) {
                lp.height = Math.max(12, heightPx);
                bar.setLayoutParams(lp);
            }
        }
    }

    private void togglePauseResume() {
        if (speechManager.getCurrentState() == SpeechManager.State.PAUSED) {
            speechManager.resumeListening();
            hapticHelper.vibrateStart();
        } else if (speechManager.getCurrentState() == SpeechManager.State.LISTENING ||
                   speechManager.getCurrentState() == SpeechManager.State.HEARING_SPEECH) {
            speechManager.pauseListening();
            hapticHelper.vibratePause();
        } else {
            speechManager.startListening();
            hapticHelper.vibrateStart();
        }
    }

    private void adjustFontSize(int delta) {
        int current = captionAdapter.getFontSizeSp();
        int newSize = current + delta;
        captionAdapter.setFontSizeSp(newSize);
        preferences.setCaptionFontSizeSp(captionAdapter.getFontSizeSp());

        Toast.makeText(this, "Text size: " + captionAdapter.getFontSizeSp() + "sp", Toast.LENGTH_SHORT).show();
    }

    private void showClearConfirmationDialog() {
        if (captionAdapter.getItemCount() == 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear Conversation?")
                .setMessage("Are you sure you want to clear current live captions?")
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    captionAdapter.clear();
                    tvEmptyPlaceholder.setVisibility(View.VISIBLE);
                    cardPartialResult.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.captions_cleared_toast, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void speakQuickPhrase(String phrase) {
        if (TextUtils.isEmpty(phrase)) return;

        ttsManager.speak(phrase);

        // Also add to captions as user's response
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(new Date());

        CaptionItem item = new CaptionItem(phrase, formattedTime, false);
        item.setSpeakerTag("You (Spoken)");
        captionAdapter.addCaption(item);
        tvEmptyPlaceholder.setVisibility(View.GONE);
        rvCaptions.smoothScrollToPosition(captionAdapter.getItemCount() - 1);
    }

    private boolean saveCurrentSession(boolean showToast) {
        List<CaptionItem> items = captionAdapter.getItems();
        if (items.isEmpty()) {
            if (showToast) {
                Toast.makeText(this, "No captions to save yet", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        long endTime = System.currentTimeMillis();
        long id = sessionRepository.saveSession(
                null,
                sessionStartTime,
                endTime,
                preferences.getLanguage(),
                items
        );

        if (id != -1) {
            isSessionSaved = true;
            if (showToast) {
                Toast.makeText(this, R.string.captions_saved_toast, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private void finishSessionWithPrompt() {
        speechManager.stopListening();

        if (captionAdapter.getItemCount() > 0 && !isSessionSaved) {
            if (preferences.isAutoSaveEnabled()) {
                saveCurrentSession(false);
                finish();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Save Conversation?")
                        .setMessage("Do you want to save this conversation before exiting?")
                        .setPositiveButton(R.string.action_save, (dialog, which) -> {
                            saveCurrentSession(true);
                            finish();
                        })
                        .setNegativeButton("Don't Save", (dialog, which) -> finish())
                        .setNeutralButton(R.string.action_cancel, (dialog, which) -> {
                            speechManager.resumeListening();
                        })
                        .show();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finishSessionWithPrompt();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speechManager != null) {
            speechManager.pauseListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (speechManager != null) {
            speechManager.destroy();
            speechManager = null;
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
            ttsManager = null;
        }
        super.onDestroy();
    }
}
