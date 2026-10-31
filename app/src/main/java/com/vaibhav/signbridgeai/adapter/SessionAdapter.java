package com.vaibhav.signbridgeai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaibhav.signbridgeai.R;
import com.vaibhav.signbridgeai.model.ConversationSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying saved conversation sessions.
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(ConversationSession session);
        void onSessionDelete(ConversationSession session);
    }

    private final List<ConversationSession> sessions = new ArrayList<>();
    private final OnSessionClickListener listener;
    private final boolean showDeleteButton;

    public SessionAdapter(OnSessionClickListener listener, boolean showDeleteButton) {
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }

    public void setSessions(List<ConversationSession> newSessions) {
        this.sessions.clear();
        if (newSessions != null) {
            this.sessions.addAll(newSessions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ConversationSession session = sessions.get(position);
        holder.bind(session, listener, showDeleteButton);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvLanguage;
        private final TextView tvStats;
        private final TextView tvPreview;
        private final ImageButton btnDelete;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvDate = itemView.findViewById(R.id.tvSessionDate);
            tvLanguage = itemView.findViewById(R.id.tvSessionLanguage);
            tvStats = itemView.findViewById(R.id.tvSessionStats);
            tvPreview = itemView.findViewById(R.id.tvSessionPreview);
            btnDelete = itemView.findViewById(R.id.btnDeleteSession);
        }

        public void bind(ConversationSession session, OnSessionClickListener listener, boolean showDelete) {
            tvTitle.setText(session.getTitle());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(session.getStartTime())));

            String lang = session.getLanguage();
            com.vaibhav.signbridgeai.model.LanguageItem langItem = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode(lang);
            tvLanguage.setText(langItem.getFlagEmoji() + " " + langItem.getLanguageName());

            String stats = session.getSentenceCount() + " captions • " + session.getTotalWords() + " words • " + session.getFormattedDuration();
            tvStats.setText(stats);

            String preview = session.getPreviewText();
            if (preview != null && !preview.isEmpty()) {
                tvPreview.setVisibility(View.VISIBLE);
                tvPreview.setText(preview);
            } else {
                tvPreview.setVisibility(View.GONE);
            }

            btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionDelete(session);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });
        }
    }
}
