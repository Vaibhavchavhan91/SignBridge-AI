package com.vaibhav.signbridgeai.model;

/**
 * Represents a single captured caption sentence or utterance.
 */
public class CaptionItem {
    private long id;
    private long sessionId;
    private String speakerTag;
    private String text;
    private long timestampMs;
    private String formattedTime;
    private boolean isPartial;

    public CaptionItem() {
    }

    public CaptionItem(String text, String formattedTime, boolean isPartial) {
        this.text = text;
        this.formattedTime = formattedTime;
        this.isPartial = isPartial;
        this.timestampMs = System.currentTimeMillis();
        this.speakerTag = "Speaker";
    }

    public CaptionItem(long sessionId, String speakerTag, String text, long timestampMs, String formattedTime) {
        this.sessionId = sessionId;
        this.speakerTag = speakerTag;
        this.text = text;
        this.timestampMs = timestampMs;
        this.formattedTime = formattedTime;
        this.isPartial = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSpeakerTag() {
        return speakerTag != null ? speakerTag : "Speaker";
    }

    public void setSpeakerTag(String speakerTag) {
        this.speakerTag = speakerTag;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public String getFormattedTime() {
        return formattedTime != null ? formattedTime : "";
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }
}
