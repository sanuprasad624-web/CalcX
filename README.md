# CALCX — Scientific Calculator & Engineering Suite

> **Calculate Anything. Understand Everything.**  
> *Made by Aman Prasad*

CALCX is an advanced, offline-first Android scientific calculator, 2D Cartesian & implicit graphing engine, symbolic calculus solver, and comprehensive JEE exam toolkit built with modern Jetpack Compose and Kotlin.

---

## ✨ Features

- **100% Offline-First**: Zero external server or AI API key requirement. Works completely offline.
- **Interactive 2D Graphing Engine**: Real-time implicit equations (e.g. $x^2 + y^2 = 25$) and standard functions, adaptive grid lines, dynamic pinch-to-zoom axis labels, and automatic color cycling.
- **Desmos-Style Keypads**: Dedicated 123 calculator keypad and QWERTY math variable keyboard.
- **Calculus Suite**: Symbolic and numerical differentiation ($n$-th order), indefinite integration, and numerical definite integration via adaptive Simpson's rule.
- **Complete JEE & Engineering Toolkit**:
  - Physics Engine & Constant Library (SI units, dimensional analysis)
  - Chemistry Periodic Table & Molar Mass Engine
  - Matrix Algebra & Systems of Linear Equations ($n \times n$)
  - Programmer Base Conversions (Hex, Dec, Oct, Bin, Two's Complement)
  - Unit Converter (Length, Mass, Temperature, Pressure, Energy, etc.)
  - Statistics & Probability Engine
  - Financial Calculator (Compound Interest, Loan EMI, TVM)
  - Date & Time Arithmetic
- **Local Persistence**: Local Room database for calculation history, bookmarks, and persistent engineering notes.

---

## 🛠️ Local Build Instructions

### Prerequisites
- JDK 21 (Temurin / OpenJDK 21)
- Android SDK (API level 36)

### Commands

```bash
# Clone the repository
git clone https://github.com/<username>/calcx.git
cd calcx

# Ensure Gradle wrapper is executable
chmod +x gradlew

# Run unit tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Output APK location
ls -la app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚀 Automated CI/CD (GitHub Actions)

The project includes an automated GitHub Actions pipeline in `.github/workflows/android.yml`.

### Workflow Capabilities
- **Triggers**:
  - Push to `main` or `master` branches
  - Pull Requests targeting `main` or `master`
  - Version tag pushes (e.g., `git tag v1.0.0 && git push origin v1.0.0`)
  - Manual triggers via **Actions -> Run workflow** (`workflow_dispatch`)
- **Pipeline Steps**:
  1. Check out repository code
  2. Set up JDK 21 (Temurin)
  3. Validate Gradle Wrapper integrity
  4. Cache Gradle dependencies
  5. Run JVM unit tests (`testDebugUnitTest`)
  6. Assemble APK (`assembleDebug` or `assembleRelease`)
  7. Verify APK existence, size, and compute SHA-256 checksum
  8. Package with standard naming: `CALCX-v<version>-<flavor>.apk`
  9. Upload APK as workflow artifact
  10. Automatically publish GitHub Release when a version tag (`v*`) is pushed

### Downloading the Built APK
1. Go to the **Actions** tab in the GitHub repository.
2. Select the latest workflow run.
3. Scroll down to the **Artifacts** section.
4. Download `CALCX-v1.0.0-debug.apk` (or release APK).
5. On tagged releases, the APK is directly attached to the **Releases** page under Assets.

### GitHub Secrets (Optional for Release Signing)

The build and tests **never fail** if secrets are missing. If release signing secrets are not configured, the pipeline builds and packages a signed debug APK.

To enable production release signing, configure these repository secrets under **Settings -> Secrets and variables -> Actions**:

| Secret Name | Description |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded release keystore (`.jks`) file |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the release keystore |
| `ANDROID_KEY_ALIAS` | Alias name of the release key |
| `ANDROID_KEY_PASSWORD` | Password for the release key |

---

## 👨‍💻 Author

**CALCX**  
*Made by Aman Prasad*  
Calculate Anything. Understand Everything.
