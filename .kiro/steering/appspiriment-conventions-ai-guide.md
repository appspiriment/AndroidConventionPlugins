---
inclusion: manual
---

# Appspiriment Convention Plugins — AI Coding Tool Reference Guide

This guide is the authoritative reference for AI coding tools working in projects that use the `io.github.appspiriment` convention plugin suite. Read it completely before generating any `build.gradle.kts` content, suggesting plugin IDs, or referencing the version catalog.

---

## 1. Plugin Suite Overview

- **Group ID:** `io.github.appspiriment`
- **Artifact:** `conventions`
- **Published to:** Maven Central
- **Current stable version:** `0.0.14`

### The 4 Plugin IDs

| Plugin ID | Purpose |
|-----------|---------|
| `io.github.appspiriment.application` | `:app` module — full Android app, Hilt + Compose always on |
| `io.github.appspiriment.library` | All library module types — Hilt and Compose are opt-in flags |
| `io.github.appspiriment.data` | Data layer opt-ins (Room, Retrofit, DataStore, Security, WorkManager) |
| `io.github.appspiriment.firebase` | Firebase opt-ins — standalone, apply alongside any module plugin |

### ⛔ Old Plugin IDs That No Longer Exist

The following plugin IDs have been **removed**. Do not generate them, suggest them, or reference them in any context:

- `io.github.appspiriment.library-hilt` — REMOVED
- `io.github.appspiriment.library-compose` — REMOVED
- `io.github.appspiriment.library-hilt-compose` — REMOVED

The replacement is `io.github.appspiriment.library` with `hilt.set(true)` and/or `compose.set(true)` in the `appspiriment { }` block.

---

## 2. Version Catalog — Slim Structure

The distributed `appspirimentlibs.versions.toml` is intentionally minimal. It contains **only**:

### `[versions]` — all version strings

```toml
[versions]
# App & SDK Versions
versionCode = "1"
versionName = "1.0.0"
minSdk = "26"
targetSdk = "36"
compileSdk = "36"
javaVersion = "21"

# Appspiriment Plugin Versions
appspiriment = "0.0.14"
appspirimentLogUtils = "0.0.1"
appspirimentUtils = "0.0.5"
appspirimentComposeUtils = "0.0.6"

# Tooling
agp = "8.13.1"
kotlin = "2.3.10"
ksp = "2.3.6"
hilt = "2.58"
kotlinserializeplugin = "2.3.10"

# Library Versions
coreKtx = "1.17.0"
room = "2.7.2"
kotlinserialize = "1.8.0"
kotlinxCoroutines = "1.10.1"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.12.4"
composeBom = "2026.02.01"
composeNavigation = "2.9.1"
composeHiltNavigation = "1.3.0"
lifecycle = "2.10.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
datastore = "1.1.1"
security = "1.1.0-alpha06"
tink = "1.15.0"
work = "2.10.0"
lottie = "6.6.0"
firebaseBom = "33.14.0"
googleServices = "4.4.3"
firebasecrashlyticsplugin = "3.0.4"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
mockito = "6.2.3"
turbine = "1.1.0"
```

### `[plugins]` — exactly 4 Appspiriment aliases

```toml
[plugins]
appspiriment-application = { id = "io.github.appspiriment.application", version.ref = "appspiriment" }
appspiriment-library     = { id = "io.github.appspiriment.library",     version.ref = "appspiriment" }
appspiriment-data        = { id = "io.github.appspiriment.data",        version.ref = "appspiriment" }
appspiriment-firebase    = { id = "io.github.appspiriment.firebase",    version.ref = "appspiriment" }
```

### What the catalog does NOT contain

- ❌ No `[libraries]` section
- ❌ No `[bundles]` section
- ❌ No standard tooling plugin aliases (`google-android-application`, `kotlin-android`, `dagger-hilt-android`, `devtools-ksp`, `kotlinx-serialization`, `google-services`, `firebase-crashlytics`, etc.)

**Core principle:** The plugin owns all dependency `group:artifact` coordinates. The consumer catalog owns only version strings. The plugin reads versions via `getVersion()` / `findVersion()` — never `findLibrary()` or `findBundle()`.

---

## 3. Root `build.gradle.kts` Pattern

Standard tooling plugins use **string IDs + version from catalog**. Only Appspiriment plugins use `alias()`.

```kotlin
// build.gradle.kts (root)
plugins {
    id("com.android.application")                   version appspirimentlibs.versions.agp.get()                     apply false
    id("com.android.library")                        version appspirimentlibs.versions.agp.get()                     apply false
    id("org.jetbrains.kotlin.android")               version appspirimentlibs.versions.kotlin.get()                  apply false
    id("org.jetbrains.kotlin.plugin.compose")        version appspirimentlibs.versions.kotlin.get()                  apply false
    id("org.jetbrains.kotlin.jvm")                   version appspirimentlibs.versions.kotlin.get()                  apply false
    id("com.google.devtools.ksp")                    version appspirimentlibs.versions.ksp.get()                     apply false
    id("com.google.dagger.hilt.android")             version appspirimentlibs.versions.hilt.get()                    apply false
    id("org.jetbrains.kotlin.plugin.serialization")  version appspirimentlibs.versions.kotlinserializeplugin.get()   apply false
    alias(appspirimentlibs.plugins.appspiriment.application) apply false
    alias(appspirimentlibs.plugins.appspiriment.library)     apply false
    alias(appspirimentlibs.plugins.appspiriment.data)        apply false
    alias(appspirimentlibs.plugins.appspiriment.firebase)    apply false
}
```

**Why string IDs for standard tooling?** Because those plugin aliases (`google-android-application`, `kotlin-android`, etc.) no longer exist in the slim catalog. Using `alias()` for them would cause an unresolved catalog alias error.

---

## 4. Plugin Reference

### 4.1 `io.github.appspiriment.application`

**For:** `:app` module only.

**Behavior:**
- Always enables Hilt + Compose unconditionally (no flags needed)
- Applies internally: `com.android.application`, `kotlin-android`, `kotlin-compose`, `kotlinx-serialization`, `dagger-hilt-android`, `devtools-ksp`
- Auto-adds: core-ktx, lifecycle-runtime-ktx, coroutines, full Compose stack (BOM-managed), Hilt, hilt-navigation-compose, unit-test deps
- Auto-adds appspiriment-utils/logutils (when `enableUtils=true`, the default)
- Auto-adds appspiriment-compose + lottie-compose (when `enableUtils=true` and Compose is active)
- `ui-tooling` is `debugImplementation` only
- Does **not** contain any Firebase logic — use `appspiriment.firebase` alongside it

**Usage:**

```kotlin
// app/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.application)
}

android {
    namespace = "com.example.app"
    defaultConfig {
        applicationId = "com.example.app"
        versionCode = appspirimentlibs.versions.versionCode.get().toInt()
        versionName = appspirimentlibs.versions.versionName.get()
    }
}
```

---

### 4.2 `io.github.appspiriment.library` (CONSOLIDATED)

**For:** All Android library module types — plain, Hilt, Compose, or both.

**Behavior:**
- Default (no flags): plain Android library with base deps + test deps only
- `hilt.set(true)` → adds Hilt plugins + deps
- `compose.set(true)` → adds Compose plugins + deps
- Both flags together → union of both, no duplication
- `hilt-navigation-compose` is added exactly once even when both flags are true
- `kotlinx-serialization` Gradle plugin is added exactly once even when both flags are true

**Usage — plain library:**

```kotlin
plugins { alias(appspirimentlibs.plugins.appspiriment.library) }
android { namespace = "com.example.mylibrary" }
```

**Usage — with Hilt:**

```kotlin
plugins { alias(appspirimentlibs.plugins.appspiriment.library) }
android { namespace = "com.example.domain" }
appspiriment { hilt.set(true) }
```

**Usage — with Compose:**

```kotlin
plugins { alias(appspirimentlibs.plugins.appspiriment.library) }
android { namespace = "com.example.ui.components" }
appspiriment { compose.set(true) }
```

**Usage — with Hilt + Compose (feature modules):**

```kotlin
plugins { alias(appspirimentlibs.plugins.appspiriment.library) }
android { namespace = "com.example.feature.home" }
appspiriment {
    hilt.set(true)
    compose.set(true)
}
```

---

### 4.3 `io.github.appspiriment.data`

**For:** Data layer modules. Opt-in data layer dependencies via `dataLayer { }` extension.

**Behavior:**
- Nothing is added unless explicitly enabled — all options default to off
- Typically combined with `appspiriment.library` + `hilt.set(true)`
- Chucker is handled internally (debugImplementation / releaseImplementation no-op) — NOT in the catalog

**Options:**

| Block | Property | What it adds |
|-------|----------|-------------|
| `room { }` | `enabled` | room-runtime, room-ktx (impl); room-compiler (ksp) |
| `room { }` | `usePaging` | room-paging (impl) |
| `retrofit { }` | `enabled` | okhttp, retrofit, okhttp-logging (impl) |
| `retrofit { }` | `useChucker` | Chucker debugImpl / no-op releaseImpl |
| `retrofit { }` | `useKotlinSerialization` | converter-kotlinx-serialization (impl) |
| `security { }` | `enabled` | security-crypto, tink-android (impl) |
| `dataStore { }` | `enabled` | datastore-preferences (impl) |
| `workManager { }` | `enabled` | work-runtime-ktx, hilt-work, hilt-compiler (impl/ksp) |

**Usage:**

```kotlin
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
    alias(appspirimentlibs.plugins.appspiriment.data)
}
android { namespace = "com.example.data" }
appspiriment { hilt.set(true) }
dataLayer {
    room {
        enabled.set(true)
        usePaging.set(false)
    }
    retrofit {
        enabled.set(true)
        useChucker.set(true)
        useKotlinSerialization.set(false)
    }
    security { enabled.set(true) }
    dataStore { enabled.set(true) }
    workManager { enabled.set(true) }
}
```

---

### 4.4 `io.github.appspiriment.firebase` (NEW — standalone plugin)

**For:** Any module that needs Firebase. Apply alongside any module plugin.

**Behavior:**
- Standalone — does not extend `AndroidBaseConventionPlugin`
- Detects module type via `pluginManager.hasPlugin("com.android.application")`
- Firebase BOM added as `platform()` when any service is enabled
- `crashlytics.set(true)` on an **application** module: auto-applies `google-services` + `firebase-crashlytics` Gradle plugins AND validates `google-services.json` exists (throws `IllegalStateException` at configuration time if missing)
- `crashlytics.set(true)` on a **library** module: only adds the crashlytics dependency, does NOT apply Gradle plugins
- App-level services: `analytics`, `crashlytics`, `auth`, `messaging`
- Data-layer services: `database`, `storage`, `remoteConfig`

**Usage — app module with Firebase:**

```kotlin
// app/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.application)
    alias(appspirimentlibs.plugins.appspiriment.firebase)
}
android {
    namespace = "com.example.app"
    defaultConfig {
        applicationId = "com.example.app"
        versionCode = appspirimentlibs.versions.versionCode.get().toInt()
        versionName = appspirimentlibs.versions.versionName.get()
    }
}
firebase {
    analytics.set(true)
    crashlytics.set(true)   // auto-applies google-services + crashlytics plugins, validates google-services.json
    auth.set(true)
    messaging.set(true)
}
```

**Usage — data module with Firebase:**

```kotlin
// data/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
    alias(appspirimentlibs.plugins.appspiriment.data)
    alias(appspirimentlibs.plugins.appspiriment.firebase)
}
android { namespace = "com.example.data" }
appspiriment { hilt.set(true) }
dataLayer {
    room { enabled.set(true) }
}
firebase {
    database.set(true)
    storage.set(true)
    remoteConfig.set(true)
}
```

---

## 5. The `appspiriment { }` Extension — Full Property Reference

| Property | Type | Default | Applies to | Description |
|----------|------|---------|------------|-------------|
| `enableUtils` | `Property<Boolean>` | `true` | all plugins | Adds appspiriment-utils, logutils-dev (debugImpl), logutils-prod (releaseImpl). Compose modules also get appspiriment-compose + lottie-compose. |
| `enableMinify` | `Property<Boolean>` | `false` | all plugins | Enables R8 minification in the release build type. |
| `addDevSuffixToDebug` | `Property<Boolean>` | `true` | application only | Appends `.dev` to applicationId and `.yyyyMMdd.HHmmss` to versionName in debug builds. |
| `hilt` | `Property<Boolean>` | `false` | library only | Enables Hilt DI — applies Hilt + KSP plugins, adds hilt-android (impl) + hilt-android-compiler (ksp) + kotlinx-serialization-json (impl). |
| `compose` | `Property<Boolean>` | `false` | library only | Enables Jetpack Compose — applies kotlin-compose + kotlinx-serialization plugins, adds full Compose BOM stack, hilt-navigation-compose, ui-tooling (debugImpl). |

**Notes:**
- `hilt` and `compose` flags are read in `afterEvaluate` via `configureCapabilities()` in `AndroidBaseConventionPlugin`. Do not suggest removing `afterEvaluate`.
- `AndroidApplicationConventionPlugin` ignores `hilt` and `compose` flags — it always enables both unconditionally.

---

## 6. The `firebase { }` Extension — Full Property Reference

| Property | Type | Default | Module scope | What it adds |
|----------|------|---------|-------------|-------------|
| `analytics` | `Property<Boolean>` | `false` | App | `com.google.firebase:firebase-analytics-ktx` (BOM-managed) |
| `crashlytics` | `Property<Boolean>` | `false` | App | `com.google.firebase:firebase-crashlytics` (BOM-managed) + `google-services` plugin + `firebase-crashlytics` plugin + `google-services.json` validation (app modules only) |
| `auth` | `Property<Boolean>` | `false` | App | `com.google.firebase:firebase-auth` (BOM-managed) |
| `messaging` | `Property<Boolean>` | `false` | App | `com.google.firebase:firebase-messaging-ktx` (BOM-managed) |
| `database` | `Property<Boolean>` | `false` | Any | `com.google.firebase:firebase-database-ktx` (BOM-managed) |
| `storage` | `Property<Boolean>` | `false` | Any | `com.google.firebase:firebase-storage-ktx` (BOM-managed) |
| `remoteConfig` | `Property<Boolean>` | `false` | Any | `com.google.firebase:firebase-config-ktx` (BOM-managed) |

**Notes:**
- Firebase BOM version comes from `firebaseBom` in `[versions]`. Users can override it.
- Firebase BOM is always added as `platform()` — never as a regular `implementation` dependency.
- `google-services` and `firebase-crashlytics` Gradle plugins are applied only when `crashlytics=true` AND the module is an application module.
- `google-services.json` validation runs in `afterEvaluate` before any plugin is applied. It throws `IllegalStateException` at configuration time with a message naming the expected file path.
- `AndroidFirebaseConventionPlugin` is standalone — it does not extend `AndroidBaseConventionPlugin`.

---

## 7. SDK and Compiler Settings — Auto-Configured, Do NOT Set Manually

The plugins auto-configure all of the following from the catalog. **Never write these in a consumer `build.gradle.kts`:**

| Setting | Source |
|---------|--------|
| `compileSdk` | `appspirimentlibs.versions.compileSdk` |
| `minSdk` | `appspirimentlibs.versions.minSdk` |
| `targetSdk` | `appspirimentlibs.versions.targetSdk` |
| `sourceCompatibility` / `targetCompatibility` | `appspirimentlibs.versions.javaVersion` |
| `kotlinOptions { jvmTarget }` | Kotlin JVM_21 |
| `buildFeatures { compose = true }` | Set by plugin when Compose is active |
| `composeOptions { kotlinCompilerExtensionVersion }` | Handled by plugin |

**Kotlin compiler flags applied automatically:**
- `-opt-in=kotlin.RequiresOptIn`
- `-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi`
- `-opt-in=kotlinx.coroutines.FlowPreview`
- `-Xannotation-default-target=param-property`
- `-Xcontext-parameters`

---

## 8. What NOT to Write — Comprehensive List

Never generate any of the following in consumer `build.gradle.kts` files:

**Android config (plugin handles it):**
- `compileSdk`, `minSdk`, `targetSdk`
- `compileOptions { sourceCompatibility / targetCompatibility }`
- `kotlinOptions { jvmTarget }`
- `buildFeatures { compose = true }`
- `composeOptions { kotlinCompilerExtensionVersion }`

**Dependencies (plugin adds automatically):**
- `core-ktx`, `coroutines`, `lifecycle-runtime-ktx` — base deps, always added
- `hilt-android`, `hilt-android-compiler` — added when `hilt=true`
- `compose-bom`, `ui`, `material3`, `navigation-compose`, `activity-compose`, etc. — added when `compose=true`
- `junit`, `espresso`, `mockito`, `turbine` — test deps, always added
- Any `firebase-*` dependency — use `firebase { }` extension instead
- Firebase BOM — use `firebase { }` extension instead

**Catalog aliases that do not exist:**
- `appspirimentlibs.libraries.*` — no `[libraries]` section in the slim catalog
- `appspirimentlibs.bundles.*` — no `[bundles]` section in the slim catalog
- `appspirimentlibs.plugins.google.android.application` — not in slim catalog
- `appspirimentlibs.plugins.kotlin.android` — not in slim catalog
- `appspirimentlibs.plugins.dagger.hilt.android` — not in slim catalog
- `appspirimentlibs.plugins.devtools.ksp` — not in slim catalog
- `appspirimentlibs.plugins.kotlinx.serialization` — not in slim catalog
- `appspirimentlibs.plugins.appspiriment.library.hilt` — REMOVED plugin
- `appspirimentlibs.plugins.appspiriment.library.compose` — REMOVED plugin
- `appspirimentlibs.plugins.appspiriment.library.hilt.compose` — REMOVED plugin

---

## 9. Multi-Module Project Layout

### Directory Structure

```
MyApp/
├── gradle/
│   └── appspirimentlibs.versions.toml   ← versions + 4 plugin aliases only
├── app/
│   └── build.gradle.kts                 ← appspiriment.application [+ appspiriment.firebase]
├── feature/
│   ├── home/
│   │   └── build.gradle.kts             ← appspiriment.library + hilt=true + compose=true
│   └── auth/
│       └── build.gradle.kts             ← appspiriment.library + hilt=true + compose=true
├── data/
│   └── build.gradle.kts                 ← appspiriment.library + hilt=true + appspiriment.data [+ appspiriment.firebase]
├── domain/
│   └── build.gradle.kts                 ← appspiriment.library + hilt=true
├── ui-components/
│   └── build.gradle.kts                 ← appspiriment.library + compose=true
├── build.gradle.kts                     ← root
└── settings.gradle.kts                  ← registers appspirimentlibs catalog
```

### Module-to-Plugin Mapping

| Module type | Plugin(s) | `appspiriment { }` flags | `firebase { }` |
|-------------|-----------|--------------------------|----------------|
| App entry point | `appspiriment.application` | — (Hilt+Compose always on) | — |
| App with Firebase | `appspiriment.application` + `appspiriment.firebase` | — | `analytics`, `crashlytics`, `auth`, `messaging` |
| Feature screen (ViewModel + UI) | `appspiriment.library` | `hilt=true`, `compose=true` | — |
| Feature screen (UI only) | `appspiriment.library` | `compose=true` | — |
| Domain / use-case layer | `appspiriment.library` | `hilt=true` | — |
| Data layer | `appspiriment.library` + `appspiriment.data` | `hilt=true` | — |
| Data layer with Firebase | `appspiriment.library` + `appspiriment.data` + `appspiriment.firebase` | `hilt=true` | `database`, `storage`, `remoteConfig` |
| Shared UI components | `appspiriment.library` | `compose=true` | — |
| Pure Kotlin utility | `appspiriment.library` | — | — |

---

## 10. Version Override Capability

Users can override any library version in `[versions]` without waiting for a plugin update. The plugin reads version strings from the consumer catalog at configuration time, so any override takes effect immediately on the next sync.

```toml
# gradle/appspirimentlibs.versions.toml
[versions]
hilt = "2.59"              # override — plugin will use this version for hilt-android and hilt-android-compiler
composeBom = "2026.05.00"  # override — plugin will use this BOM version
room = "2.8.0"             # override — plugin will use this version for all room-* artifacts
firebaseBom = "33.15.0"    # override — firebase plugin will use this BOM version
```

---

## 11. Debug Build Behavior (`addDevSuffixToDebug`)

When `addDevSuffixToDebug = true` (the default for application modules), debug builds automatically get:

- `applicationId` → `com.example.app.dev` (allows side-by-side install with release)
- `versionName` → `1.0.0.20260504.143022` (format: `yyyyMMdd.HHmmss`)

Disable with `addDevSuffixToDebug.set(false)` if CI or testing requires a stable `applicationId`.

This property is **application-only** — it has no effect on library modules.

---

## 12. Upgrading the Plugin

```bash
./gradlew upgradeAppspiriment -PnewVersion=0.0.15
# Then sync Gradle
```

The `upgradeAppspiriment` task is baked into the project template's root `build.gradle.kts`. It updates the `appspiriment` version key in `appspirimentlibs.versions.toml`.

---

## 13. The `:resources` Module

`io.github.appspiriment:resources:1.0.0` is an optional AAR companion module that ships default Material Design 3 color tokens and dimension resources. It is **not** added automatically by any plugin — add it explicitly if needed:

```kotlin
dependencies {
    implementation("io.github.appspiriment:resources:1.0.0")
}
```

Override any value in your module's `src/main/res/` — Android resource merging will prefer your values.

---

## 14. Architecture Notes for AI Tools

These are implementation details that AI tools must respect when suggesting changes to the plugin source code:

- **`afterEvaluate` is intentional** in all plugins. Extension values (`hilt`, `compose`, `firebase { }`, `dataLayer { }`) are only available after the consumer's build script runs. Do not suggest removing `afterEvaluate`.
- **`hilt` and `compose` flags** on `appspiriment.library` are read in `afterEvaluate` via a `configureCapabilities()` hook in `AndroidBaseConventionPlugin`. `AndroidApplicationConventionPlugin` ignores these flags and always enables both.
- **`AndroidFirebaseConventionPlugin` is standalone** — it does not extend `AndroidBaseConventionPlugin`. It detects module type via `pluginManager.hasPlugin("com.android.application")`.
- **`google-services.json` validation** runs in `afterEvaluate` before any plugin is applied. It throws `IllegalStateException` at configuration time.
- **Firebase BOM** is added as `platform()` — never as a regular `implementation` dependency.
- **`hilt-navigation-compose`** is added exactly once even when both `hilt=true` and `compose=true`.
- **`kotlinx-serialization` Gradle plugin** is added exactly once even when both `hilt=true` and `compose=true`.
- **The plugin owns all `group:artifact` coordinates internally.** It only reads version strings from the consumer catalog via `getVersion()`. There are no `findLibrary()` or `findBundle()` calls against the consumer catalog.
- **`ui-tooling`** is `debugImplementation` only — never `implementation`.
- **Material 1** (`androidx.compose.material:material`) is excluded — use Material 3 only.
- **Retrofit serialization converter** is `com.squareup.retrofit2:converter-kotlinx-serialization` — NOT the archived JakeWharton library.
- **`multidex`** is excluded — minSdk 26+ has native multidex.
- **`projectConfigs`** is cached in `extensions.extraProperties` — do not inline the catalog lookup.

---

## 15. Dependency Version Reference (Current)

| Library | Version |
|---------|---------|
| AGP | 8.13.1 |
| Kotlin | 2.3.10 |
| KSP | 2.3.6 |
| Hilt | 2.58 |
| Compose BOM | 2026.02.01 |
| Navigation Compose | 2.9.1 |
| Hilt Navigation Compose | 1.3.0 |
| Lifecycle | 2.10.0 |
| Room | 2.7.2 |
| Retrofit | 2.11.0 |
| OkHttp | 4.12.0 |
| DataStore | 1.1.1 |
| WorkManager | 2.10.0 |
| Coroutines | 1.10.1 |
| kotlinx.serialization | 1.8.0 |
| Chucker | 4.2.0 |
| Lottie | 6.6.0 |
| Firebase BOM | 33.14.0 |
| Tink Android | 1.15.0 |
| Security Crypto | 1.1.0-alpha06 |

---

## 16. `settings.gradle.kts` — Catalog Registration

```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("appspirimentlibs") {
            from(files("gradle/appspirimentlibs.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

---

## 17. Quick Decision Guide

**Which plugin do I use?**

1. Is it the `:app` module? → `appspiriment.application`
2. Does it need Firebase? → also apply `appspiriment.firebase`
3. Is it a library with Room/Retrofit/DataStore/etc.? → also apply `appspiriment.data`
4. Everything else is `appspiriment.library` with flags:
   - Needs Hilt? → `appspiriment { hilt.set(true) }`
   - Needs Compose? → `appspiriment { compose.set(true) }`
   - Needs both? → set both flags

**What goes in `build.gradle.kts`?**

- Plugin declaration(s)
- `android { namespace = "..." }` (and `defaultConfig { applicationId, versionCode, versionName }` for app modules)
- `appspiriment { }` block only if overriding defaults
- `firebase { }` block only if `appspiriment.firebase` is applied
- `dataLayer { }` block only if `appspiriment.data` is applied
- Nothing else — no SDK versions, no compiler options, no dependency declarations for anything the plugin manages
