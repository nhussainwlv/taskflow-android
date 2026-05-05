# Guide — TaskFlow Native (Android)

This document explains how the **TaskFlow native Android application** works and how to build and run it. It pairs with the TaskFlow PWA for dissertation evaluation (same product concept implemented on the Android platform).

---

## What this app is

TaskFlow for Android is a **native task-management** client with a Material Design UI: boards/columns, tasks with tags and assignees, filtering, saved views, notifications for reminders, and settings aligned with accessibility themes (e.g. contrast).  

It is implemented as a standard **Android Studio** project using **Java**, **AndroidX**, **Room**, **Navigation**, **WorkManager**, **DataStore**, and **Material Components**.

---

## How it works (architecture)

### Package and entry

- **Application ID / namespace:** `com.taskflow`
- **Launcher activity:** `com.taskflow.ui.MainActivity` (see `AndroidManifest.xml`)
- **Application class:** `TaskFlowApplication` — global setup (e.g. strict mode in debug).

### Data layer

- **Room** (`TaskFlowDatabase`) stores entities such as tasks, columns, boards, tags, users, saved views, and task–tag relations.
- **DAOs** (`TaskDao`, `ColumnDao`, etc.) expose queries and updates; **LiveData** is used for observable UI updates where applicable.
- **`TaskRepository`** is the main façade used by ViewModels: database work runs on a **background executor**; UI updates are marshalled back to the main thread as needed.
- On **first database creation**, sample users, tags, columns, and tasks are **seeded** so evaluators can explore features immediately.

### UI layer

- **View binding** is enabled (`buildFeatures { viewBinding true }`).
- **Navigation Component** connects fragments and handles the app graph.
- Bottom sheets and Material components implement quick-add, task detail, and settings flows.

### Background behaviour

- **WorkManager** supports deferred/background work (see manifest startup provider).
- **Notification** receivers handle task reminders and **boot completed** rescheduling where configured.

### Deep links and shortcuts

- Custom scheme: `taskflow://task/...` (see manifest intent filters).
- **App shortcuts** XML provides launcher shortcuts (see `meta-data` for `android.app.shortcuts`).

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Android Studio** | Recommended: latest stable with Android SDK installed. |
| **JDK 17** | The module targets Java 17 (`compileOptions` in `app/build.gradle`). |
| **Android SDK** | **compileSdk 33**, **minSdk 24**, **targetSdk 33** (see `app/build.gradle`). |
| **Device or emulator** | API **24+** (Android 7.0 or newer). |

---

## How to run (Android Studio)

1. Open **Android Studio**.

2. **File → Open…** and select the project folder:

```text
TaskFlow Native (Android) 2365963
```

Wait for Gradle sync to finish.

3. Choose a **device**: physical phone with USB debugging, or a **virtual device** (AVD) with API ≥ 24.

4. Click **Run** (green triangle) or press **Shift+F10** (Windows/Linux) / **Ctrl+R** (macOS may vary).  

   Studio builds the **debug** variant and installs `com.taskflow` on the device.

5. Launch **TaskFlow** from the app drawer.

### First run

If the database is new, you should see **seeded demo data** (sample users, tasks, columns) similar in spirit to the PWA demo dataset.

---

## How to run from Terminal (macOS / Linux)

With the Android SDK and Gradle wrapper available:

```bash
cd "/Users/adrian/Documents/Project & Professionalism Module/Final Disseration/TaskFlow Native (Android) 2365963"
./gradlew installDebug
```

Ensure a device or emulator is connected (`adb devices`). Then open the app on the device.

### Build debug APK only

```bash
./gradlew assembleDebug
```

Output is typically under `app/build/outputs/apk/debug/`.

---

## Release builds

Release builds use **minification** and **resource shrinking** (`minifyEnabled true`, `shrinkResources true`) with ProGuard rules in `app/proguard-rules.pro`.  

Use **Build → Generate Signed Bundle / APK** in Android Studio for store-ready artefacts, or `./gradlew assembleRelease` with signing configured.

---

## Troubleshooting

| Issue | What to try |
|-------|-------------|
| Gradle sync failed | Open SDK Manager; install **API 33** platform and build tools; accept licences (`sdkmanager --licenses`). |
| JDK version errors | Point Android Studio to **JDK 17** (Settings → Build → Gradle → Gradle JDK). |
| Install failed | Enable **USB debugging**; authorize the computer; check `adb devices`. |
| Emulator slow | Use a **system image** with Google APIs x86_64; allocate enough RAM in AVD settings. |

---

## Project layout (high level)

```
TaskFlow Native (Android) 2365963/
├── app/
│   ├── build.gradle           # Module config, dependencies, SDK versions
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/taskflow/ # Application, UI, data, models
│   │   └── res/               # Layouts, themes, strings, drawables
│   └── proguard-rules.pro
├── build.gradle               # Top-level Gradle / plugins
└── settings.gradle
```

---

## Relationship to the PWA

Both apps implement the **same product scenario** for comparison: task boards, filters, persistence on device, and accessibility-oriented settings.  

They **do not share a live backend** in the dissertation artefact — data stays **local** (Room on Android; localStorage in the PWA).  

For running the browser version, see **guides-for-pwa.md**.

---

*TaskFlow Native (Android) — dissertation artefact. Pair with **guides-for-pwa.md** for the Progressive Web App.*
