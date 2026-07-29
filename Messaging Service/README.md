# Nexus Hybrid Messaging 💬⚡

[![Kotlin](https://img.shields.io/badge/Language-Kotlin%201.9.22-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM%20%2B%20UDF-green.svg)](https://developer.android.com/topic/architecture)
[![Encryption](https://img.shields.io/badge/Security-SQLCipher%20%2B%20E2EE-red.svg)](https://www.zetetic.net/sqlcipher/)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)](LICENSE)

An Android-native hybrid messaging application foundation that combines the **End-to-End Encryption (E2EE)** and simplicity of **WhatsApp / iMessage** with the **Server/Channel & Voice Room structure** of **Discord**.

---

## 🌟 Key Features

### 📩 Direct Messages & Group Chats (WhatsApp / iMessage Style)
- **E2EE Signal Protocol Hooks**: Built-in state contracts for end-to-end payload encryption.
- **Rich Media & Voice Notes**: Interactive waveform player bar with play/pause controls.
- **iMessage Reaction Overlay**: Quick emoji reaction picker popover (`❤️`, `👍`, `🔥`, `😂`, `😮`, `🚀`).
- **Read Receipts & Typing Indicators**: Dynamic `✓✓` sent/read ticks and active typing notifications.
- **Smart Scroll Behavior**: Unread badge floating action button to jump to latest messages seamlessly.

### 🛡️ Servers & Channels (Discord Style)
- **Multi-Server Navigation Drawer**: Collapsible left sidebar rail with active server indicators and creation triggers.
- **Nested Categories & Channels**: Hierarchical organization supporting Text (`#`) and Voice (`🔊`) channels.
- **Active Voice Status Banner**: Persistent connection indicator with single-tap mute, deafen, and disconnect controls.

### 🧭 Hybrid Bottom Navigation
Switch seamlessly between:
- 💬 **Chats**: Direct Messages & E2EE Group Conversations.
- 🌐 **Spaces**: Discord-style Server Community Channels.
- 📞 **Calls**: WebRTC Audio/Video call session history.
- ⚙️ **Settings**: Profile, security keys, and Signal identity fingerprint manager.

---

## 🏗️ Architecture & Tech Stack

The app adheres to **Android Clean Architecture**, **MVVM**, and **Unidirectional Data Flow (UDF)**.

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│        Jetpack Compose + Material 3 + ViewModels        │
└────────────────────────────┬────────────────────────────┘
                             │ StateFlow & UiEvents
┌────────────────────────────▼────────────────────────────┐
│                    Domain Layer                         │
│             UseCases & Repository Contracts             │
└────────────────────────────┬────────────────────────────┘
                             │ Flows & Data Models
┌────────────────────────────▼────────────────────────────┐
│                     Data Layer                          │
│     MessageRepositoryImpl │ ServerRepositoryImpl        │
└──────────────┬──────────────────────────┬───────────────┘
               │                          │
┌──────────────▼─────────────┐ ┌──────────▼──────────────┐
│  Local Encrypted Database  │ │    Networking Engine    │
│    Room + SQLCipher DB     │ │  Ktor WebSocket Client  │
└────────────────────────────┘ └─────────────────────────┘
```

- **Language**: 100% Modern Kotlin
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Dependency Injection**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- **Local Persistence**: Room Database encrypted via **SQLCipher** (`SupportFactory`)
- **Networking & WebSockets**: Ktor Client CIO engine with exponential backoff reconnect logic & active 25s ping/pong heartbeats
- **Serialization**: KotlinX Serialization for typed JSON socket frames
- **Asynchronous Execution**: Kotlin Coroutines & `StateFlow` / `SharedFlow`

---

## 📁 Project Structure

```text
app/src/main/java/com/hybrid/messaging/
├── MessagingApp.kt                  # Application entry point (SQLCipher native lib loader)
├── MainActivity.kt                  # Activity host with Compose theme wrapper
├── core/
│   ├── database/                    # Room Database & SQLCipher Encryption
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── dao/                     # UserDao, ChatRoomDao, ServerDao, MessageDao, ReactionDao
│   │   ├── entity/                  # Room Entities
│   │   └── di/                      # DatabaseModule (Hilt SQLCipher factory)
│   ├── domain/                      # Clean Domain Interfaces & State Wrappers
│   │   ├── model/                   # Core Models (User, ChatRoom, Server, Message, Reaction)
│   │   ├── repository/              # Auth, Message, Server, ChatRoom, WebRtc Repositories
│   │   └── util/                    # Resource state (Success, Error, Loading)
│   ├── network/                     # Ktor Engine & WebSocket Manager
│   │   ├── websocket/               # WebSocketManager, SocketFrame, ConnectionState
│   │   └── di/                      # NetworkModule (Ktor Client Hilt Provider)
│   └── data/                        # Repository Implementations
│       ├── repository/
│       └── di/                      # RepositoryModule (Hilt @Binds)
└── feature/                         # Jetpack Compose UI Features
    ├── theme/                       # Dynamic Dark/Light Color tokens & M3 Typography
    ├── chat/                        # ChatScreen, VoiceNotePlayerUI, ReactionPickerSheet
    ├── spaces/                      # SpaceNavigationDrawer, ServerRail, ChannelSidebar
    └── main/                        # MainScaffold & Hybrid Bottom Navigation
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Jellyfish | 2023.3.1 or newer.
- **JDK**: Version 17.
- **Android SDK**: API 34 (Android 14+). Minimum supported API: 26 (Android 8.0).

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/nexus-hybrid-messaging.git
   cd nexus-hybrid-messaging
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle dependencies:
   ```bash
   ./gradlew build
   ```
4. Run on an Android Emulator or physical device connected via ADB.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
