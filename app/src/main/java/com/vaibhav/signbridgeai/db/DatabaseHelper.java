package com.vaibhav.signbridgeai.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database helper for storing conversation sessions and caption entries.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "signbridge_conversations.db";
    private static final int DATABASE_VERSION = 1;

    // Table Sessions
    public static final String TABLE_SESSIONS = "sessions";
    public static final String COL_SESSION_ID = "_id";
    public static final String COL_SESSION_TITLE = "title";
    public static final String COL_SESSION_START_TIME = "start_time";
    public static final String COL_SESSION_END_TIME = "end_time";
    public static final String COL_SESSION_LANG = "language";
    public static final String COL_SESSION_WORDS = "total_words";
    public static final String COL_SESSION_SENTENCES = "sentence_count";
    public static final String COL_SESSION_PREVIEW = "preview_text";

    // Table Captions
    public static final String TABLE_CAPTIONS = "captions";
    public static final String COL_CAPTION_ID = "_id";
    public static final String COL_CAPTION_SESSION_ID = "session_id";
    public static final String COL_CAPTION_SPEAKER = "speaker_tag";
    public static final String COL_CAPTION_TEXT = "caption_text";
    public static final String COL_CAPTION_TIMESTAMP = "timestamp_ms";
    public static final String COL_CAPTION_FORMATTED_TIME = "formatted_time";

    private static final String CREATE_TABLE_SESSIONS = "CREATE TABLE " + TABLE_SESSIONS + " ("
            + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SESSION_TITLE + " TEXT, "
            + COL_SESSION_START_TIME + " INTEGER, "
            + COL_SESSION_END_TIME + " INTEGER, "
            + COL_SESSION_LANG + " TEXT, "
            + COL_SESSION_WORDS + " INTEGER, "
            + COL_SESSION_SENTENCES + " INTEGER, "
            + COL_SESSION_PREVIEW + " TEXT);";

    private static final String CREATE_TABLE_CAPTIONS = "CREATE TABLE " + TABLE_CAPTIONS + " ("
            + COL_CAPTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CAPTION_SESSION_ID + " INTEGER, "
            + COL_CAPTION_SPEAKER + " TEXT, "
            + COL_CAPTION_TEXT + " TEXT, "
            + COL_CAPTION_TIMESTAMP + " INTEGER, "
            + COL_CAPTION_FORMATTED_TIME + " TEXT, "
            + "FOREIGN KEY(" + COL_CAPTION_SESSION_ID + ") REFERENCES "
            + TABLE_SESSIONS + "(" + COL_SESSION_ID + ") ON DELETE CASCADE);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SESSIONS);
        db.execSQL(CREATE_TABLE_CAPTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAPTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }
}
