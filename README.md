# PaperScreen

A minimalist, privacy-focused Android launcher and reading environment.

![Android 7.0+](https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-Yes-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Release v1.0](https://img.shields.io/badge/Release-v1.0-blue.svg)

--------------------------------------------------

## 🚀 Feature Highlights

**Minimalist Android Launcher**
- Works as an Android Home launcher
- Discovers installed applications locally
- Adaptive application grid
- Local app search
- External app icons retain their original colors

**📖 Paper Reader**
- EPUB reading
- TXT reading
- Bookmarks
- Persistent highlights
- Notes
- In-book search

**📄 Paper Viewer**
- PDF viewing
- TXT viewing
- JPG/JPEG, PNG, WEBP viewing
- External `ACTION_VIEW` integration
- `content://` URI support
- EPUB bridge to Paper Reader

**📚 Offline Dictionary**
- English seed dictionary
- Hindi seed dictionary
- Offline lookup
- Word selection
- No cloud dictionary service

**💬 Help & Feedback**
- Report problems
- Suggest ideas
- Give feedback
- GitHub project access

**🔒 Privacy**
- Offline-first architecture
- No analytics
- No telemetry
- No Firebase
- No GitHub API
- No background tracking
- No `QUERY_ALL_PACKAGES`
- No WebView

--------------------------------------------------

## Design

PaperScreen UI uses a restrained paper-and-ink visual system. External installed-app icons retain their original Android colors.

- **Paper (Background):** `#D8D6CF`
- **Ink (Text/Foreground):** `#444444`

--------------------------------------------------

## Supported Content

| Format | Support |
|--------|---------|
| PDF | Viewer |
| TXT | Viewer + Reader features where applicable |
| EPUB | Paper Reader |
| JPG/JPEG | Viewer |
| PNG | Viewer |
| WEBP | Viewer |

--------------------------------------------------

## Requirements

- **OS:** Android 7.0 (API 24) or newer
- **Target SDK:** 36
- PaperScreen can optionally be selected as the default Home app

--------------------------------------------------

## Download

Use the official GitHub Release to download the application.

[**PaperScreen v1.0 Release**](https://github.com/Ram-Dev-tech/PaperScreen/releases/tag/v1.0)

> **Note:** PaperScreen v1.0 is currently distributed as an unsigned release APK.

**File:** `PaperScreen-v1.0.apk`  
**SHA-256:** `85e97f0bd6bb88f49522382cf4cf7bc253e763f4c798819ae74fdfd43cc7e85f`

You can verify the checksum of the downloaded APK by running the following command in your terminal:
```bash
sha256sum PaperScreen-v1.0.apk
```

--------------------------------------------------

## Build from Source

To build PaperScreen from source, follow these steps:

```bash
git clone https://github.com/Ram-Dev-tech/PaperScreen.git
cd PaperScreen
./gradlew assembleDebug
```

For release builds:

```bash
./gradlew assembleRelease
```
*Note: A release build may be unsigned depending on your local signing configuration.*

--------------------------------------------------

## Privacy

PaperScreen is designed with a strict privacy-first architecture:

- No analytics
- No telemetry
- No Firebase
- No background tracking
- No GitHub API
- No WebView
- No `QUERY_ALL_PACKAGES`
- Offline-first core functionality
- The external browser is only used when the user explicitly chooses GitHub feedback or project links. Opening an external GitHub link through `ACTION_VIEW` hands control to the user's chosen external application.

--------------------------------------------------

## Architecture

At a high-level, PaperScreen is composed of the following modules:

- **Launcher** → PaperScreen Home
- **Reader** → EPUB / TXT
- **Viewer** → PDF / TXT / Images
- **Dictionary** → Offline Room-backed dictionaries

--------------------------------------------------

## Feedback & Support

To get help or provide feedback, please visit our GitHub Issues page:

🐛 [Report a problem](https://github.com/Ram-Dev-tech/PaperScreen/issues)  
💡 [Suggest an idea](https://github.com/Ram-Dev-tech/PaperScreen/issues)  
💬 [Give feedback](https://github.com/Ram-Dev-tech/PaperScreen/issues)

[**Visit the GitHub Project**](https://github.com/Ram-Dev-tech/PaperScreen)

--------------------------------------------------

## Roadmap

Future plans for PaperScreen include:

- Improve device compatibility
- Expand dictionary datasets
- Improve launcher customization
- Improve reader/viewer performance

--------------------------------------------------

## License

This project is licensed under the [MIT License](LICENSE).

--------------------------------------------------

## Credits

PaperScreen is built using native Android technologies including Kotlin and Jetpack Compose.