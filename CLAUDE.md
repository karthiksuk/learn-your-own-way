# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application built with Kotlin and Jetpack Compose called "Learn My Own Way". It's a standard Android project using modern Android development practices with Compose UI framework.

## Architecture

- **Build System**: Gradle with Kotlin DSL (build.gradle.kts)
- **UI Framework**: Jetpack Compose with Material3 design system
- **Language**: Kotlin with Java 11 target compatibility
- **Package Structure**: `com.karthik.learnmyownway`
- **Minimum SDK**: 28 (Android 9.0)
- **Target SDK**: 36
- **Compose Compiler**: Kotlin 2.0.21 with Compose plugin

## Key Dependencies

Dependencies are managed through `gradle/libs.versions.toml` using version catalogs:
- androidx.core:core-ktx
- androidx.lifecycle:lifecycle-runtime-ktx 
- androidx.activity:activity-compose
- androidx.compose BOM (2024.09.00)
- androidx.compose.material3
- Testing: JUnit, Espresso, Compose UI testing

## Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Clean build
./gradlew clean

# Build and install debug APK
./gradlew installDebug
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.karthik.learnmyownway.ExampleUnitTest"

# Run instrumented test class
./gradlew connectedAndroidTest --tests "com.karthik.learnmyownway.ExampleInstrumentedTest"
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

## Project Structure

- `app/src/main/java/com/karthik/learnmyownway/` - Main source code
  - `MainActivity.kt` - Entry point activity with Compose setup
  - `ui/theme/` - Compose theme configuration (Color.kt, Theme.kt, Type.kt)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests
- `app/src/main/res/` - Android resources (layouts, strings, etc.)

## Theme and UI

The app uses a custom Compose theme called `LearnMyOwnWayTheme` defined in `ui/theme/Theme.kt`. UI components follow Material3 design patterns with Compose.

## Testing Strategy

- Unit tests use JUnit 4
- Instrumented tests use AndroidX Test with Espresso
- Compose UI tests use `androidx.compose.ui.test.junit4`
- Test runner: `androidx.test.runner.AndroidJUnitRunner`