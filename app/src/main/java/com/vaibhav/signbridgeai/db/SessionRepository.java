package com.vaibhav.signbridgeai.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vaibhav.signbridgeai.model.CaptionItem;
import com.vaibhav.signbridgeai.model.ConversationSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository for managing conversation sessions and captions in SQLite storage.
 */
public class SessionRepository {

    private final DatabaseHelper dbHelper;

    public SessionRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    /**
     * Saves a complete conversation session along with all its caption items.
     */
    public synchronized long saveSession(String title, long startTime, long endTime,
                                        String language, List<CaptionItem> items) {
        if (items == null || items.isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        long sessionId = -1;

        try {
            int totalWords = 0;
            StringBuilder previewBuilder = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                CaptionItem item = items.get(i);
                String txt = item.getText().trim();
                if (!txt.isEmpty()) {
                    String[] words = txt.split("\\s+");
                    totalWords += words.length;
                    if (previewBuilder.length() < 120) {
                        if (previewBuilder.length() > 0) {
                            previewBuilder.append(" ");
                        }
                        previewBuilder.append(txt);
                    }
                }
            }

            if (title == null || title.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
                title = "Conversation (" + sdf.format(new Date(startTime)) + ")";
            }

            ContentValues sessionValues = new ContentValues();
            sessionValues.put(DatabaseHelper.COL_SESSION_TITLE, title);
            sessionValues.put(DatabaseHelper.COL_SESSION_START_TIME, startTime);
            sessionValues.put(DatabaseHelper.COL_SESSION_END_TIME, endTime);
            sessionValues.put(DatabaseHelper.COL_SESSION_LANG, language);
            sessionValues.put(DatabaseHelper.COL_SESSION_WORDS, totalWords);
            sessionValues.put(DatabaseHelper.COL_SESSION_SENTENCES, items.size());
            sessionValues.put(DatabaseHelper.COL_SESSION_PREVIEW, previewBuilder.toString());

            sessionId = db.insert(DatabaseHelper.TABLE_SESSIONS, null, sessionValues);

            if (sessionId != -1) {
                for (CaptionItem item : items) {
                    ContentValues captionValues = new ContentValues();
                    captionValues.put(DatabaseHelper.COL_CAPTION_SESSION_ID, sessionId);
                    captionValues.put(DatabaseHelper.COL_CAPTION_SPEAKER, item.getSpeakerTag());
                    captionValues.put(DatabaseHelper.COL_CAPTION_TEXT, item.getText());
                    captionValues.put(DatabaseHelper.COL_CAPTION_TIMESTAMP, item.getTimestampMs());
                    captionValues.put(DatabaseHelper.COL_CAPTION_FORMATTED_TIME, item.getFormattedTime());
                    db.insert(DatabaseHelper.TABLE_CAPTIONS, null, captionValues);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return sessionId;
    }

    public synchronized List<ConversationSession> getAllSessions() {
        return querySessions(null, null, DatabaseHelper.COL_SESSION_START_TIME + " DESC", null);
    }

    public synchronized List<ConversationSession> getRecentSessions(int limit) {
        return querySessions(null, null, DatabaseHelper.COL_SESSION_START_TIME + " DESC", String.valueOf(limit));
    }

    public synchronized List<ConversationSession> searchSessions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSessions();
        }
        String selection = DatabaseHelper.COL_SESSION_TITLE + " LIKE ? OR " + DatabaseHelper.COL_SESSION_PREVIEW + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        return querySessions(selection, selectionArgs, DatabaseHelper.COL_SESSION_START_TIME + " DESC", null);
    }

    private List<ConversationSession> querySessions(String selection, String[] selectionArgs, String orderBy, String limit) {
        List<ConversationSession> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SESSIONS, null, selection, selectionArgs, null, null, orderBy, limit)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_TITLE));
                    long start = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_START_TIME));
                    long end = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_END_TIME));
                    String lang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_LANG));
                    int words = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_WORDS));
                    int sentences = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_SENTENCES));
                    String preview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_PREVIEW));

                    list.add(new ConversationSession(id, title, start, end, lang, words, sentences, preview));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public synchronized ConversationSession getSessionById(long sessionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_SESSION_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(sessionId)};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SESSIONS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_TITLE));
                long start = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_START_TIME));
                long end = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_END_TIME));
                String lang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_LANG));
                int words = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_WORDS));
                int sentences = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_SENTENCES));
                String preview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SESSION_PREVIEW));

                return new ConversationSession(id, title, start, end, lang, words, sentences, preview);
            }
        }
        return null;
    }

    public synchronized List<CaptionItem> getCaptionsForSession(long sessionId) {
        List<CaptionItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_CAPTION_SESSION_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(sessionId)};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_CAPTIONS, null, selection, selectionArgs, null, null, DatabaseHelper.COL_CAPTION_TIMESTAMP + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_ID));
                    long sessId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_SESSION_ID));
                    String speaker = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_SPEAKER));
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_TEXT));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_TIMESTAMP));
                    String formatted = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAPTION_FORMATTED_TIME));

                    CaptionItem item = new CaptionItem(sessId, speaker, text, timestamp, formatted);
                    item.setId(id);
                    list.add(item);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public synchronized boolean deleteSession(long sessionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_SESSIONS, DatabaseHelper.COL_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        db.delete(DatabaseHelper.TABLE_CAPTIONS, DatabaseHelper.COL_CAPTION_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        return rows > 0;
    }

    public synchronized void deleteAllSessions() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CAPTIONS, null, null);
        db.delete(DatabaseHelper.TABLE_SESSIONS, null, null);
    }

    public synchronized int getTotalSessionsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SESSIONS, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public synchronized int getTotalCaptionsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CAPTIONS, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }
}
