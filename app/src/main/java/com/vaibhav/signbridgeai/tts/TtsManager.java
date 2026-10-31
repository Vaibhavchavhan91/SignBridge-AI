package com.vaibhav.signbridgeai.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Text-to-Speech manager enabling deaf users to type messages and speak them aloud.
 */
public class TtsManager implements TextToSpeech.OnInitListener {

    private static final String TAG = "SignBridge_TTS";

    private final Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private Locale currentLocale = Locale.US;

    public TtsManager(Context context) {
        this.context = context.getApplicationContext();
        this.textToSpeech = new TextToSpeech(this.context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true;
            applyLocale(currentLocale);
            Log.d(TAG, "TTS initialized successfully");
        } else {
            Log.w(TAG, "TTS initialization failed with code: " + status);
        }
    }

    public void setLanguage(String langCode) {
        Locale target = Locale.US;
        if (langCode != null && !langCode.trim().isEmpty()) {
            try {
                target = Locale.forLanguageTag(langCode);
            } catch (Exception e) {
                target = Locale.US;
            }
        }

        this.currentLocale = target;
        if (isInitialized && textToSpeech != null) {
            applyLocale(target);
        }
    }

    private void applyLocale(Locale locale) {
        try {
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "TTS language not supported: " + locale);
                textToSpeech.setLanguage(Locale.US);
            }
        } catch (Exception e) {
            Log.w(TAG, "Exception setting TTS locale: " + e.getMessage());
        }
    }

    public void speak(String text) {
        if (!isInitialized || textToSpeech == null || text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            textToSpeech.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, null, "SignBridge_UTTERANCE");
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text: " + e.getMessage());
        }
    }

    public void stop() {
        if (isInitialized && textToSpeech != null) {
            try {
                textToSpeech.stop();
            } catch (Exception ignored) {
            }
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (Exception ignored) {
            } finally {
                textToSpeech = null;
                isInitialized = false;
            }
        }
    }
}
