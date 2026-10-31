package com.vaibhav.signbridgeai;

import com.vaibhav.signbridgeai.model.CaptionItem;
import com.vaibhav.signbridgeai.model.ConversationSession;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SignBridge AI data models and business logic.
 */
public class ExampleUnitTest {

    @Test
    public void testCaptionItemDefaults() {
        CaptionItem item = new CaptionItem("Good morning, how are you?", "10:30 AM", false);
        assertEquals("Good morning, how are you?", item.getText());
        assertEquals("10:30 AM", item.getFormattedTime());
        assertEquals("Speaker", item.getSpeakerTag());
        assertFalse(item.isPartial());
        assertTrue(item.getTimestampMs() > 0);
    }

    @Test
    public void testCaptionItemCustomSpeaker() {
        CaptionItem item = new CaptionItem(1L, "You (Spoken)", "Yes, I understand", 1000L, "10:32 AM");
        assertEquals("You (Spoken)", item.getSpeakerTag());
        assertEquals("Yes, I understand", item.getText());
        assertEquals("10:32 AM", item.getFormattedTime());
    }

    @Test
    public void testConversationSessionDuration() {
        long start = 1000000L;
        long end = start + (125 * 1000L); // 2m 5s

        ConversationSession session = new ConversationSession(
                1L, "Doctor Appointment", start, end, "en-US", 140, 12, "Preview text"
        );

        assertEquals("Doctor Appointment", session.getTitle());
        assertEquals(125L, session.getDurationSeconds());
        assertEquals("2m 5s", session.getFormattedDuration());
        assertEquals("en-US", session.getLanguage());
        assertEquals(140, session.getTotalWords());
        assertEquals(12, session.getSentenceCount());
    }

    @Test
    public void testShortSessionDuration() {
        long start = 5000L;
        long end = 5000L + (45 * 1000L); // 45 seconds

        ConversationSession session = new ConversationSession(
                2L, "Brief Checkin", start, end, "hi-IN", 30, 3, "नमस्ते"
        );

        assertEquals(45L, session.getDurationSeconds());
        assertEquals("45s", session.getFormattedDuration());
    }

    @Test
    public void testLanguageHelperRetrieval() {
        com.vaibhav.signbridgeai.model.LanguageItem us = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("en-US");
        assertEquals("United States", us.getCountryName());
        assertEquals("🇺🇸", us.getFlagEmoji());

        com.vaibhav.signbridgeai.model.LanguageItem fr = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("fr-FR");
        assertEquals("France", fr.getCountryName());
        assertEquals("🇫🇷", fr.getFlagEmoji());

        com.vaibhav.signbridgeai.model.LanguageItem ja = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("ja-JP");
        assertEquals("Japan", ja.getCountryName());
        assertEquals("🇯🇵", ja.getFlagEmoji());
    }

    @Test
    public void testLanguageHelperSearch() {
        java.util.List<com.vaibhav.signbridgeai.model.LanguageItem> results = com.vaibhav.signbridgeai.util.LanguageHelper.search("Spain", null);
        assertFalse(results.isEmpty());
        assertEquals("Spain", results.get(0).getCountryName());
    }

    @Test
    public void testEnglishHindiMarathiLocales() {
        com.vaibhav.signbridgeai.model.LanguageItem en = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("en-US");
        assertNotNull(en);
        assertEquals("en-US", en.getCode());

        com.vaibhav.signbridgeai.model.LanguageItem hi = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("hi-IN");
        assertNotNull(hi);
        assertEquals("hi-IN", hi.getCode());
        assertEquals("हिन्दी", hi.getLanguageName());
        assertEquals("India", hi.getCountryName());
        assertEquals("🇮🇳", hi.getFlagEmoji());

        com.vaibhav.signbridgeai.model.LanguageItem mr = com.vaibhav.signbridgeai.util.LanguageHelper.getLanguageByCode("mr-IN");
        assertNotNull(mr);
        assertEquals("mr-IN", mr.getCode());
        assertEquals("मराठी", mr.getLanguageName());
        assertEquals("India", mr.getCountryName());
        assertEquals("🇮🇳", mr.getFlagEmoji());
    }
}
