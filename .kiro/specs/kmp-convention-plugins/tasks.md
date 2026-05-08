# Implementation Plan: KMP Convention Plugins

## Overview

Implement a KMP/CMP convention plugin suite inside the existing `:conventions` Gradle module, mirroring the architecture of the existing Android plugin suite. All new files go under `conventions/src/main/java/com/appspiriment/conventions/`. The existing Android plugin code is not modified.

The implementation proceeds in layers: catalog → extensions → KMP configuration helpers → base plugin → library plugins → data layer plugin → application plugin → build script wiring → tests.

## Tasks

- [ ] 1. Create the `kmplibs.versions.toml` version catalog
  - Create `gradle/kmplibs.versions.toml` with the full catalog content defined in the design
  - Include `[versions]` section: `compileSdk`, `minSdk`, `targetSdk`, `javaVersion`, `kmpAppspiriment = "LIBVERSION"`, `kotlin`, `coroutines`, `composeMultiplatform`, `koin`, `koinAnnotations`, `ksp`, `ktor`, `sqlDelight`, `datastore`, `kotlinxSerialization`, `turbine`, `appspirimentLogUtils`, `appspirimentUtils`
  - Include `[libraries]` section with all KMP library coordinates: coroutines, serialization, Koin (core/android/compose/compose-viewmodel/test/annotations/ksp-compiler), Ktor (core/okhttp/darwin/cio/js/content-negotiation/logging/serialization-kotlinx-json), SQLDelight (runtime/android-driver/native-driver/sqlite-driver), DataStore (preferences-core), testing (kotlin-test, turbine), Appspiriment utils (logutils-dev/prod, utils)
  - Include `[plugins]` section with aliases for: `kotlin-multiplatform`, `kotlin-compose-compiler`, `compose-multiplatform`, `kotlin-serialization`, `devtools-ksp`, `sqldelight`, `android-library`, `android-application`, and all six `kmp-*` plugin aliases with `version.ref = "kmpAppspiriment"`
  - _Requirements: 11.2, 11.3, 11.4, 11.7_

- [ ] 2. Create KMP extension types (`KmpCustomExtensions.kt`)
  - Create `conventions/src/main/java/com/appspiriment/conventions/extensions/KmpCustomExtensions.kt`
  - Define `internal const val KMP_EXTENSION_NAME = "kmp"` and `internal const val KMP_DATA_LAYER_EXTENSION_NAME = "kmpDataLayer"`
  - Implement `abstract class KmpExtension @Inject constructor(objects: ObjectFactory)` with `Property<Boolean>` fields: `enableIos` (default `false`), `enableDesktop` (default `false`), `enableWasm` (default `false`), `enableMinify` (default `false`), `enableUtils` (default `true`)
  - Implement `abstract class KmpDataLayerExtension @Inject constructor(objects: ObjectFactory)` with nested `sqlDelight: SqlDelightConfig`, `ktor: KtorConfig`, `dataStore: SimpleConfig`, `serialization: SimpleConfig` — each with a corresponding `fun <name>(action: Action<T>)` helper
  - Implement `abstract class SqlDelightConfig @Inject constructor(objects: ObjectFactory)` with `enabled: Property<Boolean>` and `schemaVersion: Property<Int>`
  - Implement `abstract class KtorConfig @Inject constructor(objects: ObjectFactory)` with `enabled: Property<Boolean>` and `useLogging: Property<Boolean>`
  - Reuse `SimpleConfig` from `CustomExtensions.kt` for `dataStore` and `serialization` — do not redefine it
  - Add top-level `fun Project.kmpDataLayer(configure: Action<KmpDataLayerExtension>)` convenience function
  - _Requirements: 2.1, 6.2, 7.1, 8.1, 9.1, 10.1, 13.1, 13.2, 13.3, 13.4, 13.7_

- [ ] 3. Create KMP dependency lists (`KmpDependencies.kt`)
  - Create `conventions/src/main/java/com/appspiriment/conventions/extensions/KmpDependencies.kt`
  - Define plugin alias lists: `kmpBasePluginList` (`kotlin-multiplatform`, `android-library`), `kmpComposePluginList` (`compose-multiplatform`, `kotlin-compose-compiler`), `kmpKoinPluginList` (`devtools-ksp`), `kmpSerializationPluginList` (`kotlin-serialization`), `kmpSqlDelightPluginList` (`sqldelight`)
  - Define `kmpCoreDependencies` — `kotlinx-coroutines-core` on `commonMainImplementation`
  - Define `kmpTestDependencies` — `kotlin-test`, `kotlinx-coroutines-test`, `turbine` on `commonTestImplementation`
  - Define `kmpComposeDependencies` — individual CMP artifacts (`compose.runtime`, `compose.foundation`, `compose.material3`, `compose.ui`, `compose.components.resources`) on `commonMainImplementation`; `compose.uiTooling` on `androidMainDebugImplementation`
  - Define `kmpKoinCommonDependencies` — `koin-core`, `koin-annotations` on `commonMainImplementation`; `koin-test` on `commonTestImplementation`; `koin-android` on `androidMainImplementation`
  - Define `kmpKoinComposeDependencies` — `koin-compose`, `koin-compose-viewmodel` on `commonMainImplementation`
  - Define `kmpKtorCommonDependencies` — `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json` on `commonMainImplementation`
  - Define `kmpUtilDependencies` — `appspiriment-utils` on `commonMainImplementation`; `appspiriment-logutils-dev` on `androidMainDebugImplementation`; `appspiriment-logutils-prod` on `androidMainReleaseImplementation`
  - Use the `Dependency` data class and `ImplType` enum from `Extensions.kt` (no new types needed)
  - _Requirements: 1.5, 1.6, 3.4, 3.5, 3.6, 4.3, 4.4, 4.5, 4.6, 5.2, 5.3, 8.3, 13.5, 14.1, 14.2, 14.3_

- [ ] 4. Create `KmpProjectConfiguration.kt`
  - Create `conventions/src/main/java/com/appspiriment/conventions/extensions/KmpProjectConfiguration.kt`
  - Implement `val Project.kmpLibs: VersionCatalog` accessor using `extensions.getByType<VersionCatalogsExtension>().named(kmpTomlName)` — `kmpTomlName` comes from the generated `KmpConstants.kt`
  - Implement `val Project.kmpProjectConfigs: KmpProjectConfiguration` with the same caching pattern as `projectConfigs` in `ProjectConfiguration.kt` (extra properties keyed by `"kmp.projectConfigs"`)
  - Read `minSdk`, `compileSdk`, `javaVersion` from `kmpLibs` using the existing `getVersion()` helper from `Extensions.kt`
  - Define `data class KmpProjectConfiguration(val minSdk: Int, val compileSdk: Int, val javaVersion: JavaVersion)` — note: no `targetSdk` field (KMP library modules don't set `targetSdk`; the application plugin reads it directly)
  - _Requirements: 1.3, 12.4_

- [ ] 5. Create `KotlinKmp.kt` — KMP target configuration helpers
  - Create `conventions/src/main/java/com/appspiriment/conventions/extensions/KotlinKmp.kt`
  - Implement `internal fun Project.configureKmpEarly(kotlinExtension: KotlinMultiplatformExtension)` — configures the `androidTarget` with `compileSdk`, `minSdk`, and `jvmTarget` from `kmpProjectConfigs`; sets Kotlin compiler options (`jvmTarget`, `-opt-in=kotlin.RequiresOptIn`, `-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi`, `-Xcontext-parameters`)
  - Implement `internal fun Project.configureKmpLate(kotlinExtension: KotlinMultiplatformExtension, kmpExt: KmpExtension)` — reads `enableIos`, `enableDesktop`, `enableWasm` from `kmpExt` and delegates to the per-target helpers
  - Implement `internal fun Project.configureIosTargets(kotlinExtension: KotlinMultiplatformExtension)` — adds `iosArm64()`, `iosSimulatorArm64()`, `iosX64()`; creates `iosMain` source set depending on `commonMain` and `iosTest` depending on `commonTest`
  - Implement `internal fun Project.configureDesktopTarget(kotlinExtension: KotlinMultiplatformExtension)` — adds `jvm("desktop")`; creates `desktopMain` depending on `commonMain` and `desktopTest` depending on `commonTest`
  - Implement `internal fun Project.configureWasmTarget(kotlinExtension: KotlinMultiplatformExtension)` — adds `wasmJs { browser() }`; creates `wasmJsMain` depending on `commonMain` and `wasmJsTest` depending on `commonTest`
  - _Requirements: 1.3, 1.4, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

- [ ] 6. Create `KmpBaseConventionPlugin.kt`
  - Create `conventions/src/main/java/com/appspiriment/conventions/plugins/KmpBaseConventionPlugin.kt`
  - Define `enum class KmpPluginCapability { KOIN, COMPOSE }` at the top of the file
  - Implement `abstract class KmpBaseConventionPlugin : Plugin<Project>` with a `private val capabilities = mutableSetOf<KmpPluginCapability>()`
  - In `apply(target: Project)`:
    1. Register `KmpExtension` under `KMP_EXTENSION_NAME` via `extensions.create`
    2. Apply mandatory plugins via `pluginManager.applyPluginFromLibs(kmpLibs to kmpBasePluginList)`
    3. Configure the Android target early: `extensions.configure<KotlinMultiplatformExtension> { configureKmpEarly(this) }`
    4. Apply core `commonMain`/`commonTest` dependencies via `implementDependency(kmpLibs, kmpCoreDependencies)` and `implementDependency(kmpLibs, kmpTestDependencies)`
    5. In `afterEvaluate`: read `KmpExtension`, call `configureKmpLate`, apply Compose deps if `COMPOSE` in capabilities, apply Koin deps + per-target KSP if `KOIN` in capabilities, apply util deps if `enableUtils` is true
  - Implement `protected fun Project.setupCompose()` — adds `COMPOSE` to capabilities, applies `kmpComposePluginList` plugins
  - Implement `protected fun Project.setupKoin()` — adds `KOIN` to capabilities, applies `kmpKoinPluginList` plugins
  - Implement `internal fun hasCapability(cap: KmpPluginCapability) = cap in capabilities`
  - For Koin KSP in `afterEvaluate`: add `koin-ksp-compiler` to `kspAndroid` always; add to `kspIosArm64`, `kspIosSimulatorArm64`, `kspIosX64` when `enableIos = true`; add to `kspDesktop` when `enableDesktop = true`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 4.2, 4.4, 13.5, 13.6, 14.1, 14.2, 14.3_

- [ ] 7. Create `KmpLibraryConventionPlugin.kt` with all four library plugin variants
  - Create `conventions/src/main/java/com/appspiriment/conventions/plugins/KmpLibraryConventionPlugin.kt`
  - Implement `open class KmpLibraryConventionPlugin : KmpBaseConventionPlugin()` with `open val setupCompose: Boolean = false` and `open val setupKoin: Boolean = false`; in `apply`: call `setupCompose()` if `setupCompose`, call `setupKoin()` if `setupKoin`, then `super.apply(this)`
  - Implement `class KmpLibraryComposeConventionPlugin : KmpLibraryConventionPlugin()` with `override val setupCompose = true`
  - Implement `class KmpLibraryKoinConventionPlugin : KmpLibraryConventionPlugin()` with `override val setupKoin = true`
  - Implement `class KmpLibraryKoinComposeConventionPlugin : KmpLibraryConventionPlugin()` with `override val setupCompose = true` and `override val setupKoin = true`
  - The combined `KmpLibraryKoinComposeConventionPlugin` must not add any plugin or dependency more than once — this is guaranteed by the `capabilities` set in the base class and `applyPluginFromLibs`'s `hasPlugin` guard
  - _Requirements: 1.9, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 4.1, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 8. Create `KmpDataLayerConventionPlugin.kt`
  - Create `conventions/src/main/java/com/appspiriment/conventions/plugins/feature/KmpDataLayerConventionPlugin.kt`
  - Implement `class KmpDataLayerConventionPlugin : KmpBaseConventionPlugin()`
  - In `apply`: create `KmpDataLayerExtension` under `KMP_DATA_LAYER_EXTENSION_NAME`, call `super.apply(this)`, then register a second `afterEvaluate` block that reads both `KmpExtension` and `KmpDataLayerExtension`
  - In the data-layer `afterEvaluate` block, implement all opt-in features:
    - **SQLDelight** (`sqlDelight.enabled`): apply `kmpSqlDelightPluginList`; add `sqldelight-runtime` to `commonMainImplementation`; add `sqldelight-android-driver` to `androidMainImplementation`; add `sqldelight-native-driver` to `iosMainImplementation` if `enableIos`; add `sqldelight-sqlite-driver` to `desktopMainImplementation` if `enableDesktop`; configure SQLDelight extension with `linkSqlite = true`
    - **Ktor** (`ktor.enabled`): apply `kmpSerializationPluginList` (track `serializationPluginApplied`); add `kmpKtorCommonDependencies`; add `ktor-client-okhttp` to `androidMainImplementation`; add `ktor-client-darwin` to `iosMainImplementation` if `enableIos`; add `ktor-client-cio` to `desktopMainImplementation` if `enableDesktop`; add `ktor-client-js` to `wasmJsMainImplementation` if `enableWasm`; add `ktor-client-logging` to `commonMainImplementation` if `useLogging`
    - **DataStore** (`dataStore.enabled`): add `datastore-preferences-core` to `commonMainImplementation` only — do NOT add the Android-only `datastore-preferences` artifact
    - **Serialization** (`serialization.enabled`): apply `kmpSerializationPluginList` only if not already applied; add `kotlinx-serialization-json` to `commonMainImplementation`
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 9.1, 9.2, 9.3, 10.1, 10.2, 10.3, 10.4_

- [ ] 9. Create `KmpApplicationConventionPlugin.kt`
  - Create `conventions/src/main/java/com/appspiriment/conventions/plugins/KmpApplicationConventionPlugin.kt`
  - Implement `class KmpApplicationConventionPlugin : Plugin<Project>` (standalone — does NOT extend `KmpBaseConventionPlugin`)
  - In `apply`:
    1. Register `KmpExtension` under `KMP_EXTENSION_NAME`
    2. Apply plugins from `kmpLibs`: `android-application`, `kotlin-android` (i.e. `org.jetbrains.kotlin.android`), `compose-multiplatform`, `kotlin-compose-compiler`
    3. Configure `ApplicationExtension` early: `compileSdk`, `minSdk`, `targetSdk` (use `compileSdk` value), `compileOptions` with `javaVersion`
    4. Add `kotlinx-coroutines-android` to `implementation`
    5. Add Compose Multiplatform BOM as `platform()` `implementation` dependency; add `compose.runtime`, `compose.ui`, `compose.material3`, `compose.foundation` to `implementation`; add `compose.uiTooling` as `debugImplementation`
    6. In `afterEvaluate`: read `KmpExtension`; enable R8 on release if `enableMinify = true`; add util dependencies if `enableUtils = true` (same pattern as `kmpUtilDependencies` but using standard `implementation`/`debugImplementation`/`releaseImplementation` configs)
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9_

- [ ] 10. Add `updateKmpLibFileVersion` task and wire `KmpConstants.kt` into the build
  - Modify `conventions/build.gradle.kts` to add the `updateKmpLibFileVersion` task, mirroring the existing `updateLibFileVersion` task exactly:
    - Declare `val kmpGeneratedSourceDir = layout.buildDirectory.dir("generated/kmp/kotlin")`
    - Register `updateKmpLibFileVersion` task with `group = "versioning"`, reading `gradle/kmplibs.versions.toml`, replacing `LIBVERSION` with `currentVersion`, parsing `[versions]`/`[plugins]`/`[libraries]` sections, and writing `KmpConstants.kt` to `com/appspiriment/conventions/extensions/KmpConstants.kt` inside `kmpGeneratedSourceDir`
    - The generated `KmpConstants.kt` must define: `internal const val kmpTomlName = "kmplibs"`, `internal const val kmpLibVersion`, `internal const val kmpTomlContents`, and `internal val kmpLibRefs = AppspirimentLibRef(...)`
    - If `gradle/kmplibs.versions.toml` does not exist, log a `warn`-level message and return early without failing
    - Wire `kmpGeneratedSourceDir` into `kotlin { sourceSets.main { kotlin.srcDir(updateKmpLibFileVersion) } }`
    - Add `dependsOn(updateKmpLibFileVersion)` to the `sourcesJar` task matching block (same pattern as `updateLibFileVersion`)
  - _Requirements: 11.1, 11.5, 11.6, 15.5_

- [ ] 11. Register the six KMP plugins in `conventions/build.gradle.kts`
  - Add six `create(...)` blocks inside the existing `gradlePlugin { plugins { } }` block:
    - `"kmpLibrary"` → id `io.github.appspiriment.kmp.library` → `KmpLibraryConventionPlugin`
    - `"kmpLibraryCompose"` → id `io.github.appspiriment.kmp.library-compose` → `KmpLibraryComposeConventionPlugin`
    - `"kmpLibraryKoin"` → id `io.github.appspiriment.kmp.library-koin` → `KmpLibraryKoinConventionPlugin`
    - `"kmpLibraryKoinCompose"` → id `io.github.appspiriment.kmp.library-koin-compose` → `KmpLibraryKoinComposeConventionPlugin`
    - `"kmpData"` → id `io.github.appspiriment.kmp.data` → `KmpDataLayerConventionPlugin`
    - `"kmpApplication"` → id `io.github.appspiriment.kmp.application` → `KmpApplicationConventionPlugin`
  - Each registration must include `displayName` and `description` as specified in the design
  - _Requirements: 15.1, 15.2, 15.3_

- [ ] 12. Checkpoint — verify the conventions module compiles cleanly
  - Ensure all new Kotlin files compile without errors: run `./gradlew :conventions:compileKotlin`
  - Verify `updateKmpLibFileVersion` task runs and generates `KmpConstants.kt` correctly: run `./gradlew :conventions:updateKmpLibFileVersion`
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Write unit tests for `KmpLibraryConventionPlugin` and `KmpLibraryComposeConventionPlugin`
  - Add test class `KmpLibraryConventionPluginTest` to `conventions/src/test/java/com/appspiriment/conventions/` using Gradle TestKit (same pattern as `ConventionPluginTest`)
  - [ ] 13.1 Test: applying the plugin registers the `kmp` extension on the project
    - _Requirements: 1.7, 13.1_
  - [ ] 13.2 Test: default configuration (no `kmp { }` block) applies only the `androidTarget`
    - _Requirements: 1.8, 2.1_
  - [ ] 13.3 Test: `enableIos = true` adds `iosArm64`, `iosSimulatorArm64`, `iosX64` targets and creates `iosMain`/`iosTest` source sets
    - _Requirements: 2.2, 2.3_
  - [ ] 13.4 Test: `enableDesktop = true` adds a `jvm` target named `desktop` and creates `desktopMain`/`desktopTest` source sets
    - _Requirements: 2.4, 2.5_
  - [ ] 13.5 Test: `enableWasm = true` adds `wasmJs` target with browser execution and creates `wasmJsMain`/`wasmJsTest` source sets
    - _Requirements: 2.6, 2.7_
  - [ ] 13.6 Test: `commonMain` always contains `kotlinx-coroutines-core`
    - _Requirements: 1.5_
  - [ ] 13.7 Test: `commonTest` always contains `kotlin-test`, `kotlinx-coroutines-test`, `turbine`
    - _Requirements: 1.6, 14.1, 14.2, 14.3_
  - [ ]* 13.8 Test: `KmpLibraryComposeConventionPlugin` applies `org.jetbrains.compose` and `org.jetbrains.kotlin.plugin.compose`
    - _Requirements: 3.2, 3.3_
  - [ ]* 13.9 Test: `KmpLibraryComposeConventionPlugin` with `enableDesktop = true` adds `compose.desktop.currentOs` to `desktopMain`
    - _Requirements: 3.7_

- [ ] 14. Write unit tests for `KmpLibraryKoinConventionPlugin` and `KmpLibraryKoinComposeConventionPlugin`
  - Add test class `KmpLibraryKoinConventionPluginTest`
  - [ ] 14.1 Test: `com.google.devtools.ksp` plugin is applied
    - _Requirements: 4.2_
  - [ ] 14.2 Test: `koin-core` and `koin-annotations` are in `commonMain`; `koin-android` is in `androidMain`; `koin-test` is in `commonTest`
    - _Requirements: 4.3, 4.4, 4.5, 4.6, 14.5_
  - [ ] 14.3 Test: KSP compiler is added for each enabled target (`kspAndroid` always; `kspIosArm64`/`kspIosSimulatorArm64`/`kspIosX64` when `enableIos = true`; `kspDesktop` when `enableDesktop = true`)
    - _Requirements: 4.4_
  - [ ]* 14.4 Test: `KmpLibraryKoinComposeConventionPlugin` adds `koin-compose` and `koin-compose-viewmodel` to `commonMain`
    - _Requirements: 5.2, 5.3_
  - [ ]* 14.5 Test: `KmpLibraryKoinComposeConventionPlugin` — no plugin or dependency appears more than once (example-based duplication check)
    - _Requirements: 5.4_

- [ ] 15. Write unit tests for `KmpDataLayerConventionPlugin`
  - Add test class `KmpDataLayerConventionPluginTest`
  - [ ] 15.1 Test: no optional dependencies added when no `kmpDataLayer` block is configured
    - _Requirements: 6.3_
  - [ ] 15.2 Test: `sqlDelight.enabled = true` applies the SQLDelight plugin and adds platform-appropriate drivers
    - _Requirements: 7.2, 7.3, 7.4, 7.5, 7.6_
  - [ ] 15.3 Test: `ktor.enabled = true` applies the serialization plugin and adds platform-appropriate Ktor engines
    - _Requirements: 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_
  - [ ] 15.4 Test: `ktor.useLogging = true` adds `ktor-client-logging` to `commonMain`
    - _Requirements: 8.8_
  - [ ] 15.5 Test: `dataStore.enabled = true` adds `datastore-preferences-core` to `commonMain` and does NOT add the Android-only `datastore-preferences` artifact
    - _Requirements: 9.2, 9.3_
  - [ ] 15.6 Test: `serialization.enabled = true` applies the serialization plugin and adds `kotlinx-serialization-json` to `commonMain`
    - _Requirements: 10.2, 10.3_
  - [ ]* 15.7 Test: both `ktor.enabled = true` and `serialization.enabled = true` result in the serialization plugin applied exactly once
    - _Requirements: 10.4_

- [ ] 16. Write unit tests for `KmpApplicationConventionPlugin` and `updateKmpLibFileVersion` task
  - Add test class `KmpApplicationConventionPluginTest`
  - [ ] 16.1 Test: `com.android.application` and `org.jetbrains.kotlin.android` are applied
    - _Requirements: 12.1, 12.2_
  - [ ] 16.2 Test: Compose plugins (`org.jetbrains.compose`, `org.jetbrains.kotlin.plugin.compose`) are applied
    - _Requirements: 12.3_
  - [ ] 16.3 Test: `kotlinx-coroutines-android` is in `implementation` dependencies
    - _Requirements: 12.5_
  - [ ]* 16.4 Test: `kmp.enableMinify = true` enables R8 on the release build type
    - _Requirements: 12.8_
  - Add test cases to `ConventionPluginTest` (or a new `UpdateKmpLibFileVersionTaskTest` class) for the `updateKmpLibFileVersion` task:
  - [ ] 16.5 Test: task skips gracefully (logs warning, does not fail) when `kmplibs.versions.toml` is missing
    - _Requirements: 11.6_
  - [ ]* 16.6 Test: generated `KmpConstants.kt` contains the embedded TOML content and `LIBVERSION` is replaced with the current version
    - _Requirements: 11.5, 15.5_

- [ ] 17. Write property-based tests
  - [ ]* 17.1 Write property test for the no-duplication invariant (Property 1)
    - For random subsets of `{COMPOSE, KOIN}` capabilities and `{IOS, DESKTOP, WASM}` targets, build a project with those capabilities and assert that the set of applied plugin IDs has no duplicates and the set of dependency coordinates has no duplicates
    - **Property 1: No duplication of plugins and dependencies in combined capability plugins**
    - **Validates: Requirements 5.1, 5.4, 10.4**
    - Minimum 100 iterations
  - [ ]* 17.2 Write property test for the LIBVERSION replacement invariant (Property 2)
    - For random valid semantic version strings, run the TOML processing logic and assert: no `LIBVERSION` literal remains, the version string is present, and replacing the version back with `LIBVERSION` yields the original TOML
    - **Property 2: LIBVERSION placeholder replacement is total and correct**
    - **Validates: Requirements 11.5, 15.5**
    - Minimum 100 iterations

- [ ] 18. Final checkpoint — ensure all tests pass
  - Run `./gradlew :conventions:test` and confirm all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- The existing Android plugin code (`AndroidBaseConventionPlugin`, `AndroidLibraryConventionPlugin`, etc.) must not be modified
- `SimpleConfig` from `CustomExtensions.kt` is reused as-is for `dataStore` and `serialization` in `KmpDataLayerExtension`
- `applyPluginFromLibs` from `Extensions.kt` already guards against duplicate plugin application via `hasPlugin` — no additional deduplication logic is needed in the plugin classes
- All reads of `KmpExtension` and `KmpDataLayerExtension` values must happen inside `afterEvaluate` blocks
- KSP for Koin must be added per-target: `kspAndroid` (always), `kspIosArm64`/`kspIosSimulatorArm64`/`kspIosX64` (when `enableIos`), `kspDesktop` (when `enableDesktop`)
- Compose Multiplatform dependencies are accessed via the `org.jetbrains.compose` plugin's extension — there is no standalone CMP BOM; use individual catalog aliases
- The `updateKmpLibFileVersion` task mirrors `updateLibFileVersion` exactly; both must be wired into `sourcesJar`
- Property tests use the JVM test runner (no KMP test runner needed for plugin logic)
