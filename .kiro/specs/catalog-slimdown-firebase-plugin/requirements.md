# Requirements Document

## Introduction

This feature slims down the distributed `appspirimentlibs.versions.toml` catalog so consumer projects only see what they need (versions + Appspiriment plugin aliases), moves all internal dependency coordinates into plugin Kotlin code, adds a `firebase { }` extension to the application plugin, adds a `firebase { }` block inside `dataLayer { }` on the data plugin, and updates the `updateLibFileVersion` task and project template to match the new structure.

The core principle is: **the plugin owns all dependency coordinates; the consumer catalog owns only version strings**. This eliminates `findLibrary()` and `findBundle()` calls against the consumer catalog for internally-managed dependencies, while preserving the consumer's ability to override any version.

## Glossary

- **Consumer Catalog**: The `appspirimentlibs.versions.toml` file distributed to and loaded by consumer Android projects.
- **Plugin Catalog**: The `gradle/libs.versions.toml` file used internally by the `:conventions` build itself.
- **Convention_Plugin**: Any Gradle plugin in the `io.github.appspiriment.*` namespace implemented in this project.
- **Coordinate**: A Maven dependency identifier in `group:artifact:version` form.
- **Slim_TOML**: The new reduced form of the Consumer Catalog containing only `[versions]` and the four Appspiriment `[plugins]` aliases.
- **FirebaseExtension**: The `firebase { }` DSL block exposed on `AndroidApplicationConventionPlugin`.
- **DataLayerFirebaseConfig**: The `firebase { }` DSL block nested inside `dataLayer { }` on `AndroidDataLayerConventionPlugin`.
- **BOM**: A Maven Bill of Materials dependency used as a `platform()` dependency to align Firebase library versions.
- **updateLibFileVersion**: The Gradle task in `conventions/build.gradle.kts` that generates `Constants.kt` by reading the Consumer Catalog TOML.
- **Constants_kt**: The generated `Constants.kt` file baked into the plugin JAR that embeds the TOML content and parsed metadata.
- **AppspirimentLibRef**: The data class in `LibsData.kt` that holds parsed catalog metadata (versions, plugins, libraries).

---

## Requirements

### Requirement 1: Slim the Distributed Consumer Catalog

**User Story:** As a consumer Android developer, I want the distributed `appspirimentlibs.versions.toml` to contain only version strings and Appspiriment plugin aliases, so that my project catalog is not polluted with library aliases and bundles that the convention plugins manage internally.

#### Acceptance Criteria

1. THE Consumer_Catalog SHALL contain a `[versions]` section with all version strings currently present (SDK targets, all library versions, tooling versions, Appspiriment library versions).
2. THE Consumer_Catalog SHALL contain a `[plugins]` section with exactly the following four Appspiriment plugin aliases: `appspiriment-application`, `appspiriment-library`, `appspiriment-data`, and `appspiriment-firebase`.
3. THE Consumer_Catalog SHALL NOT contain a `[libraries]` section.
4. THE Consumer_Catalog SHALL NOT contain a `[bundles]` section.
5. THE Consumer_Catalog SHALL NOT contain standard tooling plugin aliases (e.g. `google-android-application`, `kotlin-android`, `dagger-hilt-android`, `devtools-ksp`, `kotlinx-serialization`, `google-services`, `firebase-crashlytics`) in the `[plugins]` section.
6. THE Consumer_Catalog SHALL retain the `firebaseBom`, `googleServices`, and `firebasecrashlyticsplugin` version keys in `[versions]` so consumers can override Firebase and Google Services versions.
7. THE Consumer_Catalog in `project-template/root/gradle/appspirimentlibs.versions.toml` SHALL be identical in structure to `gradle/appspirimentlibs.versions.toml`.

---

### Requirement 2: Coordinate-Based Dependency Resolution in Plugin Code

**User Story:** As a plugin maintainer, I want all dependency coordinates hardcoded in Kotlin plugin code and only version strings read from the consumer catalog, so that removing `[libraries]` and `[bundles]` from the Consumer Catalog does not break any plugin.

#### Acceptance Criteria

1. THE Convention_Plugin SHALL NOT call `findLibrary()` on the consumer catalog (`appspirimentLibs`) for any internally-managed dependency.
2. THE Convention_Plugin SHALL NOT call `findBundle()` on the consumer catalog (`appspirimentLibs`) for any internally-managed dependency.
3. THE Convention_Plugin SHALL read version strings from the consumer catalog using `getVersion()` (or equivalent `findVersion()`) calls only.
4. THE `Dependency` model in `Extensions.kt` SHALL support a `coordinates` field (a `String` in `group:artifact:version` form) as an alternative to the existing `aliases` field, so that hardcoded coordinates can be passed directly to the dependency handler.
5. THE `implementDependency` function in `Extensions.kt` SHALL resolve a `Dependency` using its `coordinates` field when `aliases` is empty or absent, adding the dependency string directly without a catalog lookup.
6. THE `Dependencies.kt` file SHALL replace all `ImplType.BUNDLE` entries in `baseDependencies`, `composeDependencies`, `hiltDependencies`, `utilDependencies`, and `composeUtilDependencies` with explicit `ImplType.DEPENDENCY` entries using hardcoded `coordinates` strings and version values read from the consumer catalog.
7. THE `AndroidDataLayerConventionPlugin` SHALL replace all `Dependency(aliases = listOf(...))` calls with coordinate-based `Dependency` instances that embed the group, artifact, and version read from the consumer catalog.
8. WHEN a version key referenced by a hardcoded coordinate is absent from the consumer catalog, THE Convention_Plugin SHALL throw an `IllegalStateException` with a message identifying the missing version key and the catalog name.

---

### Requirement 3: `firebase { }` Extension on the Application Plugin

**User Story:** As a consumer Android developer, I want a `firebase { }` block on the application plugin, so that I can opt in to individual Firebase services without manually managing Firebase BOM, plugin application, or `google-services.json` validation.

#### Acceptance Criteria

1. THE `AndroidApplicationConventionPlugin` SHALL expose a `firebase { }` DSL block of type `FirebaseExtension` on the applying project.
2. THE `FirebaseExtension` SHALL expose the following `Property<Boolean>` fields, all defaulting to `false`: `analytics`, `crashlytics`, `auth`, `messaging`.
3. WHEN no `FirebaseExtension` property is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL NOT add any Firebase dependency or apply any Firebase-related Gradle plugin.
4. WHEN any `FirebaseExtension` property is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL add `com.google.firebase:firebase-bom:<firebaseBom version>` as a `platform()` dependency under `implementation`.
5. WHEN `analytics` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL add `com.google.firebase:firebase-analytics-ktx` as an `implementation` dependency (version managed by the BOM).
6. WHEN `auth` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL add `com.google.firebase:firebase-auth` as an `implementation` dependency (version managed by the BOM).
7. WHEN `messaging` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL add `com.google.firebase:firebase-messaging-ktx` as an `implementation` dependency (version managed by the BOM).
8. WHEN `crashlytics` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL add `com.google.firebase:firebase-crashlytics` as an `implementation` dependency (version managed by the BOM).
9. WHEN `crashlytics` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL apply the `com.google.gms.google-services` Gradle plugin with version read from the `googleServices` key in the consumer catalog.
10. WHEN `crashlytics` is set to `true`, THE `AndroidApplicationConventionPlugin` SHALL apply the `com.google.firebase.crashlytics` Gradle plugin with version read from the `firebasecrashlyticsplugin` key in the consumer catalog.
11. WHEN `crashlytics` is set to `true` AND the file `google-services.json` does not exist in the module's project directory, THE `AndroidApplicationConventionPlugin` SHALL throw an `IllegalStateException` at configuration time with a message that names the expected file path and instructs the developer to download it from the Firebase console.
12. WHEN `crashlytics` is set to `true` AND `google-services.json` exists in the module's project directory, THE `AndroidApplicationConventionPlugin` SHALL proceed without error.
13. THE `FirebaseExtension` BOM version SHALL be read from the `firebaseBom` version key in the consumer catalog.

---

### Requirement 4: `firebase { }` Block Inside `dataLayer { }` on the Data Plugin

**User Story:** As a consumer Android developer, I want a `firebase { }` block inside `dataLayer { }`, so that Firebase data-layer dependencies (database, storage, remote config) can be added to data modules without coupling them to app-level Firebase setup.

#### Acceptance Criteria

1. THE `DataLayerExtension` SHALL expose a nested `firebase` property of type `DataLayerFirebaseConfig` accessible via a `firebase { }` DSL action.
2. THE `DataLayerFirebaseConfig` SHALL expose the following `Property<Boolean>` fields, all defaulting to `false`: `database`, `storage`, `remoteConfig`.
3. WHEN no `DataLayerFirebaseConfig` property is set to `true`, THE `AndroidDataLayerConventionPlugin` SHALL NOT add any Firebase dependency.
4. WHEN any `DataLayerFirebaseConfig` property is set to `true`, THE `AndroidDataLayerConventionPlugin` SHALL add `com.google.firebase:firebase-bom:<firebaseBom version>` as a `platform()` dependency under `implementation`.
5. WHEN `database` is set to `true`, THE `AndroidDataLayerConventionPlugin` SHALL add `com.google.firebase:firebase-database-ktx` as an `implementation` dependency (version managed by the BOM).
6. WHEN `storage` is set to `true`, THE `AndroidDataLayerConventionPlugin` SHALL add `com.google.firebase:firebase-storage-ktx` as an `implementation` dependency (version managed by the BOM).
7. WHEN `remoteConfig` is set to `true`, THE `AndroidDataLayerConventionPlugin` SHALL add `com.google.firebase:firebase-config-ktx` as an `implementation` dependency (version managed by the BOM).
8. THE `AndroidDataLayerConventionPlugin` SHALL NOT apply `com.google.gms.google-services` or `com.google.firebase.crashlytics` Gradle plugins — those are app-level concerns only.
9. THE `DataLayerFirebaseConfig` BOM version SHALL be read from the `firebaseBom` version key in the consumer catalog.

---

### Requirement 5: Update `updateLibFileVersion` Task

**User Story:** As a plugin maintainer, I want the `updateLibFileVersion` task to correctly parse and embed the slim Consumer Catalog, so that `Constants.kt` reflects the new TOML structure (no `[libraries]`, no `[bundles]`) and the generated `AppspirimentLibRef` remains accurate.

#### Acceptance Criteria

1. THE `updateLibFileVersion` task SHALL parse the `[versions]` section of the Consumer Catalog and populate `AppspirimentLibRef.versions` with all version keys.
2. THE `updateLibFileVersion` task SHALL parse the `[plugins]` section of the Consumer Catalog and populate `AppspirimentLibRef.plugins` with all plugin IDs found.
3. THE `updateLibFileVersion` task SHALL NOT attempt to parse a `[libraries]` section; `AppspirimentLibRef.libraries` SHALL be an empty list in the generated `Constants.kt`.
4. THE `updateLibFileVersion` task SHALL NOT attempt to parse a `[bundles]` section; `AppspirimentLibRef.bundles` (if present) SHALL be an empty list in the generated `Constants.kt`.
5. WHEN the Consumer Catalog TOML file does not exist, THE `updateLibFileVersion` task SHALL log a warning and skip generation without failing the build.
6. THE generated `Constants.kt` SHALL embed the full TOML content as the `appspirimentTomlContents` string constant, with the `LIBVERSION` placeholder replaced by the current plugin version.

---

### Requirement 6: Update Project Template

**User Story:** As a consumer Android developer setting up a new project from the template, I want the template's root `build.gradle.kts` and `app/build.gradle.kts` to reflect the slim catalog and the new Firebase DSL, so that the template compiles correctly and demonstrates current best practices.

#### Acceptance Criteria

1. THE `project-template/root/build.gradle.kts` SHALL declare standard tooling plugins (`com.android.application`, `kotlin-android`, `dagger-hilt-android`, `devtools-ksp`, `kotlinx-serialization`, etc.) using their string plugin IDs and version strings read from `appspirimentlibs.versions.*`, not using catalog aliases that no longer exist in the slim TOML.
2. THE `project-template/root/build.gradle.kts` SHALL declare the four Appspiriment plugin aliases using `alias(appspirimentlibs.plugins.appspiriment.*)` with `apply false`, since those aliases remain in the slim TOML.
3. THE `project-template/root/app/build.gradle.kts` SHALL include a commented-out `firebase { }` block demonstrating all four properties (`analytics`, `crashlytics`, `auth`, `messaging`) with example values.
4. THE `project-template/root/app/build.gradle.kts` SHALL NOT reference any catalog alias from `[libraries]` or `[bundles]` that no longer exists in the slim TOML.
5. WHEN a consumer project is created from the template and synced in Android Studio, THE template SHALL produce no unresolved catalog alias errors.

---

### Requirement 7: Consolidate Library Plugins into One

**User Story:** As a consumer Android developer, I want a single `appspiriment.library` plugin with `hilt` and `compose` capability flags in the `appspiriment { }` extension, so that I don't need to remember four different plugin IDs and can express capabilities as explicit configuration.

#### Acceptance Criteria

1. THE four existing plugin IDs (`io.github.appspiriment.library`, `io.github.appspiriment.library-hilt`, `io.github.appspiriment.library-compose`, `io.github.appspiriment.library-hilt-compose`) SHALL be replaced by a single plugin ID: `io.github.appspiriment.library`.
2. THE `AppspirimentExtension` (the `appspiriment { }` block) SHALL expose two new `Property<Boolean>` fields: `hilt` (default `false`) and `compose` (default `false`).
3. WHEN `hilt.set(true)` is configured, THE `io.github.appspiriment.library` plugin SHALL apply the Hilt Gradle plugins (`dagger-hilt-android`, `devtools-ksp`, `kotlinx-serialization`) and add Hilt dependencies (`hilt-android` implementation, `hilt-android-compiler` ksp, `kotlinx-serialization-json` implementation).
4. WHEN `compose.set(true)` is configured, THE `io.github.appspiriment.library` plugin SHALL apply the Compose Gradle plugins (`kotlin-compose`, `kotlinx-serialization`) and add the full Compose dependency stack (BOM as platform, `android-compose` bundle equivalent, `hilt-navigation-compose`, `ui-tooling` debugImplementation, `ui-test-manifest` debugImplementation, `ui-test-junit4` androidTestImplementation).
5. WHEN both `hilt.set(true)` and `compose.set(true)` are configured, THE plugin SHALL add all Hilt and Compose dependencies without duplication (specifically `hilt-navigation-compose` and `kotlinx-serialization` plugin are added exactly once).
6. WHEN neither `hilt` nor `compose` is set to `true`, THE `io.github.appspiriment.library` plugin SHALL behave identically to the current `io.github.appspiriment.library` plugin (base Android library with no Hilt or Compose).
7. THE `io.github.appspiriment.application` plugin SHALL NOT be affected by this change — it continues to enable Hilt and Compose unconditionally.
8. THE `AndroidLibraryHiltConventionPlugin`, `AndroidLibraryComposeConventionPlugin`, and `AndroidLibraryHiltComposeConventionPlugin` classes SHALL be removed; their logic SHALL be absorbed into `AndroidLibraryConventionPlugin` reading from `AppspirimentExtension`.
9. THE Consumer Catalog `[plugins]` section SHALL contain only `appspiriment-library` (not `appspiriment-library-hilt`, `appspiriment-library-compose`, or `appspiriment-library-hilt-compose`).
10. THE project template SHALL be updated to demonstrate the new single-plugin pattern with `hilt.set(true)` and `compose.set(true)` in the `appspiriment { }` block.

---

### Requirement 8: New `appspiriment.firebase` Plugin

**User Story:** As a consumer Android developer, I want a dedicated `io.github.appspiriment.firebase` plugin that I apply alongside my module plugin, so that Firebase setup is completely opt-in and not bundled into the application plugin.

#### Acceptance Criteria

1. A new plugin `io.github.appspiriment.firebase` SHALL be created, implemented by `AndroidFirebaseConventionPlugin`.
2. THE `AndroidFirebaseConventionPlugin` SHALL expose a `firebase { }` DSL block of type `FirebaseExtension` on the applying project.
3. THE `FirebaseExtension` SHALL expose: `analytics`, `crashlytics`, `auth`, `messaging` (app-level services, `Property<Boolean>`, default `false`) and `database`, `storage`, `remoteConfig` (data-layer services, `Property<Boolean>`, default `false`).
4. THE `AndroidFirebaseConventionPlugin` SHALL detect whether it is applied to an application module (has `com.android.application` plugin) or a library module, and apply `google-services` and `crashlytics` Gradle plugins only when applied to an application module with `crashlytics.set(true)`.
5. THE `google-services.json` validation (fail with clear error if missing) SHALL only run when `crashlytics.set(true)` AND the plugin is applied to an application module.
6. THE `AndroidApplicationConventionPlugin` SHALL NOT contain any Firebase logic — Firebase is handled entirely by `AndroidFirebaseConventionPlugin`.
7. THE `DataLayerExtension` SHALL NOT contain a `firebase { }` block — Firebase data dependencies are handled by `AndroidFirebaseConventionPlugin` when applied to a library/data module.
8. THE Consumer Catalog `[plugins]` section SHALL include `appspiriment-firebase = { id = "io.github.appspiriment.firebase", version.ref = "appspiriment" }`.
9. THE project template `app/build.gradle.kts` SHALL demonstrate applying both `appspiriment.application` and `appspiriment.firebase` with a commented-out `firebase { }` block.
10. THE project template SHALL include an example data module `build.gradle.kts` demonstrating `appspiriment.library` + `appspiriment.firebase` with `database.set(true)`.
