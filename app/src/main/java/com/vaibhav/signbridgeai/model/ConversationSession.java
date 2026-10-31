package com.vaibhav.signbridgeai.model;

/**
 * Represents a saved conversation session with metadata.
 */
public class ConversationSession {
    private long id;
    private String title;
    private long startTime;
    private long endTime;
    private String language;
    private int totalWords;
    private int sentenceCount;
    private String previewText;

    public ConversationSession() {
    }

    public ConversationSession(long id, String title, long startTime, long endTime,
                               String language, int totalWords, int sentenceCount, String previewText) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.language = language;
        this.totalWords = totalWords;
        this.sentenceCount = sentenceCount;
        this.previewText = previewText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "Conversation";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLanguage() {
        return language != null ? language : "en-US";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public String getPreviewText() {
        return previewText != null ? previewText : "";
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public long getDurationSeconds() {
        if (endTime > startTime && startTime > 0) {
            return (endTime - startTime) / 1000;
        }
        return 0;
    }

    public String getFormattedDuration() {
        long seconds = getDurationSeconds();
        long mins = seconds / 60;
        long secs = seconds % 60;
        if (mins == 0) {
            return secs + "s";
        }
        return String.format("%dm %ds", mins, secs);
    }
}
