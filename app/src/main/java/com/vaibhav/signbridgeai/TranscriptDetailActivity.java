package com.vaibhav.signbridgeai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.adapter.CaptionAdapter;
import com.vaibhav.signbridgeai.db.SessionRepository;
import com.vaibhav.signbridgeai.model.CaptionItem;
import com.vaibhav.signbridgeai.model.ConversationSession;
import com.vaibhav.signbridgeai.util.AppPreferences;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Detailed view of a past conversation transcript.
 * Provides copy, share, and delete capabilities.
 */
public class TranscriptDetailActivity extends AppCompatActivity {

    private SessionRepository sessionRepository;
    private AppPreferences preferences;
    private long sessionId;
    private ConversationSession session;
    private List<CaptionItem> captionList;

    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvLangBadge;
    private TextView tvStats;
    private RecyclerView rvCaptions;
    private CaptionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript_detail);

        sessionRepository = new SessionRepository(this);
        preferences = new AppPreferences(this);
        sessionId = getIntent().getLongExtra("session_id", -1);

        if (sessionId == -1) {
            finish();
            return;
        }

        initViews();
        loadSessionData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDate = findViewById(R.id.tvDetailDate);
        tvLangBadge = findViewById(R.id.tvDetailLangBadge);
        tvStats = findViewById(R.id.tvDetailStats);
        rvCaptions = findViewById(R.id.rvDetailCaptions);

        adapter = new CaptionAdapter();
        adapter.setFontSizeSp(preferences.getCaptionFontSizeSp());
        rvCaptions.setLayoutManager(new LinearLayoutManager(this));
        rvCaptions.setAdapter(adapter);

        findViewById(R.id.btnCopyTranscript).setOnClickListener(v -> copyTranscript());
        findViewById(R.id.btnShareTranscript).setOnClickListener(v -> shareTranscript());
        findViewById(R.id.btnDeleteTranscript).setOnClickListener(v -> confirmDelete());
    }

    private void loadSessionData() {
        session = sessionRepository.getSessionById(sessionId);
        captionList = sessionRepository.getCaptionsForSession(sessionId);

        if (session == null) {
            finish();
            return;
        }

        tvTitle.setText(session.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(session.getStartTime())));

        String lang = session.getLanguage();
        com.vaibhav.signbridgeai.model.LanguageItem langItem = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode(lang);
        tvLangBadge.setText(langItem.getFullDisplayName());

        String stats = session.getSentenceCount() + " captions • " + session.getTotalWords() + " words • Duration: " + session.getFormattedDuration();
        tvStats.setText(stats);

        adapter.setItems(captionList);
    }

    private String getFullTranscriptText() {
        if (captionList == null || captionList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(session != null ? session.getTitle() : "Conversation Transcript").append("\n");
        sb.append("Recorded with SignBridge AI\n\n");

        for (CaptionItem item : captionList) {
            sb.append("[").append(item.getFormattedTime()).append("] ")
              .append(item.getSpeakerTag()).append(": ")
              .append(item.getText()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private void copyTranscript() {
        String full = getFullTranscriptText();
        if (full.isEmpty()) {
            Toast.makeText(this, "Transcript is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Transcript", full);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.detail_copied_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareTranscript() {
        String full = getFullTranscriptText();
        if (full.isEmpty()) {
            Toast.makeText(this, "Transcript is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, session != null ? session.getTitle() : "SignBridge Conversation");
        shareIntent.putExtra(Intent.EXTRA_TEXT, full);
        startActivity(Intent.createChooser(shareIntent, "Share Conversation Transcript"));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.history_confirm_delete_single_title)
                .setMessage(R.string.history_confirm_delete_single_msg)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    sessionRepository.deleteSession(sessionId);
                    Toast.makeText(this, R.string.history_deleted_toast, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
