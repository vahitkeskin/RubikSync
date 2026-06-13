# 🧪 RubikSync Testing Instructions

Welcome to the test suite of **RubikSync**. We have integrated a clean and professional testing architecture conforming to Kotlin Multiplatform (KMP) standards, including both **Unit Tests** and **Compose UI Tests**.

---

## 🚀 How to Run Tests

You can run these tests directly from your terminal or command-line interface.

### 1. Run All Tests
To run all test suites across all targets:
```bash
./gradlew allTests
```

### 2. Run JVM/Desktop Tests (Recommended for speed)
To run all common unit and UI tests on the desktop JVM target:
```bash
./gradlew :shared:jvmTest
```

### 3. Run Android Host Unit Tests
To run unit tests on the Android host environment (robolectric or JVM mock-based):
```bash
./gradlew :shared:testAndroidHostTest
```

### 4. Run Android Instrumented UI Tests (Device/Emulator)
To run UI tests directly on a connected Android phone or active emulator (this will physically open the app and click the buttons automatically):
```bash
./gradlew :androidApp:connectedDebugAndroidTest
```

---

## 🎯 Run Specific Actions / Tests (Selectively)

As requested, each core action of the application has been mapped to an isolated test function. You can run any specific action test by using the Gradle `--tests` filtering flag.

### A. Scramble Action (`scramble`)
Verify that the scramble logic successfully randomizes the cube state:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testScrambleAction"
```

### B. Solve Action (`solve`)
Verify that `RubikSolver` (Two-Phase Coset Engine) successfully generates a solution and solves the cube:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testSolveAction"
```

### C. Reset Action (`reset`)
Verify that resetting a scrambled cube returns it back to its initial solved state:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testResetAction"
```

### D. Change Language Action (`dil değiştir`)
Verify that updating the application language updates localized strings correctly:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testChangeLanguageAction"
```

### E. Change Theme Action (`tema değiştir`)
Verify that switching between dark and light themes updates the AppState theme configuration:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testChangeThemeAction"
```

### F. Sound Action (`ses aç/kapat`)
Verify sound enabled/disabled state toggles:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testSoundSettingsAction"
```

### G. Timer Action (`kronometre`)
Verify starting, pausing, and resetting the solve timer:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testTimerAction"
```

### H. UI Rendering Test (`UI testleri`)
Verify UI element rendering and click actions using Compose Multiplatform UI testing:
```bash
./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUiTest"
```
