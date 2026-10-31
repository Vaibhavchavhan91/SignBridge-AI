package com.vaibhav.signbridgeai.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Hardened SpeechManager for Android SpeechRecognizer API.
 * Ensures thread affinity, safeguards against restart loops, prevents duplicate callbacks,
 * manages graceful pause/resume, and cleans up resources thoroughly without memory leaks.
 */
public class SpeechManager {

    private static final String TAG = "SignBridge_Speech";

    public enum State {
        IDLE,
        LISTENING,
        HEARING_SPEECH,
        PAUSED,
        STOPPED,
        ERROR
    }

    public interface SpeechCallback {
        void onStateChanged(State state, String statusMessage);
        void onPartialResult(String partialText);
        void onFinalResult(String finalText);
        void onRmsChanged(float rmsDb, int level); // level: 0 to 10
        void onError(int errorCode, String errorMessage);
    }

    private final Context context;
    private final Handler mainHandler;
    private SpeechRecognizer speechRecognizer;
    private SpeechCallback callback;

    private State currentState = State.IDLE;
    private boolean isUserActive = false; // true only when user explicitly started listening
    private boolean isPaused = false;
    private boolean isListeningInProgress = false; // prevents duplicate startListening() calls
    private String selectedLanguage = "en-US";

    // Restart loop safeguards
    private int consecutiveErrorCount = 0;
    private static final int MAX_CONSECUTIVE_ERRORS = 5;
    private long lastErrorTime = 0;

    private final Runnable restartRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUserActive && !isPaused) {
                internalStartListening();
            }
        }
    };

    public SpeechManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setCallback(SpeechCallback callback) {
        this.callback = callback;
    }

    public void setLanguage(String languageCode) {
        this.selectedLanguage = languageCode != null ? languageCode : "en-US";
    }

    public String getLanguage() {
        return selectedLanguage;
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isRecognitionAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    private void runOnMainThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    /**
     * User explicitly pressed Start Listening.
     */
    public synchronized void startListening() {
        runOnMainThread(() -> {
            isUserActive = true;
            isPaused = false;
            consecutiveErrorCount = 0;
            mainHandler.removeCallbacks(restartRunnable);

            if (!isRecognitionAvailable()) {
                updateState(State.ERROR, "Speech recognition service is not available on this device.");
                notifyError(-1, "Speech recognition service is not available on this device.");
                return;
            }

            initRecognizerIfNeeded();
            internalStartListening();
        });
    }

    /**
     * User explicitly pressed Pause.
     */
    public synchronized void pauseListening() {
        runOnMainThread(() -> {
            isPaused = true;
            isUserActive = false;
            isListeningInProgress = false;
            mainHandler.removeCallbacks(restartRunnable);

            if (speechRecognizer != null) {
                try {
                    speechRecognizer.stopListening();
                    speechRecognizer.cancel();
                } catch (Exception e) {
                    Log.w(TAG, "Exception while pausing recognizer: " + e.getMessage());
                }
            }

            updateState(State.PAUSED, "Listening paused");
            if (callback != null) {
                callback.onRmsChanged(0, 0);
            }
        });
    }

    /**
     * User explicitly resumed listening from paused state.
     */
    public synchronized void resumeListening() {
        runOnMainThread(() -> {
            if (!isPaused && isUserActive) {
                return;
            }
            isPaused = false;
            isUserActive = true;
            consecutiveErrorCount = 0;
            mainHandler.removeCallbacks(restartRunnable);

            initRecognizerIfNeeded();
            internalStartListening();
        });
    }

    /**
     * User pressed Stop or left the screen.
     */
    public synchronized void stopListening() {
        runOnMainThread(() -> {
            isUserActive = false;
            isPaused = false;
            isListeningInProgress = false;
            mainHandler.removeCallbacks(restartRunnable);

            if (speechRecognizer != null) {
                try {
                    speechRecognizer.stopListening();
                    speechRecognizer.cancel();
                } catch (Exception e) {
                    Log.w(TAG, "Exception while stopping recognizer: " + e.getMessage());
                }
            }

            updateState(State.STOPPED, "Session stopped");
            if (callback != null) {
                callback.onRmsChanged(0, 0);
            }
        });
    }

    /**
     * Completely release all resources and avoid memory leaks.
     */
    public synchronized void destroy() {
        runOnMainThread(() -> {
            isUserActive = false;
            isPaused = false;
            isListeningInProgress = false;
            mainHandler.removeCallbacks(restartRunnable);

            destroyRecognizerInternal();
            currentState = State.IDLE;
        });
    }

    private void destroyRecognizerInternal() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.cancel();
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.w(TAG, "Exception destroying SpeechRecognizer: " + e.getMessage());
            } finally {
                speechRecognizer = null;
            }
        }
    }

    private void initRecognizerIfNeeded() {
        if (speechRecognizer == null) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        if (!isUserActive || isPaused) return;
                        consecutiveErrorCount = 0;
                        isListeningInProgress = true;
                        updateState(State.LISTENING, "Listening for speech…");
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        if (!isUserActive || isPaused) return;
                        updateState(State.HEARING_SPEECH, "Hearing voice…");
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                        if (!isUserActive || isPaused || callback == null) return;
                        int level = Math.max(0, Math.min(10, (int) ((rmsdB + 2.0f) * 1.2f)));
                        callback.onRmsChanged(rmsdB, level);
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        if (!isUserActive || isPaused) return;
                        isListeningInProgress = false;
                        updateState(State.LISTENING, "Processing speech…");
                        if (callback != null) {
                            callback.onRmsChanged(0, 0);
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                        isListeningInProgress = false;
                        handleSpeechError(errorCode);
                    }

                    @Override
                    public void onResults(Bundle results) {
                        isListeningInProgress = false;
                        handleSpeechResults(results);
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        handlePartialResults(partialResults);
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize SpeechRecognizer: " + e.getMessage());
                notifyError(SpeechRecognizer.ERROR_CLIENT, "Failed to initialize SpeechRecognizer.");
            }
        }
    }

    private void internalStartListening() {
        if (!isUserActive || isPaused || speechRecognizer == null || isListeningInProgress) {
            return;
        }

        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selectedLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

            isListeningInProgress = true;
            speechRecognizer.startListening(intent);
            updateState(State.LISTENING, "Listening for speech…");
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
            isListeningInProgress = false;
            handleSpeechError(SpeechRecognizer.ERROR_CLIENT);
        }
    }

    private void handleSpeechResults(Bundle results) {
        if (!isUserActive || isPaused) {
            return;
        }

        consecutiveErrorCount = 0;
        if (results != null) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String bestMatch = matches.get(0).trim();
                if (!bestMatch.isEmpty()) {
                    if (callback != null) {
                        callback.onFinalResult(bestMatch);
                    }
                }
            }
        }

        // Seamlessly continue listening for the next sentence if user remains active
        if (isUserActive && !isPaused) {
            mainHandler.removeCallbacks(restartRunnable);
            mainHandler.postDelayed(restartRunnable, 150);
        }
    }

    private void handlePartialResults(Bundle partialResults) {
        if (!isUserActive || isPaused || callback == null) {
            return;
        }

        if (partialResults != null) {
            ArrayList<String> partialList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (partialList != null && !partialList.isEmpty()) {
                String text = partialList.get(0).trim();
                if (!text.isEmpty()) {
                    callback.onPartialResult(text);
                }
            }
        }
    }

    private void handleSpeechError(int errorCode) {
        if (!isUserActive || isPaused) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastErrorTime < 1500) {
            consecutiveErrorCount++;
        } else {
            consecutiveErrorCount = 1;
        }
        lastErrorTime = now;

        String errorMsg = getErrorMessage(errorCode);
        Log.w(TAG, "SpeechRecognizer error: " + errorCode + " - " + errorMsg + " (Consecutive: " + consecutiveErrorCount + ")");

        // Normal operational pauses in live speech recognition
        boolean isBenignTimeout = (errorCode == SpeechRecognizer.ERROR_NO_MATCH ||
                                   errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT);

        if (isBenignTimeout) {
            consecutiveErrorCount = 0; // reset for silence timeouts
            if (isUserActive && !isPaused) {
                mainHandler.removeCallbacks(restartRunnable);
                mainHandler.postDelayed(restartRunnable, 200);
            }
            return;
        }

        // Busy or Client errors often indicate binder lock; recreate instance
        if (errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || errorCode == SpeechRecognizer.ERROR_CLIENT) {
            destroyRecognizerInternal();
            initRecognizerIfNeeded();
        }

        // Loop protection against fatal repeat errors
        if (consecutiveErrorCount >= MAX_CONSECUTIVE_ERRORS) {
            isUserActive = false;
            mainHandler.removeCallbacks(restartRunnable);
            updateState(State.ERROR, errorMsg);
            notifyError(errorCode, errorMsg);
            return;
        }

        // Wait with backoff and retry
        long delay = Math.min(2500, 400 * consecutiveErrorCount);
        updateState(State.LISTENING, "Reconnecting recognizer…");

        mainHandler.removeCallbacks(restartRunnable);
        mainHandler.postDelayed(() -> {
            if (isUserActive && !isPaused) {
                initRecognizerIfNeeded();
                internalStartListening();
            }
        }, delay);
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error. Check microphone.";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Speech recognizer client error. Reconnecting…";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission required.";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network connection error. Speech service may require Internet on this device.";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Speech service network timeout.";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech detected.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition engine busy. Resetting…";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error. Selected language might require downloading on your device.";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech heard.";
            default:
                return "Recognition error (" + errorCode + ").";
        }
    }

    private void updateState(State newState, String message) {
        this.currentState = newState;
        if (callback != null) {
            callback.onStateChanged(newState, message);
        }
    }

    private void notifyError(int code, String message) {
        if (callback != null) {
            callback.onError(code, message);
        }
    }
}
