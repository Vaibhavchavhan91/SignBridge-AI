package com.vaibhav.signbridgeai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.adapter.SessionAdapter;
import com.vaibhav.signbridgeai.db.SessionRepository;
import com.vaibhav.signbridgeai.model.ConversationSession;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * History Activity displaying list of saved conversation transcripts with search and delete.
 */
public class HistoryActivity extends AppCompatActivity implements SessionAdapter.OnSessionClickListener {

    private SessionRepository sessionRepository;
    private SessionAdapter sessionAdapter;
    private RecyclerView rvSessions;
    private View layoutEmpty;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sessionRepository = new SessionRepository(this);

        initViews();
        loadSessions("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessions(etSearch.getText().toString());
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarHistory);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvSessions = findViewById(R.id.rvHistorySessions);
        layoutEmpty = findViewById(R.id.layoutEmptyHistory);
        etSearch = findViewById(R.id.etHistorySearch);

        sessionAdapter = new SessionAdapter(this, true);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(sessionAdapter);

        findViewById(R.id.btnClearAllHistory).setOnClickListener(v -> showClearAllConfirmationDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSessions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSessions(String query) {
        List<ConversationSession> list;
        if (query == null || query.trim().isEmpty()) {
            list = sessionRepository.getAllSessions();
        } else {
            list = sessionRepository.searchSessions(query.trim());
        }

        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvSessions.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvSessions.setVisibility(View.VISIBLE);
            sessionAdapter.setSessions(list);
        }
    }

    @Override
    public void onSessionClick(ConversationSession session) {
        Intent intent = new Intent(this, TranscriptDetailActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    @Override
    public void onSessionDelete(ConversationSession session) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.history_confirm_delete_single_title)
                .setMessage(R.string.history_confirm_delete_single_msg)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    sessionRepository.deleteSession(session.getId());
                    loadSessions(etSearch.getText().toString());
                    Toast.makeText(this, R.string.history_deleted_toast, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showClearAllConfirmationDialog() {
        if (sessionAdapter.getItemCount() == 0) {
            Toast.makeText(this, "History is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.history_confirm_clear_title)
                .setMessage(R.string.history_confirm_clear_msg)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    sessionRepository.deleteAllSessions();
                    loadSessions("");
                    Toast.makeText(this, R.string.history_all_cleared_toast, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
