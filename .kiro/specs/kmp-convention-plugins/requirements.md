# Requirements Document

## Introduction

This feature adds a KMP/CMP (Kotlin Multiplatform / Compose Multiplatform) convention plugin suite to the existing `io.github.appspiriment` Gradle plugin library. The new suite mirrors the design philosophy of the existing Android plugin suite — opinionated defaults, opt-in capabilities, DSL extensions for per-module configuration, and a distributed version catalog — but targets KMP shared modules rather than Android-only modules.

The suite covers all commonly used KMP/CMP concerns: multiplatform project setup, Compose Multiplatform UI, Koin dependency injection, Ktor networking, SQLDelight persistence, KMP DataStore, kotlinx.serialization, kotlinx.coroutines, and KMP-compatible testing. Target platforms are Android, iOS (via Kotlin/Native), Desktop JVM, and Web (Kotlin/WASM).

A new distributed version catalog (`kmplibs.versions.toml`) is introduced alongside the existing `appspirimentlibs.versions.toml`. The new plugins are published under the same `io.github.appspiriment` group ID with a `kmp.` prefix to distinguish them from the Android plugins.

## Glossary

- **KMP**: Kotlin Multiplatform — the Kotlin technology for sharing code across Android, iOS, Desktop, and Web targets.
- **CMP**: Compose Multiplatform — JetBrains' extension of Jetpack Compose that runs on KMP targets.
- **KMP_Plugin**: Any Gradle convention plugin in the `io.github.appspiriment.kmp.*` namespace implemented by this feature.
- **KMP_Catalog**: The `kmplibs.versions.toml` file distributed to and loaded by consumer KMP projects. Analogous to `appspirimentlibs.versions.toml`.
- **KMP_Extension**: The `kmp { }` DSL block exposed on every KMP_Plugin, analogous to the existing `appspiriment { }` block.
- **KmpDataLayerExtension**: The `kmpDataLayer { }` DSL block for opt-in data-layer capabilities, analogous to the existing `dataLayer { }` block.
- **Shared_Module**: A Gradle module that applies `kotlin("multiplatform")` and targets two or more KMP platforms.
- **Target**: A KMP compilation target — one of `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `iosX64`, `jvm` (Desktop), `wasmJs`.
- **Source_Set**: A KMP source set such as `commonMain`, `androidMain`, `iosMain`, `desktopMain`, `wasmJsMain`.
- **Koin**: The KMP-compatible dependency injection framework used in place of Hilt (which is Android-only).
- **Ktor**: The KMP-compatible HTTP client framework used in place of Retrofit.
- **SQLDelight**: The KMP-compatible SQL persistence framework used in place of Android Room.
- **KMP_DataStore**: `androidx.datastore:datastore-preferences-core` — the KMP-compatible DataStore artifact.
- **Compose_Multiplatform**: The `org.jetbrains.compose` Gradle plugin and its associated UI libraries.
- **updateKmpLibFileVersion**: The Gradle task that generates `KmpConstants.kt` by embedding the KMP_Catalog TOML content, analogous to the existing `updateLibFileVersion` task.
- **KmpConstants_kt**: The generated Kotlin file baked into the plugin JAR that embeds the KMP_Catalog TOML content as a string constant.

---

## Requirements

### Requirement 1: KMP Shared Library Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.library` plugin, so that I can set up a Kotlin Multiplatform shared module with sensible defaults without writing boilerplate multiplatform configuration.

#### Acceptance Criteria

1. THE `KmpLibraryConventionPlugin` SHALL apply the `org.jetbrains.kotlin.multiplatform` Gradle plugin to the target project.
2. THE `KmpLibraryConventionPlugin` SHALL apply the `com.android.library` Gradle plugin to the target project, because every KMP shared module in this suite includes an Android target.
3. THE `KmpLibraryConventionPlugin` SHALL configure the `androidTarget` source set with `compileSdk`, `minSdk`, and `javaVersion` values read from the KMP_Catalog `[versions]` section.
4. THE `KmpLibraryConventionPlugin` SHALL configure Kotlin compiler options with `jvmTarget` set to the `javaVersion` value from the KMP_Catalog.
5. THE `KmpLibraryConventionPlugin` SHALL add `kotlinx-coroutines-core` to `commonMain` dependencies.
6. THE `KmpLibraryConventionPlugin` SHALL add KMP-compatible test dependencies (`kotlin-test`, `kotlinx-coroutines-test`) to `commonTest` dependencies.
7. THE `KmpLibraryConventionPlugin` SHALL expose a `kmp { }` DSL block of type `KMP_Extension` on the applying project.
8. WHEN the `kmp { }` block is not configured, THE `KmpLibraryConventionPlugin` SHALL apply only the `androidTarget` by default.
9. THE `KmpLibraryConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.library`.

---

### Requirement 2: KMP Target Configuration via DSL

**User Story:** As a KMP developer, I want to declare which platforms my shared module targets inside a `kmp { }` DSL block, so that I don't need to manually configure each KMP target's source sets and dependencies.

#### Acceptance Criteria

1. THE `KMP_Extension` SHALL expose the following `Property<Boolean>` fields for opt-in target activation: `enableIos` (default `false`), `enableDesktop` (default `false`), `enableWasm` (default `false`). The Android target is always enabled and has no toggle.
2. WHEN `enableIos.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL add the `iosArm64`, `iosSimulatorArm64`, and `iosX64` targets to the multiplatform configuration.
3. WHEN `enableIos.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL create an `iosMain` source set that depends on `commonMain`, and an `iosTest` source set that depends on `commonTest`.
4. WHEN `enableDesktop.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL add the `jvm` target with the name `desktop` to the multiplatform configuration.
5. WHEN `enableDesktop.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL create a `desktopMain` source set that depends on `commonMain`, and a `desktopTest` source set that depends on `commonTest`.
6. WHEN `enableWasm.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL add the `wasmJs` target with browser execution enabled to the multiplatform configuration.
7. WHEN `enableWasm.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL create a `wasmJsMain` source set that depends on `commonMain`, and a `wasmJsTest` source set that depends on `commonTest`.
8. IF a requested target's required toolchain (e.g. Xcode for iOS) is not available on the build host, THEN THE `KmpLibraryConventionPlugin` SHALL propagate the standard Kotlin Multiplatform Gradle error without suppressing it.

---

### Requirement 3: Compose Multiplatform Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.library-compose` plugin, so that I can add Compose Multiplatform UI to a shared module without manually configuring the Compose Multiplatform Gradle plugin and its dependencies.

#### Acceptance Criteria

1. THE `KmpLibraryComposeConventionPlugin` SHALL apply all capabilities of `KmpLibraryConventionPlugin` (Requirement 1) in addition to Compose Multiplatform setup.
2. THE `KmpLibraryComposeConventionPlugin` SHALL apply the `org.jetbrains.compose` Gradle plugin to the target project.
3. THE `KmpLibraryComposeConventionPlugin` SHALL apply the `org.jetbrains.kotlin.plugin.compose` Gradle plugin to the target project.
4. THE `KmpLibraryComposeConventionPlugin` SHALL add the Compose Multiplatform BOM as a `commonMain` platform dependency.
5. THE `KmpLibraryComposeConventionPlugin` SHALL add `compose.runtime`, `compose.foundation`, `compose.material3`, `compose.ui`, and `compose.components.resources` to `commonMain` dependencies.
6. THE `KmpLibraryComposeConventionPlugin` SHALL add `compose.uiTooling` as a `debugImplementation` dependency on the `androidMain` source set.
7. WHEN `enableDesktop.set(true)` is configured on the `kmp { }` block, THE `KmpLibraryComposeConventionPlugin` SHALL add `compose.desktop.currentOs` to `desktopMain` dependencies.
8. THE `KmpLibraryComposeConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.library-compose`.

---

### Requirement 4: Koin Dependency Injection Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.library-koin` plugin, so that I can add Koin DI to a shared module without manually managing Koin's multiplatform artifacts and the KSP plugin.

#### Acceptance Criteria

1. THE `KmpLibraryKoinConventionPlugin` SHALL apply all capabilities of `KmpLibraryConventionPlugin` (Requirement 1) in addition to Koin setup.
2. THE `KmpLibraryKoinConventionPlugin` SHALL apply the `com.google.devtools.ksp` Gradle plugin to the target project.
3. THE `KmpLibraryKoinConventionPlugin` SHALL add `koin-core` to `commonMain` dependencies.
4. THE `KmpLibraryKoinConventionPlugin` SHALL add `koin-annotations` to `commonMain` dependencies and `koin-ksp-compiler` as a KSP dependency for all enabled targets.
5. THE `KmpLibraryKoinConventionPlugin` SHALL add `koin-test` to `commonTest` dependencies.
6. THE `KmpLibraryKoinConventionPlugin` SHALL add `koin-android` to `androidMain` dependencies.
7. WHEN `enableDesktop.set(true)` is configured, THE `KmpLibraryKoinConventionPlugin` SHALL add no additional Koin desktop-specific artifact (Koin core is sufficient for Desktop JVM).
8. THE `KmpLibraryKoinConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.library-koin`.

---

### Requirement 5: Compose + Koin Combined Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.library-koin-compose` plugin, so that I can set up a shared UI module with both Compose Multiplatform and Koin DI in a single plugin application.

#### Acceptance Criteria

1. THE `KmpLibraryKoinComposeConventionPlugin` SHALL apply all capabilities of `KmpLibraryComposeConventionPlugin` (Requirement 3) and all capabilities of `KmpLibraryKoinConventionPlugin` (Requirement 4) without duplication.
2. WHEN both Compose and Koin are active, THE `KmpLibraryKoinComposeConventionPlugin` SHALL add `koin-compose` to `commonMain` dependencies.
3. WHEN both Compose and Koin are active, THE `KmpLibraryKoinComposeConventionPlugin` SHALL add `koin-compose-viewmodel` to `commonMain` dependencies.
4. THE `KmpLibraryKoinComposeConventionPlugin` SHALL NOT add any dependency or apply any plugin more than once, even when both Compose and Koin capabilities are active.
5. THE `KmpLibraryKoinComposeConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.library-koin-compose`.

---

### Requirement 6: KMP Data Layer Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.data` plugin with a `kmpDataLayer { }` DSL block, so that I can opt in to SQLDelight, Ktor, KMP DataStore, and kotlinx.serialization for a shared data module without manually managing each library's Gradle plugin and dependencies.

#### Acceptance Criteria

1. THE `KmpDataLayerConventionPlugin` SHALL apply all capabilities of `KmpLibraryConventionPlugin` (Requirement 1) in addition to data-layer setup.
2. THE `KmpDataLayerConventionPlugin` SHALL expose a `kmpDataLayer { }` DSL block of type `KmpDataLayerExtension` on the applying project.
3. WHEN no `KmpDataLayerExtension` property is set to `true`, THE `KmpDataLayerConventionPlugin` SHALL NOT add any optional data-layer dependency beyond the base KMP library setup.
4. THE `KmpDataLayerConventionPlugin` SHALL use `afterEvaluate` to read `KmpDataLayerExtension` values after the consuming build script has been evaluated.
5. THE `KmpDataLayerConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.data`.

---

### Requirement 7: SQLDelight Opt-In in the KMP Data Layer

**User Story:** As a KMP developer, I want to opt in to SQLDelight persistence inside `kmpDataLayer { }`, so that the SQLDelight Gradle plugin and all platform-specific drivers are added automatically for the targets my module enables.

#### Acceptance Criteria

1. THE `KmpDataLayerExtension` SHALL expose a `sqlDelight` nested block with an `enabled` `Property<Boolean>` (default `false`) and a `schemaVersion` `Property<Int>` (default `1`).
2. WHEN `sqlDelight.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL apply the `app.cash.sqldelight` Gradle plugin.
3. WHEN `sqlDelight.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `sqldelight-runtime` to `commonMain` dependencies.
4. WHEN `sqlDelight.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `sqldelight-android-driver` to `androidMain` dependencies.
5. WHEN `sqlDelight.enabled.set(true)` is configured AND `enableIos.set(true)` is active on the `kmp { }` block, THE `KmpDataLayerConventionPlugin` SHALL add `sqldelight-native-driver` to `iosMain` dependencies.
6. WHEN `sqlDelight.enabled.set(true)` is configured AND `enableDesktop.set(true)` is active on the `kmp { }` block, THE `KmpDataLayerConventionPlugin` SHALL add `sqldelight-sqlite-driver` to `desktopMain` dependencies.
7. WHEN `sqlDelight.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL configure the SQLDelight Gradle extension with `linkSqlite = true` for the Android target.

---

### Requirement 8: Ktor Opt-In in the KMP Data Layer

**User Story:** As a KMP developer, I want to opt in to Ktor networking inside `kmpDataLayer { }`, so that the correct Ktor engine is added per platform and content negotiation with kotlinx.serialization is configured automatically.

#### Acceptance Criteria

1. THE `KmpDataLayerExtension` SHALL expose a `ktor` nested block with an `enabled` `Property<Boolean>` (default `false`) and a `useLogging` `Property<Boolean>` (default `false`).
2. WHEN `ktor.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL apply the `org.jetbrains.kotlin.plugin.serialization` Gradle plugin.
3. WHEN `ktor.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-core` and `ktor-client-content-negotiation` and `ktor-serialization-kotlinx-json` to `commonMain` dependencies.
4. WHEN `ktor.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-okhttp` to `androidMain` dependencies.
5. WHEN `ktor.enabled.set(true)` is configured AND `enableIos.set(true)` is active on the `kmp { }` block, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-darwin` to `iosMain` dependencies.
6. WHEN `ktor.enabled.set(true)` is configured AND `enableDesktop.set(true)` is active on the `kmp { }` block, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-cio` to `desktopMain` dependencies.
7. WHEN `ktor.enabled.set(true)` is configured AND `enableWasm.set(true)` is active on the `kmp { }` block, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-js` to `wasmJsMain` dependencies.
8. WHEN `ktor.useLogging.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `ktor-client-logging` to `commonMain` dependencies.

---

### Requirement 9: KMP DataStore Opt-In in the KMP Data Layer

**User Story:** As a KMP developer, I want to opt in to KMP DataStore inside `kmpDataLayer { }`, so that `datastore-preferences-core` is added to `commonMain` without me needing to know the correct KMP artifact name.

#### Acceptance Criteria

1. THE `KmpDataLayerExtension` SHALL expose a `dataStore` nested block with an `enabled` `Property<Boolean>` (default `false`).
2. WHEN `dataStore.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `datastore-preferences-core` to `commonMain` dependencies.
3. WHEN `dataStore.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL NOT add the Android-only `datastore-preferences` artifact — the KMP core artifact covers all targets.

---

### Requirement 10: kotlinx.serialization Opt-In in the KMP Data Layer

**User Story:** As a KMP developer, I want to opt in to standalone kotlinx.serialization inside `kmpDataLayer { }` (without Ktor), so that I can use JSON serialization in a shared module that does not do networking.

#### Acceptance Criteria

1. THE `KmpDataLayerExtension` SHALL expose a `serialization` nested block with an `enabled` `Property<Boolean>` (default `false`).
2. WHEN `serialization.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL apply the `org.jetbrains.kotlin.plugin.serialization` Gradle plugin (if not already applied by the `ktor` block).
3. WHEN `serialization.enabled.set(true)` is configured, THE `KmpDataLayerConventionPlugin` SHALL add `kotlinx-serialization-json` to `commonMain` dependencies.
4. WHEN both `serialization.enabled.set(true)` and `ktor.enabled.set(true)` are configured, THE `KmpDataLayerConventionPlugin` SHALL apply the serialization plugin and add `kotlinx-serialization-json` exactly once.

---

### Requirement 11: KMP Version Catalog Distribution

**User Story:** As a consumer KMP developer, I want the plugin suite to distribute a `kmplibs.versions.toml` version catalog to my project at sync time, so that I have a single source of truth for all KMP library versions without manually maintaining version strings.

#### Acceptance Criteria

1. THE KMP_Plugin suite SHALL distribute a `kmplibs.versions.toml` file to consumer projects, written into the consumer project's `gradle/` directory at sync time, analogous to how `appspirimentlibs.versions.toml` is distributed by the existing Android plugin suite.
2. THE KMP_Catalog SHALL contain a `[versions]` section with version strings for: Kotlin Multiplatform, Compose Multiplatform, Koin (core + annotations + KSP), Ktor, SQLDelight, KMP DataStore, kotlinx.serialization, kotlinx.coroutines, and all SDK/tooling versions (`compileSdk`, `minSdk`, `targetSdk`, `javaVersion`).
3. THE KMP_Catalog SHALL contain a `[plugins]` section with aliases for all six KMP_Plugin IDs: `kmp-library`, `kmp-library-compose`, `kmp-library-koin`, `kmp-library-koin-compose`, `kmp-data`, and `kmp-application`.
4. THE KMP_Catalog SHALL contain a `[libraries]` section with all KMP library coordinates referenced by the plugins, so that consumer modules can add additional dependencies from the same catalog without version conflicts.
5. THE `updateKmpLibFileVersion` Gradle task SHALL generate `KmpConstants.kt` by reading `gradle/kmplibs.versions.toml` and embedding its content as a string constant, with the `LIBVERSION` placeholder replaced by the current plugin version.
6. WHEN the `kmplibs.versions.toml` file does not exist at task execution time, THE `updateKmpLibFileVersion` task SHALL log a warning and skip generation without failing the build.
7. THE KMP_Catalog `[versions]` section SHALL include a `kmpAppspiriment` version key that matches the published plugin version, so consumers can reference the plugin version in their own catalogs.

---

### Requirement 12: KMP Application Plugin

**User Story:** As a KMP developer, I want an `io.github.appspiriment.kmp.application` plugin for the Android host app module of a KMP project, so that the Android application shell that hosts the KMP shared module is configured consistently with the rest of the suite.

#### Acceptance Criteria

1. THE `KmpApplicationConventionPlugin` SHALL apply the `com.android.application` Gradle plugin to the target project.
2. THE `KmpApplicationConventionPlugin` SHALL apply the `org.jetbrains.kotlin.android` Gradle plugin to the target project.
3. THE `KmpApplicationConventionPlugin` SHALL apply the `org.jetbrains.compose` Gradle plugin and `org.jetbrains.kotlin.plugin.compose` Gradle plugin to the target project.
4. THE `KmpApplicationConventionPlugin` SHALL configure `compileSdk`, `minSdk`, `targetSdk`, and `javaVersion` from the KMP_Catalog `[versions]` section.
5. THE `KmpApplicationConventionPlugin` SHALL add `kotlinx-coroutines-android` to `implementation` dependencies.
6. THE `KmpApplicationConventionPlugin` SHALL add the Compose Multiplatform BOM as a `platform()` `implementation` dependency and add `compose.runtime`, `compose.ui`, `compose.material3`, `compose.foundation`, and `compose.uiTooling` (as `debugImplementation`) to `implementation` dependencies.
7. THE `KmpApplicationConventionPlugin` SHALL expose a `kmp { }` DSL block of type `KMP_Extension` on the applying project.
8. WHEN `kmp.enableMinify.set(true)` is configured, THE `KmpApplicationConventionPlugin` SHALL enable R8 minification on the release build type.
9. THE `KmpApplicationConventionPlugin` SHALL be published under the plugin ID `io.github.appspiriment.kmp.application`.

---

### Requirement 13: KMP Extension DSL

**User Story:** As a KMP developer, I want a consistent `kmp { }` DSL block across all KMP plugins, so that I can configure per-module options (target platforms, minification, utility libraries) in a single, discoverable place.

#### Acceptance Criteria

1. THE `KMP_Extension` SHALL be an abstract Gradle-managed class registered under the extension name `kmp` on every project that applies a KMP_Plugin.
2. THE `KMP_Extension` SHALL expose `enableIos: Property<Boolean>` (default `false`), `enableDesktop: Property<Boolean>` (default `false`), and `enableWasm: Property<Boolean>` (default `false`).
3. THE `KMP_Extension` SHALL expose `enableMinify: Property<Boolean>` (default `false`) for use by `KmpApplicationConventionPlugin`.
4. THE `KMP_Extension` SHALL expose `enableUtils: Property<Boolean>` (default `true`) to control whether Appspiriment utility libraries (`appspiriment-utils`, `logutils`) are added to the module.
5. WHEN `enableUtils.set(true)` is configured (the default), THE KMP_Plugin SHALL add `appspiriment-utils` to `commonMain` dependencies and `appspiriment-logutils-dev` as a `debugImplementation` dependency on `androidMain` and `appspiriment-logutils-prod` as a `releaseImplementation` dependency on `androidMain`.
6. WHEN `enableUtils.set(false)` is configured, THE KMP_Plugin SHALL NOT add any Appspiriment utility library.
7. THE `KMP_Extension` SHALL use Gradle's `Property` API for all fields to ensure lazy evaluation and configuration-cache compatibility.

---

### Requirement 14: KMP-Compatible Testing Setup

**User Story:** As a KMP developer, I want KMP-compatible test dependencies configured automatically in every shared module, so that I can write unit tests in `commonTest` without manually adding test libraries.

#### Acceptance Criteria

1. THE `KmpLibraryConventionPlugin` SHALL add `kotlin-test` to `commonTest` dependencies for every module that applies any KMP_Plugin.
2. THE `KmpLibraryConventionPlugin` SHALL add `kotlinx-coroutines-test` to `commonTest` dependencies for every module that applies any KMP_Plugin.
3. THE `KmpLibraryConventionPlugin` SHALL add `turbine` (app.cash.turbine) to `commonTest` dependencies for Flow testing.
4. WHEN `enableIos.set(true)` is configured, THE `KmpLibraryConventionPlugin` SHALL add no additional iOS-specific test dependency beyond `kotlin-test` (the Kotlin/Native test runner handles iOS test execution).
5. WHEN the `koin` capability is active (via `KmpLibraryKoinConventionPlugin` or `KmpLibraryKoinComposeConventionPlugin`), THE plugin SHALL add `koin-test` to `commonTest` dependencies.

---

### Requirement 15: Consistent Plugin Publishing

**User Story:** As a plugin maintainer, I want all six KMP convention plugins registered in `conventions/build.gradle.kts` and published to Maven Central under `io.github.appspiriment`, so that consumers can apply them with a single version reference.

#### Acceptance Criteria

1. THE `conventions/build.gradle.kts` SHALL register the following six plugin IDs, each mapped to its implementation class: `io.github.appspiriment.kmp.library`, `io.github.appspiriment.kmp.library-compose`, `io.github.appspiriment.kmp.library-koin`, `io.github.appspiriment.kmp.library-koin-compose`, `io.github.appspiriment.kmp.data`, and `io.github.appspiriment.kmp.application`.
2. THE six KMP_Plugin IDs SHALL be published under the same Maven coordinates (`io.github.appspiriment:conventions`) and version as the existing Android plugins, so consumers need only one dependency declaration.
3. THE KMP_Catalog `[plugins]` section SHALL include all six KMP_Plugin aliases with `version.ref = "kmpAppspiriment"`.
4. THE `pluginversion.properties` file SHALL continue to serve as the single source of truth for the published version, shared by both the Android and KMP plugin suites.
5. WHEN a new KMP_Plugin version is published, THE `updateKmpLibFileVersion` task SHALL update the `LIBVERSION` placeholder in `KmpConstants.kt` to the new version before the JAR is assembled.
