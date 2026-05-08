# Changelog

All notable changes to the Appspiriment Convention Plugins are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows `MAJOR.MINOR.PATCH` for releases and `MAJOR.MINOR.PATCH.dev-NN` for snapshots.

---

## [Unreleased]

### Changed
- `retrofitSerialization` converter migrated from the archived JakeWharton library
  (`com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter`) to Retrofit's
  own official converter (`com.squareup.retrofit2:converter-kotlinx-serialization`).
  **Action required**: if you use `useKotlinSerialization.set(true)` in `dataLayer { }`,
  no code change is needed — the dependency alias is the same. The underlying artifact changed.

---

## [0.0.14] — Current stable

### Added
- `io.github.appspiriment.data` plugin with opt-in Room, Retrofit, DataStore, Security,
  and WorkManager support via the `dataLayer { }` extension block.
- `addDevSuffixToDebug` extension property: appends `.dev` to `applicationId` and
  `.yyyyMMdd.HHmmss` to `versionName` in debug builds. Enabled by default, opt-out via
  `addDevSuffixToDebug.set(false)`.
- Project template in `project-template/` replaces the old `io.github.appspiriment.project`
  plugin. Includes `upgradeAppspiriment` Gradle task for version upgrades.
- Gradle TestKit test suite covering versioning tasks and date suffix format.

### Changed
- `AppspirimentExtension`, `RoomConfig`, `RetrofitConfig`, `SimpleConfig` changed from
  interfaces to abstract classes for consistent Gradle managed-property support.
- `projectConfigs` now cached in `extensions.extraProperties` — avoids repeated catalog
  lookups in multi-module builds.
- `composeDependencies` changed from computed property to `val` for consistency.
- `bumpDevVersion` task now only declares `outputs` (not `inputs`) — prevents Gradle from
  incorrectly marking it as UP-TO-DATE on the second run.
- `publishDev` is now a two-step workflow: `./gradlew publishDev` bumps the version,
  `./gradlew publishToMavenLocal` publishes with the new version.
- `compose-compiler-gradle-plugin` and `gradle-plugin-publish` moved to version catalog.
- `KotlinCompile` → `KotlinJvmCompile` to avoid deprecation warnings in Kotlin 2.x.
- `resources` module rewritten as `com.android.library` (was `java-library`) — resources
  are now correctly packaged as AAR for proper Android resource merging.

### Removed
- `io.github.appspiriment.project` plugin — replaced by the project template.
- `multidex` from base bundle — minSdk 26+ has native multidex support.
- `androidx-ui-tooling` from compose bundle — now correctly `debugImplementation` only.
- `androidx-material` (Material 1) from compose bundle — use Material 3 by default.
- `androidx-ui-text-google-fonts` from compose bundle — opt-in only.
- `androidx-room-common` from distributed catalog — internal Room artifact.
- Dead legacy support library entries from `libs.versions.toml`.

### Fixed
- `buildTypes` was incorrectly nested inside `defaultConfig` — moved to correct scope.
- Duplicate `KotlinAndroidProjectExtension` configure block — removed duplicate.
- Accidental `import com.android.tools.r8.internal.re` in library plugin — removed.
- `hilt-compose-navigation` was added twice when both Hilt and Compose were active — fixed.
- `POM_URL` placeholder `https://github.com/username/mylibrary/` — replaced with real URL.

---

## Upgrade Guide

### From any version to 0.0.14+

1. Remove `id("io.github.appspiriment.project")` from your root `build.gradle.kts`.
2. Copy `project-template/root/gradle/appspirimentlibs.versions.toml` into your project's
   `gradle/` folder (or run `./gradlew upgradeAppspiriment -PnewVersion=0.0.14`).
3. Add the `upgradeAppspiriment` task to your root `build.gradle.kts` (see template).
4. Sync Gradle.

For future upgrades: `./gradlew upgradeAppspiriment -PnewVersion=<version>` then sync.
