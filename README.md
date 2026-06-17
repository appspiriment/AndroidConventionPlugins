# Appspiriment Android Convention Plugins

[![Maven Central](https://img.shields.io/maven-central/v/io.github.appspiriment/conventions.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.appspiriment/conventions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A suite of opinionated Gradle convention plugins for Android projects. Apply one plugin per module and get a fully configured, production-ready build — SDK versions, Kotlin compiler options, Compose, Hilt, Firebase, and more — without writing any boilerplate.

---

## Philosophy

Modern Android projects share the same build configuration across dozens of modules: the same SDK targets, the same Kotlin compiler flags, the same Compose BOM, the same Hilt setup. Writing this out in every `build.gradle.kts` is repetitive, error-prone, and a maintenance burden when versions need updating.

This library solves that with two ideas:

**Convention plugins** — each plugin encodes the "right" way to configure a particular type of module. An app module, a feature module, a data module — each has a plugin that handles everything. Your `build.gradle.kts` files shrink to a plugin declaration, a namespace, and any opt-in flags.

**A slim version catalog** — `appspirimentlibs.versions.toml` is the single source of truth for every dependency version across all your projects. It contains only version strings and the four Appspiriment plugin aliases. The plugins own all dependency `group:artifact` coordinates internally; the catalog owns only the version strings. This means:

- Upgrading Compose BOM or Hilt means changing one line in one file, then syncing.
- You can override any version independently without waiting for a plugin release.
- Your catalog is not polluted with library aliases and bundles you didn't ask for.

The result: new modules take seconds to set up, upgrades are a one-liner, and every module in every project stays consistent.

---

## Requirements

- Android Gradle Plugin `8.x`
- Kotlin `2.x`
- Java `21` toolchain
- `minSdk` `26+` (the plugins target API 26 minimum; native multidex is assumed)
- Gradle `8.x`

---

## Installation

### New Projects (Recommended)

Use the project template in `project-template/` to bootstrap a new project. It comes pre-wired with the `appspirimentlibs` version catalog, all plugin aliases, and the `upgradeAppspiriment` task.

**Copy the template manually:**

```bash
cp -r project-template/root/. /path/to/your/new/project/
# Replace the placeholder package name
find /path/to/your/new/project -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" \) \
  -exec sed -i 's/com\.example\.app/com.yourcompany.yourapp/g' {} +
```

Then open in Android Studio and sync.

---

### Existing Projects

**Step 1** — Copy `gradle/appspirimentlibs.versions.toml` into your project's `gradle/` folder.

**Step 2** — Register the catalog in `settings.gradle.kts`:

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

**Step 3** — Declare all plugins in your root `build.gradle.kts` with `apply false`.

Standard tooling plugins use string IDs with versions read from the catalog. Only the four Appspiriment plugins use `alias()` — because those are the only plugin aliases in the slim catalog.

```kotlin
// build.gradle.kts (root)
plugins {
    id("com.android.application")                   version appspirimentlibs.versions.agp.get()                    apply false
    id("com.android.library")                        version appspirimentlibs.versions.agp.get()                    apply false
    id("org.jetbrains.kotlin.android")               version appspirimentlibs.versions.kotlin.get()                 apply false
    id("org.jetbrains.kotlin.plugin.compose")        version appspirimentlibs.versions.kotlin.get()                 apply false
    id("org.jetbrains.kotlin.jvm")                   version appspirimentlibs.versions.kotlin.get()                 apply false
    id("com.google.devtools.ksp")                    version appspirimentlibs.versions.ksp.get()                    apply false
    id("com.google.dagger.hilt.android")             version appspirimentlibs.versions.hilt.get()                   apply false
    id("org.jetbrains.kotlin.plugin.serialization")  version appspirimentlibs.versions.kotlinserializeplugin.get()  apply false
    alias(appspirimentlibs.plugins.appspiriment.application) apply false
    alias(appspirimentlibs.plugins.appspiriment.library)     apply false
    alias(appspirimentlibs.plugins.appspiriment.data)        apply false
    alias(appspirimentlibs.plugins.appspiriment.firebase)    apply false
}
```

**Step 4** — Apply the appropriate plugin in each module (see [Plugin Reference](#plugin-reference) below).

**Step 5** — Sync Gradle.

---

## Upgrading

```bash
./gradlew upgradeAppspiriment -PnewVersion=<new-version>
```

Then sync Gradle. That's it — no other files need to change.

The `upgradeAppspiriment` task is included in the project template's root `build.gradle.kts`. It updates the `appspiriment` version key in `appspirimentlibs.versions.toml`.

Check available versions at: [Releases](https://github.com/appspiriment/AndroidConventionPlugins/releases)

---

## Plugin Reference

There are four plugins. Each module gets one primary plugin, with optional secondary plugins layered on top.

### `io.github.appspiriment.application`

For the `:app` module. Configures a full Android application with Hilt and Compose always enabled — no flags needed.

**What it sets up automatically:**
- `com.android.application` plugin
- SDK versions from the catalog (`compileSdk 36`, `minSdk 26`, `targetSdk 36`)
- Java 21 compile options and Kotlin JVM_21 target
- Kotlin 2.x compiler with recommended opt-ins
- Jetpack Compose (BOM-managed, full stack)
- Hilt dependency injection
- Base deps: core-ktx, lifecycle-runtime-ktx, coroutines
- Test deps: JUnit, Mockito-Kotlin, coroutines-test, Turbine
- `appspiriment-utils`, `appspiriment-logutils` debug/release variants (when `enableUtils = true`)
- `appspiriment-compose`, `lottie-compose` (when `enableUtils = true`)
- `ui-tooling` as `debugImplementation`

Firebase is **not** included — apply `appspiriment.firebase` alongside this plugin if you need it.

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

You do **not** need to add `compileSdk`, `minSdk`, `targetSdk`, `compileOptions`, `kotlinOptions`, `buildFeatures { compose = true }`, or any of the dependencies listed above — the plugin handles all of it.

---

### `io.github.appspiriment.library`

For all Android library module types. This single plugin replaces the old `library-hilt`, `library-compose`, and `library-hilt-compose` plugins. Hilt and Compose are opt-in via flags in the `appspiriment { }` block.

**Default (no flags) — plain Android library:**

```kotlin
// mylibrary/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
}
android { namespace = "com.example.mylibrary" }
```

**With Hilt — domain / use-case modules:**

```kotlin
// domain/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
}
android { namespace = "com.example.domain" }
appspiriment { hilt.set(true) }
```

**With Compose — shared UI component libraries:**

```kotlin
// ui-components/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
}
android { namespace = "com.example.ui.components" }
appspiriment { compose.set(true) }
```

**With Hilt + Compose — feature screen modules:**

```kotlin
// feature/home/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
}
android { namespace = "com.example.feature.home" }
appspiriment {
    hilt.set(true)
    compose.set(true)
}
```

When both flags are set, all Hilt and Compose dependencies are added without duplication. `hilt-navigation-compose` and the `kotlinx-serialization` Gradle plugin are each added exactly once.

---

### `io.github.appspiriment.data`

For data layer modules. Provides opt-in Room, Retrofit, DataStore, Security, and WorkManager. **Nothing is added unless explicitly enabled** — all features default to off.

Typically combined with `appspiriment.library` + `hilt.set(true)` since data modules usually need Hilt for repository injection.

```kotlin
// data/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library)
    alias(appspirimentlibs.plugins.appspiriment.data)
}
android { namespace = "com.example.data" }
appspiriment { hilt.set(true) }

dataLayer {
    room {
        enabled.set(true)
        usePaging.set(true)   // adds room-paging for Paging 3
    }
    retrofit {
        enabled.set(true)
        useChucker.set(true)              // Chucker HTTP inspector (debugImpl, no-op in release)
        useKotlinSerialization.set(true)  // kotlinx.serialization converter
    }
    security {
        enabled.set(true)   // androidx.security-crypto + Google Tink
    }
    dataStore {
        enabled.set(true)   // Jetpack DataStore preferences
    }
    workManager {
        enabled.set(true)   // WorkManager + Hilt-Work integration
    }
}
```

Chucker is handled entirely by the plugin (debug variant / release no-op). You do not need to manage it manually.

---

### `io.github.appspiriment.firebase`

A standalone plugin for Firebase. Apply it alongside any module plugin — it works with both application and library modules.

The plugin detects whether it is applied to an application module or a library module and behaves accordingly:

- On an **application module** with `crashlytics.set(true)`: applies the `google-services` and `firebase-crashlytics` Gradle plugins, and validates that `google-services.json` exists in the module directory (throws a clear error at configuration time if it is missing).
- On a **library module** with `crashlytics.set(true)`: adds only the crashlytics dependency — does not apply Gradle plugins.

Firebase BOM is added as `platform()` automatically when any service is enabled.

**App module with Firebase:**

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
    crashlytics.set(true)   // auto-applies google-services + crashlytics plugins; validates google-services.json
    auth.set(true)
    messaging.set(true)
}
```

**Data module with Firebase:**

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

## The `appspiriment { }` Extension

All convention plugins expose an `appspiriment { }` block for optional configuration. All properties have sensible defaults — you only need this block when you want to override something or enable Hilt/Compose on a library module.

| Property | Default | Applies to | Description |
|----------|---------|------------|-------------|
| `enableUtils` | `true` | all plugins | Adds `appspiriment-utils`, `appspiriment-logutils-dev` (debugImpl), `appspiriment-logutils-prod` (releaseImpl). For Compose modules, also adds `appspiriment-compose` and `lottie-compose`. Set to `false` for modules with strict dependency budgets. |
| `enableMinify` | `false` | all plugins | Enables R8 minification in the release build type. Recommended `true` for app modules in production. Keep `false` for library modules — consumers control minification. |
| `addDevSuffixToDebug` | `true` | application only | In debug builds, appends `.dev` to `applicationId` (so debug and release can be installed side-by-side) and a `.yyyyMMdd.HHmmss` timestamp to `versionName`. See [Debug Build Behavior](#debug-build-behavior). |
| `hilt` | `false` | library only | Enables Hilt DI — applies Hilt + KSP plugins, adds `hilt-android` (impl), `hilt-android-compiler` (ksp), `kotlinx-serialization-json` (impl). |
| `compose` | `false` | library only | Enables Jetpack Compose — applies `kotlin-compose` + `kotlinx-serialization` plugins, adds the full Compose BOM stack, `hilt-navigation-compose`, `ui-tooling` (debugImpl). |

---

## The `firebase { }` Extension

The `firebase { }` block is exposed by the `appspiriment.firebase` plugin. All properties default to `false` — nothing is added unless you opt in.

| Property | Default | Module scope | What it adds |
|----------|---------|-------------|-------------|
| `analytics` | `false` | App | `firebase-analytics-ktx` (BOM-managed) |
| `crashlytics` | `false` | App | `firebase-crashlytics` (BOM-managed) + `google-services` plugin + `firebase-crashlytics` plugin + `google-services.json` validation (app modules only) |
| `auth` | `false` | App | `firebase-auth` (BOM-managed) |
| `messaging` | `false` | App | `firebase-messaging-ktx` (BOM-managed) |
| `database` | `false` | Any | `firebase-database-ktx` (BOM-managed) |
| `storage` | `false` | Any | `firebase-storage-ktx` (BOM-managed) |
| `remoteConfig` | `false` | Any | `firebase-config-ktx` (BOM-managed) |

The Firebase BOM version comes from `firebaseBom` in `[versions]`. You can override it like any other version.

### Clean Architecture Firebase Split

A common clean architecture pattern is to split Firebase responsibilities across modules:

- **App module** — analytics, crashlytics, auth, push messaging. These are app-level concerns that belong at the entry point.
- **Data module** — Realtime Database, Cloud Storage, Remote Config. These are data-layer concerns accessed through repositories.

```
app/build.gradle.kts
  → appspiriment.application + appspiriment.firebase
  → firebase { analytics=true, crashlytics=true, auth=true, messaging=true }

data/build.gradle.kts
  → appspiriment.library + appspiriment.data + appspiriment.firebase
  → firebase { database=true, storage=true, remoteConfig=true }
```

This keeps each module's Firebase surface area minimal and testable.

---

## The `dataLayer { }` Extension

The `dataLayer { }` block is exposed by the `appspiriment.data` plugin. All options default to off.

| Block | Property | Default | What it adds |
|-------|----------|---------|-------------|
| `room { }` | `enabled` | `false` | `room-runtime`, `room-ktx` (impl); `room-compiler` (ksp) |
| `room { }` | `usePaging` | `false` | `room-paging` (impl) — for Paging 3 integration |
| `retrofit { }` | `enabled` | `false` | `okhttp`, `retrofit`, `okhttp-logging-interceptor` (impl) |
| `retrofit { }` | `useChucker` | `false` | Chucker HTTP inspector (debugImpl) + no-op (releaseImpl) |
| `retrofit { }` | `useKotlinSerialization` | `false` | `converter-kotlinx-serialization` from `com.squareup.retrofit2` (impl) |
| `security { }` | `enabled` | `false` | `androidx.security:security-crypto`, `com.google.crypto.tink:tink-android` (impl) |
| `dataStore { }` | `enabled` | `false` | `datastore-preferences` (impl) |
| `workManager { }` | `enabled` | `false` | `work-runtime-ktx`, `hilt-work`, `hilt-compiler` (impl/ksp) |

---

## What Each Plugin Adds — Full Dependency Tables

### Base (all plugins)

| Configuration | Dependency |
|--------------|-----------|
| `implementation` | `androidx.core:core-ktx` |
| `implementation` | `androidx.lifecycle:lifecycle-runtime-ktx` |
| `implementation` | `org.jetbrains.kotlinx:kotlinx-coroutines-android` |
| `testImplementation` | `junit:junit` |
| `testImplementation` | `org.mockito.kotlin:mockito-kotlin` |
| `testImplementation` | `org.jetbrains.kotlinx:kotlinx-coroutines-test` |
| `testImplementation` | `app.cash.turbine:turbine` |
| `androidTestImplementation` | `androidx.test.ext:junit` |
| `androidTestImplementation` | `androidx.test.espresso:espresso-core` |

### Hilt additions (`hilt.set(true)` or application plugin)

| Configuration | Dependency |
|--------------|-----------|
| `implementation` | `com.google.dagger:hilt-android` |
| `implementation` | `org.jetbrains.kotlinx:kotlinx-serialization-json` |
| `ksp` | `com.google.dagger:hilt-android-compiler` |

### Compose additions (`compose.set(true)` or application plugin)

| Configuration | Dependency |
|--------------|-----------|
| `implementation` (platform) | `androidx.compose:compose-bom` |
| `implementation` | `androidx.activity:activity-compose` |
| `implementation` | `androidx.compose.foundation:foundation` |
| `implementation` | `androidx.compose.ui:ui` |
| `implementation` | `androidx.compose.ui:ui-tooling-preview` |
| `implementation` | `androidx.compose.material3:material3` |
| `implementation` | `androidx.compose.material:material-icons-core-android` |
| `implementation` | `androidx.compose.material:material-icons-extended-android` |
| `implementation` | `androidx.navigation:navigation-compose` |
| `implementation` | `androidx.lifecycle:lifecycle-viewmodel-compose` |
| `implementation` | `androidx.lifecycle:lifecycle-runtime-compose` |
| `implementation` | `org.jetbrains.kotlinx:kotlinx-serialization-json` |
| `implementation` | `androidx.hilt:hilt-navigation-compose` |
| `debugImplementation` | `androidx.compose.ui:ui-tooling` |
| `debugImplementation` | `androidx.compose.ui:ui-test-manifest` |
| `androidTestImplementation` | `androidx.compose.ui:ui-test-junit4` |

### Utils additions (when `enableUtils = true`, the default)

| Configuration | Dependency |
|--------------|-----------|
| `implementation` | `io.github.appspiriment:utils` |
| `debugImplementation` | `io.github.appspiriment:logutils-dev` |
| `releaseImplementation` | `io.github.appspiriment:logutils-prod` |
| `implementation` (Compose modules only) | `io.github.appspiriment:compose-utils` |
| `implementation` (Compose modules only) | `com.airbnb.android:lottie-compose` |

---

## Typical Multi-Module Project Layout

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

| Module type | Plugin(s) | `appspiriment { }` flags |
|-------------|-----------|--------------------------|
| App entry point | `appspiriment.application` | — (Hilt + Compose always on) |
| App with Firebase | `appspiriment.application` + `appspiriment.firebase` | `firebase { analytics, crashlytics, auth, messaging }` |
| Feature screen (ViewModel + UI) | `appspiriment.library` | `hilt=true`, `compose=true` |
| Feature screen (UI only) | `appspiriment.library` | `compose=true` |
| Domain / use-case layer | `appspiriment.library` | `hilt=true` |
| Data layer | `appspiriment.library` + `appspiriment.data` | `hilt=true` |
| Data layer with Firebase | `appspiriment.library` + `appspiriment.data` + `appspiriment.firebase` | `hilt=true`; `firebase { database, storage, remoteConfig }` |
| Shared UI components | `appspiriment.library` | `compose=true` |
| Pure Kotlin utility | `appspiriment.library` | — |

---

## Overriding Library Versions

You can override any version in `[versions]` without waiting for a plugin release. The plugin reads all version strings from the consumer catalog at configuration time, so your override takes effect on the next Gradle sync.

```toml
# gradle/appspirimentlibs.versions.toml
[versions]
# ... other versions ...

# Override examples — change any of these independently:
hilt = "2.59"              # plugin will use this for hilt-android and hilt-android-compiler
composeBom = "2026.05.00"  # plugin will use this BOM for all Compose artifacts
room = "2.8.0"             # plugin will use this for all room-* artifacts
firebaseBom = "33.15.0"    # firebase plugin will use this BOM for all Firebase artifacts
kotlin = "2.4.0"           # root build.gradle.kts reads this for the kotlin plugin version
```

This is the intended upgrade path for individual libraries between plugin releases. The plugin validates that required version keys are present and throws a clear error if any are missing.

---

## Debug Build Behavior

When `addDevSuffixToDebug = true` (the default for application modules), debug builds automatically get:

- `applicationId` → `com.example.app.dev` — allows side-by-side install with the release build on the same device
- `versionName` → `1.0.0.20260504.143022` — date + time of build in `yyyyMMdd.HHmmss` format, so you always know when a debug APK was built

Disable with `addDevSuffixToDebug.set(false)` if your CI or testing setup requires a stable `applicationId` in debug builds.

This property is application-only — it has no effect on library modules.

---

## The `:resources` Module

The companion `io.github.appspiriment:resources` artifact ships default Material Design 3 color tokens and dimension resources as an AAR. It is optional — no plugin adds it automatically. Add it explicitly if you want the defaults:

```kotlin
dependencies {
    implementation("io.github.appspiriment:resources:1.0.0")
}
```

Override any value in your module's `src/main/res/` — Android's resource merging will prefer your values over the defaults.

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a full history of changes.

**Current stable: `0.0.14`**

---

## Contributing

Contributions are welcome. Please open an issue to discuss a bug fix or feature request before submitting a pull request. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## Credits

Built and maintained by [Appspiriment Labs](https://github.com/appspiriment/).

Built on top of:
- [Android Gradle Plugin](https://developer.android.com/build) by Google
- [Kotlin](https://kotlinlang.org/) by JetBrains
- [Hilt](https://dagger.dev/hilt/) by Google
- [Jetpack Compose](https://developer.android.com/compose) by Google
- [Room](https://developer.android.com/training/data-storage/room) by Google
- [Retrofit](https://square.github.io/retrofit/) by Square
- [Firebase](https://firebase.google.com/) by Google
- [Vanniktech Maven Publish Plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) by Vanniktech

---

## License

```
Copyright 2025 Appspiriment Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
