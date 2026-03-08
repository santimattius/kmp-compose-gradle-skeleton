# KMP Compose Gradle Skeleton

A modern, production-ready skeleton for **Kotlin Multiplatform (KMP)** development, featuring **Compose Multiplatform** for shared UI across Android and iOS. 

This project implements the latest industry standards, including **AGP 9.1.0**, **Kotlin 2.3.10**, and the new **Android Kotlin Multiplatform Library** plugin structure.

## 🚀 Project Overview

This repository provides a modular architecture designed for maximum code sharing without compromising platform-specific capabilities.

### Module Structure

| Module | Purpose | Plugin / Type |
| :--- | :--- | :--- |
| **`:composeApp`** | Shared business logic, UI (Compose), and resources. | `com.android.kotlin.multiplatform.library` |
| **`:androidApp`** | Android-specific entry point and configuration. | `com.android.application` |
| **`iosApp`** | Xcode project for the iOS application. | Swift / SwiftUI |

- **`commonMain`**: Shared UI components, ViewModels, and logic.
- **`androidMain` / `iosMain`**: Platform-specific implementations for APIs like Ktor engines or local storage.

## 🛠 Tech Stack

- **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (1.10.4)
- **Dependency Injection**: [Koin](https://insert-koin.io/) (4.1.1)
- **Networking**: [Ktor](https://ktor.io/) (3.4.1)
- **Image Loading**: [Coil 3](https://coil-kt.github.io/coil/) (Multiplatform support)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) (Multiplatform)
- **Resources**: [Compose Multiplatform Resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html)
- **Build System**: Gradle 9.4.0 with Version Catalogs (`libs.versions.toml`)

## 📋 Prerequisites

Ensure your development environment meets the following requirements:

- **JDK**: 17 or higher (Required by AGP 9.0+)
- **Android Studio**: Latest version (Ladybug or newer recommended for AGP 9 compatibility)
- **Xcode**: 15.0+ (for iOS development)
- **Cocoapods**: Required for iOS framework integration.

### Environment Validation
Use [KDoctor](https://github.com/Kotlin/kdoctor) to ensure everything is configured correctly:
```bash
brew install kdoctor
kdoctor
```

## ⚙️ Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/santimattius/kmp-compose-gradle-skeleton.git
cd kmp-compose-gradle-skeleton
```

### 2. Android Execution
Open the project in Android Studio. Select the `androidApp` run configuration and click **Run**.

Alternatively, via CLI:
```bash
./gradlew :androidApp:installDebug
```

### 3. iOS Execution
- **Via Android Studio**: Use the [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) to run the `iosApp` configuration directly.
- **Via Xcode**: Open `iosApp/iosApp.xcodeproj`, select your target device/simulator, and press **Cmd + R**.

## 🏗 Key Features

- **Type-Safe Resources**: Shared strings, drawables, and fonts via Compose Resources.
- **Unified Dependency Management**: Centralized `libs.versions.toml` for consistent versions across modules.
- **Modern AGP Integration**: Utilizes the new modular DSL for Android Multiplatform libraries.
- **MVI/MVVM Ready**: Architectural patterns supported by shared ViewModels and Lifecycle-aware components.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
