# SignBridge AI — Real-Time Speech-to-Text Live Captions for Android

[![Android](https://img.shields.io/badge/Platform-Android%20API%2024%2B-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Java](https://img.shields.io/badge/Language-Java%2011-ED8B00.svg?style=flat&logo=openjdk)](https://www.java.com)
[![Material 3](https://img.shields.io/badge/Design-Material%203-757575.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![SQLite](https://img.shields.io/badge/Database-SQLite-003B57.svg?style=flat&logo=sqlite)](https://www.sqlite.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Table of Contents

1. [Project Introduction](#-project-introduction)
2. [Problem Statement](#-problem-statement)
3. [Project Objectives](#-project-objectives)
4. [Main Features](#-main-features)
5. [Technology Stack](#-technology-stack)
6. [Project Architecture](#-project-architecture)
7. [Installation & Setup](#-installation--setup-instructions)
8. [How to Run in Android Studio](#-how-to-run-the-application-in-android-studio)
9. [Permissions Required](#-permissions-required)
10. [Privacy & Security](#-privacy--security)
11. [Future Improvements](#-future-improvements)

---

## 🌉 Project Introduction

**SignBridge AI** (`com.vaibhav.signbridgeai`) is a native Android accessibility application designed specifically for **deaf and hard-of-hearing individuals**. Built entirely in **Java with XML layouts** using Android Studio, it transforms nearby spoken conversations into readable, high-contrast live captions displayed on the user's mobile screen in real time.

The app bridges the communication gap between deaf individuals and the hearing world — during doctor consultations, classroom lectures, store visits, family discussions, or any face-to-face situation — without requiring any third-party account, subscription, or cloud upload.

---

## ❗ Problem Statement

Approximately **430 million people worldwide** experience disabling hearing loss (WHO, 2023). In everyday situations, deaf and hard-of-hearing individuals often face:

- **Communication barriers** during medical consultations, job interviews, public services, and social interactions.
- **Dependency on interpreters**, who are expensive, unavailable on demand, and not always accessible in rural areas.
- **Inadequate real-time tools**: Existing captioning apps are often cloud-dependent, require data connectivity, demand accounts/subscriptions, or do not support Indian regional languages like Hindi and Marathi.
- **Privacy risks**: Many apps transmit audio to external servers for processing, raising concerns about conversation confidentiality.

There is a clear need for a **free, privacy-first, offline-capable, multilingual live captioning app** that works immediately without barriers.

---

## 🎯 Project Objectives

1. **Accessibility First**: Provide a free, open-source communication tool that deaf and hard-of-hearing users can install and use immediately.
2. **Manual Microphone Control**: The microphone must **never** activate automatically — listening only starts when the user explicitly taps **Start Listening**.
3. **Multilingual Support**: Support 30+ languages and regional dialects including Hindi, Marathi, English (India/US/UK), Spanish, French, and more.
4. **Local Privacy**: All conversation data must remain entirely on-device in a local SQLite database — no cloud accounts required.
5. **Two-Way Communication**: Enable deaf users to respond to speakers using Text-to-Speech (TTS) quick replies and typed input.
6. **Reliable Speech Engine**: Implement a hardened speech recognizer with anti-loop protection, exponential backoff, and graceful error recovery.
7. **Accessibility UX**: High-contrast UI, adjustable caption font sizes (18sp–38sp), haptic feedback, and auto-scroll behavior.

---

## ✨ Main Features

### 🎙️ 1. Real-Time Live Captions
- Continuous speech recognition using Android's built-in `SpeechRecognizer` API.
- **Partial (tentative) transcriptions** shown in a floating "Hearing now…" card while speech is in progress.
- **Final transcriptions** appended to the captions timeline as conversation bubbles with timestamps.
- **5-bar visual sound level meter** (via `onRmsChanged`) — gives deaf users immediate visual confirmation that audio is being detected.

### 🌐 2. Multilingual Support (30+ Languages)
- English (US, India, UK, Australia, Canada), Hindi, Marathi, Spanish, French, German, Italian, Portuguese, Arabic, Japanese, Korean, Chinese, and more.
- Language selection via quick-access chip bar on the Home screen and a full country/language bottom sheet picker with regional filters and flag badges.

### 🎛️ 3. Manual Listening Control & Anti-Loop Safeguards
- Zero auto-listening: microphone is never activated on app open.
- **Pause / Resume / Stop** session controls.
- Exponential backoff for transient errors; maximum consecutive retry limits to protect battery and performance.
- All `SpeechRecognizer` calls run on the main Android thread; engine is cleanly destroyed on activity exit.

### 🔤 4. Two-Way Communication (TTS Replies)
- Deaf users can tap **Quick Reply chips** (e.g., *"Yes, I understand"*, *"Please repeat that slowly"*, *"I am deaf, thank you for speaking clearly"*) to speak aloud through the device speaker.
- Full typed-reply input field for custom responses.

### 💾 5. Conversation History (SQLite)
- All sessions automatically saved to an on-device SQLite database.
- **History screen** with full-text search, session duration, caption count, and date/time.
- **Transcript Detail screen** for reviewing saved conversations.
- Individual session deletion with confirmation dialog.

### ⚙️ 6. Settings & Personalization
- Default language preference stored in SharedPreferences.
- Caption font-size zoom controls (18sp / 24sp / 30sp / 38sp) within the Live Captions screen.
- High-contrast display mode for low-vision users.
- Haptic feedback toggle.

### ℹ️ 7. About Screen
- App version, purpose, open-source attribution, and accessibility mission statement.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Java 11 |
| **UI Framework** | Android Views (XML Layouts) |
| **Design System** | Material Design 3 (Material Components) |
| **Speech Recognition** | Android `SpeechRecognizer` API (system speech engine) |
| **Text-to-Speech** | Android `TextToSpeech` API |
| **Local Database** | SQLite via `SQLiteOpenHelper` |
| **Preferences** | Android `SharedPreferences` |
| **Build System** | Gradle (Kotlin DSL — `build.gradle.kts`) |
| **Min SDK** | API 24 (Android 7.0 Nougat) |
| **Target SDK** | API 36 |
| **IDE** | Android Studio (Ladybug / Koala or newer) |

---

## 🏗️ Project Architecture

The project follows a **single-module, layered architecture** with clear separation of concerns:

```
com.vaibhav.signbridgeai/
│
├── SignBridgeApp.java              # Application class (app-level init)
│
├── SplashActivity.java            # Launcher splash screen
├── MainActivity.java              # Home screen (stats, language picker, recent sessions)
├── LiveCaptionsActivity.java      # Core live captioning screen
├── HistoryActivity.java           # Saved sessions list with search
├── TranscriptDetailActivity.java  # Full transcript viewer
├── SettingsActivity.java          # App preferences
├── AboutActivity.java             # About / mission screen
│
├── adapter/
│   ├── CaptionAdapter.java        # RecyclerView adapter for caption bubbles
│   ├── LanguageAdapter.java       # Language list adapter for bottom sheet
│   └── SessionAdapter.java        # RecyclerView adapter for history sessions
│
├── db/
│   ├── DatabaseHelper.java        # SQLiteOpenHelper — schema & migrations
│   └── SessionRepository.java     # CRUD operations for sessions and captions
│
├── dialog/
│   └── LanguageSelectBottomSheet.java  # Country/language picker bottom sheet
│
├── model/
│   ├── CaptionItem.java           # Individual utterance data model
│   ├── ConversationSession.java   # Session metadata model
│   └── LanguageItem.java          # Country + language + flag model
│
├── speech/
│   └── SpeechManager.java         # Hardened SpeechRecognizer controller (anti-loop)
│
├── tts/
│   └── TtsManager.java            # TextToSpeech speaker output manager
│
└── util/
    ├── AppPreferences.java        # SharedPreferences wrapper
    ├── HapticHelper.java          # Haptic/vibration feedback utilities
    └── LanguageHelper.java        # Registry of 30+ languages and countries
```

### Data Flow Diagram

```
User Taps "Start Listening"
         │
         ▼
MainActivity ──► LiveCaptionsActivity
                        │
                        ▼
                  SpeechManager
              (Android SpeechRecognizer)
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
     Partial Results         Final Results
    (tentative card)     (caption bubble)
                              │
                              ▼
                     SessionRepository
                       (SQLite DB)
                              │
                              ▼
                     HistoryActivity
                  TranscriptDetailActivity
```

---

## 🚀 Installation & Setup Instructions

### Prerequisites

- **Android Studio** Ladybug (2024.2.1), Koala (2024.1.1), Hedgehog (2023.1.1), or newer.
- **Java Development Kit (JDK)**: JDK 11 or JDK 17 (bundled with Android Studio).
- **Android SDK**: API level 24 or higher (Android 7.0+).
- **Git**: Installed and configured.

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Vaibhavchavhan91/signbridge-ai.git
cd signbridge-ai
```

### Step 2 — Open in Android Studio

1. Launch **Android Studio**.
2. Select **File → Open**.
3. Navigate to and select the `signbridge-ai` folder.
4. Android Studio will detect the Gradle project and begin syncing automatically.

### Step 3 — Sync Gradle

Wait for the Gradle sync to complete (bottom status bar). If it fails:
- Ensure you have an active internet connection for the first sync (downloads dependencies).
- Check **File → Project Structure → SDK Location** matches your local Android SDK path.

### Step 4 — Build the Project

**Via Android Studio:**
- Go to **Build → Make Project** (`Ctrl+F9` / `Cmd+F9`).

**Via command line (Gradle wrapper):**
```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

> **Note:** The `.env` file (for Gemini API key) is excluded from version control. The app works fully without it — the API key placeholder in `.env.example` is for optional AI features only.

---

## ▶️ How to Run the Application in Android Studio

### Option A — Run on a Physical Android Device (Recommended)

Speech recognition requires a real microphone and a working speech engine.

1. On your Android phone, go to **Settings → About Phone**.
2. Tap **Build Number** 7 times to unlock **Developer Options**.
3. In **Developer Options**, enable **USB Debugging**.
4. Connect your phone to your computer via USB cable.
5. Accept the **"Allow USB Debugging"** dialog on your phone.
6. In Android Studio, select your device from the **deployment target dropdown**.
7. Click the **Run** button ▶️ (or press `Shift+F10`).
8. Once the app launches, tap **Start Listening** and **grant the microphone permission**.

### Option B — Run on Android Emulator

1. Go to **Tools → Device Manager** → **Create Device**.
2. Choose a phone (e.g., Pixel 6) with **API 30+**.
3. Select a system image with **Google APIs** (required for speech recognition).
4. Launch the emulator, then run the app as above.

> ⚠️ **Emulator limitation**: Speech recognition on the emulator requires the emulator to have Google Play Services and a working internet connection. For best results, use a physical device.

### Offline Speech Recognition (Optional)

For offline speech recognition, on your Android device:
1. Go to **Settings → System → Languages → Voice Input**.
2. Select **Offline speech recognition**.
3. Download the desired language packs (e.g., English, Hindi, Marathi).

---

## 🔒 Permissions Required

| Permission | Purpose | When Requested |
|---|---|---|
| `RECORD_AUDIO` | Captures microphone audio for speech recognition | When user taps **Start Listening** for the first time |
| `VIBRATE` | Provides haptic feedback when listening starts/stops | Granted automatically (no user dialog) |
| `INTERNET` | Required by Android `SpeechRecognizer` for cloud fallback processing | Granted automatically (no user dialog) |

> The app **never** records audio in the background. The microphone is activated **only** when the user explicitly taps the Start Listening button inside the Live Captions screen.

---

## 🔐 Privacy & Security

- **No Secret Recording**: The microphone is never activated automatically or in the background.
- **No Cloud Uploads of Conversations**: All saved transcripts reside strictly in the on-device SQLite database (`signbridge_conversations.db`). Conversations are never uploaded to any server.
- **No Account Required**: No login, email, phone number, or registration of any kind.
- **No Hardcoded API Keys**: The app does not embed or package any private API keys, signing credentials, or secrets. The `.env` file is excluded from version control via `.gitignore`.
- **Speech Engine Transparency**: `Android SpeechRecognizer` delegates to the system speech engine (typically Google Speech Services). Depending on the device, audio may be processed via Google's servers for recognition. Users who require fully offline processing can download offline language packs from their device settings.
- **User Data Control**: Users can clear live captions at any time and permanently delete saved sessions with a confirmation prompt.
- **Open Source**: Full source code is publicly available for audit at [github.com/Vaibhavchavhan91/signbridge-ai](https://github.com/Vaibhavchavhan91/signbridge-ai).

---

## 🔮 Future Improvements

| Feature | Description |
|---|---|
| **Offline On-Device Speech AI** | Integrate a fully local speech model (e.g., OpenAI Whisper, Vosk) to eliminate reliance on Google Speech Services and enable truly offline captioning. |
| **Sign Language Output** | Display animated Indian Sign Language (ISL) or American Sign Language (ASL) hand signs corresponding to recognized speech. |
| **Speaker Diarization** | Distinguish between multiple speakers in a conversation (e.g., "Speaker 1:", "Speaker 2:") for clearer multi-party transcripts. |
| **Caption Export** | Allow users to export saved conversation transcripts as `.txt`, `.pdf`, or `.srt` subtitle files. |
| **Font & Theme Customization** | User-selectable font families, additional high-contrast and dark themes, and custom color pickers. |
| **Wearable Integration** | Display live captions on Android Wear OS smartwatches for hands-free accessibility. |
| **Widget Support** | Android home screen widget showing the last captured caption or quick-launch button. |
| **Real-Time Translation** | Auto-translate recognized speech to the user's preferred language using the ML Kit Translation API. |
| **Cloud Sync (Optional Opt-in)** | Optional encrypted cloud backup of conversation history for users who consent. |
| **Tablet & Foldable Optimization** | Adaptive layouts for large screens, tablets, and foldable Android devices. |
| **Accessibility Audit** | Full TalkBack / screen-reader accessibility audit and compliance pass. |

---

## 👨‍💻 Developer

**Vaibhav Chavhan**
- GitHub: [@Vaibhavchavhan91](https://github.com/Vaibhavchavhan91)
- Repository: [github.com/Vaibhavchavhan91/signbridge-ai](https://github.com/Vaibhavchavhan91/signbridge-ai)

---

## 📄 License

This project is licensed under the **MIT License**.  
See the [LICENSE](LICENSE) file for details.

---

*Built with ❤️ to make the world more accessible for the deaf and hard-of-hearing community.*
