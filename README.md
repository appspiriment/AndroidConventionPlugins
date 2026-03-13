# Appspiriment Gradle Convention Plugins

Copyright (c) 2024 Appspiriment Labs

*This project is licensed under the terms of the GNU General Public License. Please see the LICENSE file for the full license text.*

---

## Overview

Appspiriment Gradle Convention Plugins provide a powerful, opinionated framework for standardizing Gradle build configurations across multiple Android projects. They are specifically designed for individual developers and small teams who frequently bootstrap new projects and need to maintain dependency consistency with minimal overhead.

By leveraging Gradle's convention plugin system, this framework solves two critical problems:

1.  **Rapid Project Scaffolding:** Instead of manually configuring every new project, developers can apply a single plugin to instantly set up a tested, modern Android build environment. This drastically reduces initial setup time from hours to minutes.

2.  **Centralized Dependency Management:** The plugins distribute a version catalog (`.toml` file) that provides a single source of truth for all dependency versions. Updating a library across all company projects becomes as simple as updating the plugin and re-syncing.

### The Bootstrapping Advantage

The core of this system is a unique bootstrapping mechanism. The root project plugin automatically copies a curated `appspirimentlibs.versions.toml` file into the consuming project. This approach provides an out-of-the-box, shareable dependency ecosystem without requiring complex, enterprise-grade solutions like custom Maven repositories.

**Justification for our Audience:** While unconventional for large enterprises, this bootstrapping model is a pragmatic and efficient solution for smaller teams. The immediate value of a consistent, pre-configured setup and the ease of cross-project updates outweigh the need for the highly-flexible, but complex, governance models required by larger organizations.

---

## Getting Started: A Two-Phase Approach

### Phase 1: Bootstrap the Project

In your **root `build.gradle.kts` file**, apply the `io.github.appspiriment.project` plugin. This action only needs to be performed once per project.

**Root `build.gradle.kts`**
```kotlin
plugins {
    id("io.github.appspiriment.project") apply false
}
```

This bootstraps the entire system by copying and registering the `appspirimentlibs` version catalog.

> **Sync Twice:** After applying this plugin for the first time, you must **sync Gradle twice**. The first sync copies the file; the second sync allows the IDE to read the newly registered version catalog.

### Phase 2: Apply Conventions to Modules

With the project bootstrapped, apply the appropriate convention plugin to each module's `build.gradle.kts` file.

---

## Android Convention Plugin Reference

These plugins configure your Android application and library modules.

### `io.github.appspiriment.application`

This is the definitive plugin for an Android **application** module.

-   **Primary Use-Case:** The main entry-point of your app (typically the `:app` module).
-   **Core Functionality:** Applies the `com.android.application` plugin and configures default `compileSdk`, `targetSdk`, `minSdk`, and Java versions from the version catalog.
-   **Batteries-Included:** By default, this plugin fully enables **Hilt** and **Jetpack Compose**. It applies their respective Gradle plugins and adds the necessary dependencies, providing a modern, out-of-the-box architecture for dependency injection and UI.

**Usage:**
```kotlin
// app/build.gradle.kts
plugins {
    id("io.github.appspiriment.application")
}
```

**Included Dependencies:**
This plugin includes all dependencies from the `.library.hilt.compose` plugin.

### Library Plugins

These plugins provide tiered configurations for your Android library modules.

#### `io.github.appspiriment.library`

-   **Primary Use-Case:** A foundational, feature-agnostic Android library.
-   **Core Functionality:** Applies the `com.android.library` plugin and sets the base SDK and Java versions.
-   **Minimalist by Design:** Does not apply any optional features like Hilt or Compose, keeping the library lightweight.

**Included Dependencies:**

| Type | Configuration | Dependencies |
| --- | --- | --- |
| Bundle | `implementation` | `android-base` (core-ktx, lifecycle-runtime-ktx, kotlinx-coroutines-android, multidex) |
| Bundle | `testImplementation` | `unit-test` (junit) |
| Bundle | `androidTestImplementation`| `android-test` (androidx.junit, espresso-core) |


#### `io.github.appspiriment.library.compose`

-   **Primary Use-Case:** A library module dedicated to providing Jetpack Compose UI components.
-   **Core Functionality:** Includes all features of the base `library` plugin, applies the `org.jetbrains.kotlin.plugin.compose` plugin, and adds the required `androidx.compose` dependencies.

**Included Dependencies (in addition to `library`):**

| Type | Configuration | Dependencies |
| --- | --- | --- |
| Platform | `implementation` | `androidx-compose-bom` |
| Bundle | `implementation` | `android-compose` |
| Dependency | `debugImplementation`| `androidx-ui-tooling`, `androidx-ui-test-manifest` |
| Platform | `androidTestImplementation`| `androidx-compose-bom` |
| Dependency | `androidTestImplementation`| `androidx-ui-test-junit4` |
| Dependency | `implementation` | `lottie-compose`, `hilt-compose-navigation`, `appspiriment-compose` |


#### `io.github.appspiriment.library.hilt`

-   **Primary Use-Case:** A library module that provides non-UI, dependency-injected components (e.g., data repositories, use cases, or network services).
-   **Core Functionality:** Includes all features of the base `library` plugin, applies the `dagger.hilt.android.plugin`, and adds the necessary Hilt dependencies for injection.

**Included Dependencies (in addition to `library`):**

| Type | Configuration | Dependencies |
| --- | --- | --- |
| Dependency | `implementation` | `hilt-android`, `hilt-compose-navigation` |
| Dependency | `ksp` | `hilt-compiler` |


#### `io.github.appspiriment.library.hilt.compose`

-   **Primary Use-Case:** A self-contained feature module that includes its own UI (Compose) and business logic (Hilt ViewModels, etc.).
-   **Core Functionality:** The most comprehensive library plugin. It merges the functionality of both `.library.compose` and `.library.hilt`, providing a complete environment for a modern feature module.

**Included Dependencies:**
This plugin includes the combined dependencies of the `library`, `library.compose`, and `library.hilt` plugins.

---

### Configuration via the `appspiriment` Extension

Once a convention plugin is applied, the `appspiriment` extension becomes available to enable optional, feature-specific dependencies and configurations.

**Example Usage:**
```kotlin
// app/build.gradle.kts
plugins {
    id("io.github.appspiriment.application")
}

// Enable optional features for this module
appspiriment {
    enableRoom.set(true)
    enableRetrofit.set(true)
}
```

**Available Options:**

| Property | Description | Included Dependencies |
| --- | --- | --- |
| `enableRoom` | Enables **Room** for local persistence. | `room-runtime`, `room-ktx` (`implementation`), `room-compiler` (`ksp`) |
| `enableRetrofit` | Enables **Retrofit** for networking. | The `retrofit` bundle (`implementation`), which includes Retrofit, OkHttp, the logging interceptor, and Gson converter. |
| `enableChucker` | Enables the **Chucker** HTTP inspector. | `chucker` (`debugImplementation`), `chucker-no-op` (`releaseImplementation`) |
| `enableUtils` | Includes Appspiriment utility libraries. | `appspiriment-utils` (`implementation`), `logutils-dev` (`debugImplementation`), `logutils-prod` (`releaseImplementation`) |
| `enableMinify` | Sets `isMinifyEnabled = true` in the `release` build type. | *(No dependencies)* |

---

## Contributing

Contributions are welcome. Please open an issue to discuss bug fixes or feature requests, or submit a pull request for review.
