# Project Overview: Appspiriment Android Convention Plugins

## What This Project Is

This is a **Gradle Convention Plugin library** published to Maven Central under `io.github.appspiriment`. It provides opinionated, reusable Gradle build configurations for Android projects. It is **not** an Android app — it is a build tooling library consumed by other Android projects.

## Modules

| Module | Purpose |
|--------|---------|
| `:conventions` | The core Gradle plugin module. Contains all plugin implementations, extensions, and the version catalog generator. Published as `io.github.appspiriment:conventions`. |
| `:resources` | A companion Android library module that ships default Material Design 3 color tokens and dimension resources. Published as `io.github.appspiriment:resources`. |

## Published Plugins

| Plugin ID | Class | Purpose |
|-----------|-------|---------|
| `io.github.appspiriment.application` | `AndroidApplicationConventionPlugin` | Full Android app setup (Hilt + Compose by default) |
| `io.github.appspiriment.library` | `AndroidLibraryConventionPlugin` | Minimal Android library setup |
| `io.github.appspiriment.library-hilt` | `AndroidLibraryHiltConventionPlugin` | Library + Hilt DI |
| `io.github.appspiriment.library-compose` | `AndroidLibraryComposeConventionPlugin` | Library + Jetpack Compose |
| `io.github.appspiriment.library-hilt-compose` | `AndroidLibraryHiltComposeConventionPlugin` | Library + Hilt + Compose |
| `io.github.appspiriment.data` | `AndroidDataLayerConventionPlugin` | Data layer setup (Room, Retrofit, DataStore, Security, WorkManager) |

> Note: The `io.github.appspiriment.project` plugin has been removed. Project bootstrapping is now handled by the Android Studio project template in `project-template/`. Upgrades use the `./gradlew upgradeAppspiriment -PnewVersion=X` task baked into the template's root `build.gradle.kts`.

## Version Catalogs

| Catalog | File | Purpose |
|---------|------|---------|
| `libs` | `gradle/libs.versions.toml` | Serves the `:conventions` build itself |
| `appspirimentlibs` | `gradle/appspirimentlibs.versions.toml` | The catalog **distributed to consumers** via the project plugin |
| `kmp` | `gradle/kmp.versions.toml` | KMP dependencies (future use) |

## Key Architecture Concepts

- **Bootstrapping**: `AndroidProjectConventionPlugin` writes `appspirimentlibs.versions.toml` into the consuming project's `gradle/` folder at sync time, then registers it in `settings.gradle.kts`.
- **`afterEvaluate` pattern**: `AndroidBaseConventionPlugin` uses `afterEvaluate` to read user-configured `AppspirimentExtension` values before finalizing Android configuration.
- **Baked-in TOML**: The `updateLibFileVersion` task in `conventions/build.gradle.kts` generates a `Constants.kt` file at build time that embeds the TOML content as a string constant, so the plugin can write it to consumer projects at runtime.
- **Version bumping at configuration phase**: The `getAndMaybeBumpVersion()` function runs during Gradle's configuration phase (not execution phase) to bump the dev version counter when `publishDev` is in the task graph.

## Technology Stack

- **Language**: Kotlin (JVM 21, Kotlin 2.x)
- **Build System**: Gradle with Kotlin DSL
- **DI**: Hilt (Dagger 2)
- **UI**: Jetpack Compose with Material 3
- **Persistence**: Room
- **Networking**: Retrofit + OkHttp
- **Publishing**: Vanniktech Maven Publish plugin → Maven Central (Sonatype)
- **Signing**: GPG via `useGpgCmd()`
