# Design Document: catalog-slimdown-firebase-plugin

## Overview

This feature refactors the Appspiriment convention plugin library along three axes:

1. **Catalog slimdown** — The distributed `appspirimentlibs.versions.toml` is reduced to a versions-only + 4 plugin aliases file. All `[libraries]` and `[bundles]` sections are removed. Plugin Kotlin code takes ownership of every `group:artifact` coordinate; only version strings are read from the consumer catalog.

2. **Library plugin consolidation** — The four `io.github.appspiriment.library*` plugin IDs collapse into one. Hilt and Compose capabilities become opt-in boolean flags on the existing `appspiriment { }` extension block.

3. **Firebase plugin** — A new `io.github.appspiriment.firebase` plugin is introduced. It exposes a `firebase { }` DSL block covering both app-level services (analytics, crashlytics, auth, messaging) and data-layer services (database, storage, remoteConfig). It detects the module type at configuration time and applies `google-services` / `crashlytics` Gradle plugins only when applied to an application module with `crashlytics = true`.

The guiding principle throughout is: **the plugin owns all dependency coordinates; the consumer catalog owns only version strings.**

---

## Architecture

### Before vs After: Dependency Resolution

```
BEFORE
──────
Consumer catalog (appspirimentlibs.versions.toml)
  [versions]  ← version strings
  [libraries] ← group:artifact aliases  ← plugin calls findLibrary() / findBundle()
  [bundles]   ← grouped aliases
  [plugins]   ← 10+ plugin aliases

Plugin code
  Dependencies.kt: Dependency(aliases = listOf("android-base"))  ← catalog lookup
  AndroidDataLayerConventionPlugin: findLibrary("androidx-room-runtime")  ← catalog lookup

AFTER
─────
Consumer catalog (appspirimentlibs.versions.toml)
  [versions]  ← version strings only
  [plugins]   ← exactly 4 Appspiriment plugin aliases

Plugin code
  Dependencies.kt: Dependency(coordinates = listOf("androidx.core:core-ktx:${libs.getVersion("coreKtx")}"))
  AndroidDataLayerConventionPlugin: Dependency(coordinates = listOf("androidx.room:room-runtime:${libs.getVersion("room")}"))
```

### Plugin Hierarchy (After)

```
AndroidBaseConventionPlugin  (abstract)
├── AndroidApplicationConventionPlugin   — always enables Hilt + Compose
└── AndroidLibraryConventionPlugin       — reads hilt/compose from AppspirimentExtension

AndroidFirebaseConventionPlugin          — standalone, detects module type
AndroidDataLayerConventionPlugin         — standalone data-layer plugin
```

### Configuration Flow

```
Consumer build script evaluated
        │
        ▼
Plugin.apply() called
  ├── extensions.create("appspiriment", AppspirimentExtension)
  ├── extensions.create("firebase", FirebaseExtension)   [FirebasePlugin only]
  ├── Apply mandatory Android/Kotlin plugins (by string ID)
  ├── configureAndroidEarly()
  └── applyCoreDependencies()  [coordinate-based, no catalog lookup]
        │
        ▼
afterEvaluate { }
  ├── Read AppspirimentExtension values
  ├── Read FirebaseExtension values        [FirebasePlugin only]
  ├── Conditionally apply Hilt/Compose     [LibraryPlugin only]
  ├── Conditionally add Firebase deps      [FirebasePlugin only]
  ├── Validate google-services.json        [FirebasePlugin, app + crashlytics only]
  └── configureAndroidLate()
```

---

## Components and Interfaces

### 1. `Extensions.kt` — Updated `Dependency` Model

The `Dependency` data class gains a `coordinates` field. `implementDependency` gains a new resolution path that adds coordinate strings directly without any catalog lookup.

```kotlin
// BEFORE
data class Dependency(
    val type: ImplType = ImplType.DEPENDENCY,
    val config: String = IMPLEMENTATION_CONFIGURATION_NAME,
    val aliases: List<String>
)

// AFTER
data class Dependency(
    val type: ImplType = ImplType.DEPENDENCY,
    val config: String = IMPLEMENTATION_CONFIGURATION_NAME,
    val aliases: List<String> = emptyList(),
    val coordinates: List<String> = emptyList()   // NEW: pre-built "group:artifact:version" strings
)
```

`ImplType.COORDINATE` is added as a new enum value. `implementDependency` dispatches on it:

```kotlin
internal fun DependencyHandlerScope.implementDependency(
    libs: VersionCatalog,
    dependencyList: List<Dependency>
) {
    dependencyList.forEach { dep ->
        when {
            dep.coordinates.isNotEmpty() -> {
                dep.coordinates.forEach { coord ->
                    if (dep.type == ImplType.PLATFORM) add(dep.config, platform(coord))
                    else add(dep.config, coord)
                }
            }
            dep.type == ImplType.BUNDLE -> implement(libs, dep.config, dep.aliases, isBundle = true)
            dep.type == ImplType.PLATFORM -> implement(libs, dep.config, dep.aliases, isPlatform = true)
            dep.type == ImplType.PROJECT -> dep.aliases.forEach { add(dep.config, project(it)) }
            else -> implement(libs, dep.config, dep.aliases)
        }
    }
}
```

The `ImplType.COORDINATE` enum value is added for clarity, though the dispatch above uses `coordinates.isNotEmpty()` as the primary signal to avoid breaking existing callers that pass `aliases`.

### 2. `Dependencies.kt` — Coordinate-Based Dependency Lists

All dependency list functions are converted to accept a `VersionCatalog` parameter and return coordinate-based `Dependency` instances. The `val` properties become `fun` functions.

```kotlin
// BEFORE
val baseDependencies: List<Dependency> = listOf(
    Dependency(type = ImplType.BUNDLE, config = IMPLEMENTATION_CONFIGURATION_NAME, aliases = listOf("android-base")),
    Dependency(type = ImplType.BUNDLE, config = TEST_IMPLEMENTATION_CONFIGURATION_NAME, aliases = listOf("unit-test"))
)

// AFTER
fun baseDependencies(libs: VersionCatalog) = listOf(
    Dependency(
        config = IMPLEMENTATION_CONFIGURATION_NAME,
        coordinates = listOf(
            "androidx.core:core-ktx:${libs.getVersion("coreKtx")}",
            "androidx.lifecycle:lifecycle-runtime-ktx:${libs.getVersion("lifecycleRuntimeKtx")}",
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.getVersion("kotlinxCoroutines")}"
        )
    ),
    Dependency(
        config = TEST_IMPLEMENTATION_CONFIGURATION_NAME,
        coordinates = listOf(
            "junit:junit:${libs.getVersion("junit")}",
            "org.mockito.kotlin:mockito-kotlin:${libs.getVersion("mockito")}",
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.getVersion("kotlinxCoroutines")}",
            "app.cash.turbine:turbine:${libs.getVersion("turbine")}"
        )
    )
)
```

All five lists are converted: `baseDependencies`, `composeDependencies`, `hiltDependencies`, `utilDependencies`, `composeUtilDependencies`.

Plugin lists (`basePluginList`, `composePluginList`, `hiltPluginList`) become string plugin IDs applied directly via `pluginManager.apply(id)` rather than catalog lookups. The `applyPluginFromLibs` helper is retained for backward compatibility but its internal callers in `AndroidBaseConventionPlugin` are updated to use direct `pluginManager.apply()` calls.

### 3. `CustomExtensions.kt` — New and Updated Extensions

#### `AppspirimentExtension` — Two New Fields

```kotlin
abstract class AppspirimentExtension @Inject constructor(objects: ObjectFactory) {
    abstract val enableUtils: Property<Boolean>
    abstract val enableMinify: Property<Boolean>
    abstract val addDevSuffixToDebug: Property<Boolean>
    abstract val hilt: Property<Boolean>    // NEW — default false, library modules only
    abstract val compose: Property<Boolean> // NEW — default false, library modules only
}
```

#### New `FirebaseExtension`

```kotlin
abstract class FirebaseExtension @Inject constructor(objects: ObjectFactory) {
    // App-level services
    abstract val analytics: Property<Boolean>
    abstract val crashlytics: Property<Boolean>
    abstract val auth: Property<Boolean>
    abstract val messaging: Property<Boolean>
    // Data-layer services
    abstract val database: Property<Boolean>
    abstract val storage: Property<Boolean>
    abstract val remoteConfig: Property<Boolean>
}
```

All properties default to `false` (Gradle `Property<Boolean>` returns `null` when unset; callers use `getOrElse(false)`).

A convenience extension function is added:

```kotlin
fun Project.firebase(configure: Action<FirebaseExtension>) {
    extensions.configure("firebase", configure)
}
```

### 4. `AndroidLibraryConventionPlugin.kt` — Consolidated Single Plugin

The three subclasses are deleted. `AndroidLibraryConventionPlugin` reads `hilt` and `compose` from `AppspirimentExtension` in `afterEvaluate`:

```kotlin
open class AndroidLibraryConventionPlugin : AndroidBaseConventionPlugin() {
    override val Project.commonExtension get() = extensions.getByType<LibraryExtension>()

    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("com.android.library")
            super.apply(this)
            // hilt/compose are read in afterEvaluate inside super.apply() via the
            // overridden configureCapabilities() hook
        }
    }
}
```

`AndroidBaseConventionPlugin` gains a protected open `configureCapabilities(config: AppspirimentExtension)` hook called inside `afterEvaluate`. `AndroidLibraryConventionPlugin` overrides it to conditionally call `setupHilt()` / `setupCompose()`:

```kotlin
// In AndroidBaseConventionPlugin.afterEvaluate:
configureCapabilities(config)

// In AndroidLibraryConventionPlugin:
override fun Project.configureCapabilities(config: AppspirimentExtension) {
    if (config.hilt.getOrElse(false)) setupHilt()
    if (config.compose.getOrElse(false)) setupCompose()
}
```

`AndroidApplicationConventionPlugin` does not override `configureCapabilities` — it calls `setupHilt()` and `setupCompose()` unconditionally in its `apply()` before `super.apply()`, preserving existing behavior.

### 5. `AndroidFirebaseConventionPlugin.kt` — New File

```kotlin
class AndroidFirebaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.create("firebase", FirebaseExtension::class.java)

            afterEvaluate {
                val config = extensions.getByType<FirebaseExtension>()
                val libs = appspirimentLibs
                val isAppModule = pluginManager.hasPlugin("com.android.application")

                val anyEnabled = listOf(
                    config.analytics, config.crashlytics, config.auth, config.messaging,
                    config.database, config.storage, config.remoteConfig
                ).any { it.getOrElse(false) }

                if (!anyEnabled) return@afterEvaluate

                dependencies {
                    // BOM — always added when any service is enabled
                    add("implementation", platform(
                        "com.google.firebase:firebase-bom:${libs.getVersion("firebaseBom")}"
                    ))

                    // App-level services (BOM-managed, no version)
                    if (config.analytics.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-analytics-ktx")
                    if (config.auth.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-auth")
                    if (config.messaging.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-messaging-ktx")
                    if (config.crashlytics.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-crashlytics")

                    // Data-layer services (BOM-managed, no version)
                    if (config.database.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-database-ktx")
                    if (config.storage.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-storage-ktx")
                    if (config.remoteConfig.getOrElse(false))
                        add("implementation", "com.google.firebase:firebase-config-ktx")
                }

                // App-level Gradle plugins — only for application modules with crashlytics
                if (config.crashlytics.getOrElse(false) && isAppModule) {
                    val gsJsonFile = file("${projectDir}/google-services.json")
                    if (!gsJsonFile.exists()) {
                        throw IllegalStateException(
                            "google-services.json not found at ${gsJsonFile.absolutePath}. " +
                            "Download it from the Firebase console (Project Settings → Your apps) " +
                            "and place it in the app module directory."
                        )
                    }
                    pluginManager.apply("com.google.gms.google-services")
                    pluginManager.apply("com.google.firebase.crashlytics")
                }
            }
        }
    }
}
```

**Design decision**: `google-services.json` validation runs inside `afterEvaluate` before any dependency resolution. This is the earliest safe point to read extension values. The `IllegalStateException` thrown here aborts configuration, which is the correct Gradle idiom for a fatal misconfiguration.

**Design decision**: The plugin is intentionally standalone (not extending `AndroidBaseConventionPlugin`). It can be applied alongside any module plugin — application, library, or data — without coupling to the base plugin's lifecycle.

### 6. `AndroidDataLayerConventionPlugin.kt` — Coordinate-Based Deps, No Firebase Block

`DataLayerExtension` is **not** modified — no `firebase {}` block is added there. Firebase data dependencies are handled by `AndroidFirebaseConventionPlugin` when applied to a data module.

All `findLibrary()` calls are replaced with coordinate-based `Dependency` instances:

```kotlin
// BEFORE
implementDependency(libs, listOf(
    Dependency(aliases = listOf("androidx-room-runtime", "androidx-room-ktx"))
))
add("ksp", libs.findLibrary("androidx-room-compiler").get())

// AFTER
implementDependency(libs, listOf(
    Dependency(coordinates = listOf(
        "androidx.room:room-runtime:${libs.getVersion("room")}",
        "androidx.room:room-ktx:${libs.getVersion("room")}"
    ))
))
add("ksp", "androidx.room:room-compiler:${libs.getVersion("room")}")
```

The same pattern applies to all other data-layer dependencies (security, datastore, workManager, retrofit, chucker).

### 7. `AndroidBaseConventionPlugin.kt` — Pass `VersionCatalog` to Dependency Functions

Since `baseDependencies`, `composeDependencies`, etc. become functions taking `VersionCatalog`, the call sites in `AndroidBaseConventionPlugin` are updated:

```kotlin
// BEFORE
implementDependency(libs = appspirimentLibs, dependencyList = baseDependencies)

// AFTER
implementDependency(libs = appspirimentLibs, dependencyList = baseDependencies(appspirimentLibs))
```

Plugin application in `applyMandatoryPlugins()` switches from catalog lookup to direct string IDs:

```kotlin
// BEFORE
pluginManager.applyPluginFromLibs(appspirimentLibs to basePluginList)

// AFTER
pluginManager.apply("org.jetbrains.kotlin.android")
```

### 8. `conventions/build.gradle.kts` — Plugin Registration and Task Updates

#### `gradlePlugin` block

Three library variant registrations are removed; `androidFirebase` is added:

```kotlin
// REMOVED:
create("androidHiltLibrary") { id = "io.github.appspiriment.library-hilt" ... }
create("androidComposeLibrary") { id = "io.github.appspiriment.library-compose" ... }
create("androidHiltComposeLibrary") { id = "io.github.appspiriment.library-hilt-compose" ... }

// ADDED:
create("androidFirebase") {
    id = "io.github.appspiriment.firebase"
    displayName = "Appspiriment Firebase"
    description = "Opt-in Firebase services for app and data layer modules."
    implementationClass = "com.appspiriment.conventions.plugins.AndroidFirebaseConventionPlugin"
}
```

#### `updateLibFileVersion` task

The `[libraries]` parsing block is removed. The `[bundles]` parsing block is removed. `AppspirimentLibRef` is generated with `libraries = listOf()` and `bundles = listOf()` (the `bundles` field already defaults to `emptyList()` in `LibsData.kt`).

```kotlin
// REMOVED from doLast:
"[libraries]" -> {
    val g = Regex("""group\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
    val n = Regex("""name\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
    if (g != null && n != null) libraryRefs.add("Pair(\"$g\", \"$n\")")
}

// Generated output changes:
// BEFORE: libraries = listOf(Pair("androidx.core", "core-ktx"), ...)
// AFTER:  libraries = listOf()
```

---

## Data Models

### `Dependency` (updated)

| Field | Type | Description |
|-------|------|-------------|
| `type` | `ImplType` | Resolution strategy. `COORDINATE` is new; existing values retained. |
| `config` | `String` | Gradle configuration name (e.g. `"implementation"`, `"ksp"`). |
| `aliases` | `List<String>` | Catalog alias names. Used when `coordinates` is empty. |
| `coordinates` | `List<String>` | Pre-built `"group:artifact:version"` strings. Takes priority over `aliases`. |

### `FirebaseExtension`

| Field | Type | Default | Scope |
|-------|------|---------|-------|
| `analytics` | `Property<Boolean>` | `false` | App module |
| `crashlytics` | `Property<Boolean>` | `false` | App module |
| `auth` | `Property<Boolean>` | `false` | App module |
| `messaging` | `Property<Boolean>` | `false` | App module |
| `database` | `Property<Boolean>` | `false` | Any module |
| `storage` | `Property<Boolean>` | `false` | Any module |
| `remoteConfig` | `Property<Boolean>` | `false` | Any module |

### `AppspirimentExtension` (updated)

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `enableUtils` | `Property<Boolean>` | `true` | Existing |
| `enableMinify` | `Property<Boolean>` | `false` | Existing |
| `addDevSuffixToDebug` | `Property<Boolean>` | `true` | Existing |
| `hilt` | `Property<Boolean>` | `false` | **New** — library modules only |
| `compose` | `Property<Boolean>` | `false` | **New** — library modules only |

### Slim Consumer Catalog Structure

```toml
[versions]
# App
versionCode = "1"
versionName = "1.0.0"
# SDK
minSdk = "26"
targetSdk = "36"
compileSdk = "36"
javaVersion = "21"
# Appspiriment
appspiriment = "LIBVERSION"
appspirimentLogUtils = "..."
appspirimentUtils = "..."
appspirimentComposeUtils = "..."
appspirimentUpdateUtils = "..."
# Tooling (consumer can override)
agp = "8.13.1"
kotlin = "2.3.10"
ksp = "2.3.6"
hilt = "2.58"
kotlinserializeplugin = "2.3.10"
# Library versions (consumer can override)
coreKtx = "1.17.0"
composeBom = "2026.02.01"
room = "2.7.2"
retrofit = "2.11.0"
# ... all current library versions ...
# Firebase
firebaseBom = "33.14.0"
googleServices = "4.4.3"
firebasecrashlyticsplugin = "3.0.4"

[plugins]
appspiriment-application = { id = "io.github.appspiriment.application", version.ref = "appspiriment" }
appspiriment-library     = { id = "io.github.appspiriment.library",     version.ref = "appspiriment" }
appspiriment-data        = { id = "io.github.appspiriment.data",        version.ref = "appspiriment" }
appspiriment-firebase    = { id = "io.github.appspiriment.firebase",    version.ref = "appspiriment" }
```

No `[libraries]`, no `[bundles]`.

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Coordinate dependency resolution is catalog-independent

*For any* valid Maven coordinate string in `"group:artifact:version"` form, calling `implementDependency` with a `Dependency(coordinates = listOf(coord))` must add exactly that coordinate string to the project's dependency graph without performing any catalog lookup.

**Validates: Requirements 2.4, 2.5**

---

### Property 2: Firebase BOM appears exactly once for any non-empty flag combination

*For any* non-empty subset of enabled `FirebaseExtension` boolean flags (across all seven: analytics, crashlytics, auth, messaging, database, storage, remoteConfig), the Firebase BOM (`com.google.firebase:firebase-bom`) must appear in the `implementation` configuration exactly once as a `platform()` dependency.

**Validates: Requirements 3.4, 4.4, 8.3**

---

### Property 3: Firebase dependency set equals the union of per-flag dependency sets

*For any* combination of enabled `FirebaseExtension` flags, the set of Firebase artifact coordinates added to the project's `implementation` configuration must be exactly the union of the per-flag dependency sets — no more, no less. Specifically:
- `analytics = true` → `firebase-analytics-ktx`
- `auth = true` → `firebase-auth`
- `messaging = true` → `firebase-messaging-ktx`
- `crashlytics = true` → `firebase-crashlytics`
- `database = true` → `firebase-database-ktx`
- `storage = true` → `firebase-storage-ktx`
- `remoteConfig = true` → `firebase-config-ktx`

**Validates: Requirements 3.5–3.8, 4.5–4.7, 8.3**

---

### Property 4: App-level Gradle plugins never applied to library modules

*For any* combination of `FirebaseExtension` flags, when `AndroidFirebaseConventionPlugin` is applied to a module that does **not** have the `com.android.application` plugin, neither `com.google.gms.google-services` nor `com.google.firebase.crashlytics` shall be applied to that module.

**Validates: Requirements 4.8, 8.4**

---

### Property 5: Library plugin capability set equals the union of per-flag capability sets

*For any* combination of `hilt` and `compose` flags on `AppspirimentExtension` applied to `io.github.appspiriment.library`, the set of applied Gradle plugins and added dependency coordinates must be exactly the union of what the individual old plugins (`library-hilt`, `library-compose`, `library-hilt-compose`) would have added — no more, no less. In particular, `hilt-navigation-compose` and the `kotlinx-serialization` Gradle plugin must appear at most once regardless of flag combination.

**Validates: Requirements 7.3, 7.4, 7.5**

---

### Property 6: TOML version key round-trip through `updateLibFileVersion`

*For any* TOML string containing a `[versions]` section with N distinct keys, the `updateLibFileVersion` task must produce a `Constants.kt` whose `AppspirimentLibRef.versions` list contains exactly those N keys — no more, no fewer.

**Validates: Requirements 5.1**

---

### Property 7: LIBVERSION placeholder substitution

*For any* version string V, the `Constants.kt` generated by `updateLibFileVersion` must contain the full TOML content with every occurrence of the literal string `LIBVERSION` replaced by V, and no other substitutions applied.

**Validates: Requirements 5.6**

---

## Error Handling

### Missing version key in consumer catalog

When plugin code calls `libs.getVersion("someKey")` and the key is absent, `getVersion()` (already implemented in `Extensions.kt`) throws:

```
IllegalStateException: Version alias 'someKey' not found in catalog 'appspirimentlibs'
```

This surfaces immediately at configuration time with a clear message identifying the missing key and the catalog name.

### Missing `google-services.json`

When `crashlytics = true` and the plugin is applied to an application module, `AndroidFirebaseConventionPlugin` checks for `google-services.json` in `afterEvaluate` before applying any Gradle plugins. If absent:

```
IllegalStateException: google-services.json not found at /path/to/app/google-services.json.
Download it from the Firebase console (Project Settings → Your apps) and place it in the app module directory.
```

The check runs before `pluginManager.apply("com.google.gms.google-services")`, so the error is thrown at configuration time rather than during task execution.

### `updateLibFileVersion` with missing TOML

The task already handles this gracefully:

```kotlin
if (!tomlFile.exists()) {
    logger.warn("⚠️ appspirimentlibs.versions.toml not found — skipping Constants.kt generation")
    return@doLast
}
```

No change needed here; the behavior is preserved.

### Applying `appspiriment.firebase` without an Android plugin

If `AndroidFirebaseConventionPlugin` is applied to a module that has neither `com.android.application` nor `com.android.library`, the `dependencies { }` block will fail with a standard Gradle error about unknown configurations. This is acceptable — the plugin is documented for Android modules only. No special handling is added.

---

## Testing Strategy

### Unit Tests (Gradle TestKit)

The existing `ConventionPluginTest.kt` uses Gradle TestKit. New tests should follow the same pattern: create a minimal project in a temp directory, apply the plugin under test, and assert on the resulting project state.

**Key test cases:**

- `Dependency(coordinates = listOf("com.example:lib:1.0"))` resolves without catalog lookup
- `Dependency(coordinates = listOf(...))` with `ImplType.PLATFORM` wraps in `platform()`
- `AndroidFirebaseConventionPlugin` with all flags false → no Firebase deps added
- `AndroidFirebaseConventionPlugin` with `analytics = true` → BOM + analytics dep added
- `AndroidFirebaseConventionPlugin` with `crashlytics = true` on library module → no Gradle plugins applied
- `AndroidFirebaseConventionPlugin` with `crashlytics = true` on app module, no `google-services.json` → `IllegalStateException`
- `AndroidLibraryConventionPlugin` with `hilt = false, compose = false` → same deps as old base library plugin
- `AndroidLibraryConventionPlugin` with `hilt = true` → Hilt plugins and deps added
- `AndroidLibraryConventionPlugin` with `compose = true` → Compose plugins and deps added
- `updateLibFileVersion` on slim TOML → `libraries = listOf()`, `bundles = listOf()`
- `updateLibFileVersion` on slim TOML → `versions` list contains all expected keys

### Property-Based Tests

Property-based tests use [Kotest](https://kotest.io/docs/proptest/property-based-testing.html) (`io.kotest:kotest-property`) with a minimum of 100 iterations per property.

Each test is tagged with: `Feature: catalog-slimdown-firebase-plugin, Property {N}: {property_text}`

**Property 1 test** — Generate random `group:artifact:version` strings (using Kotest `Arb.string()` for each segment), construct `Dependency(coordinates = listOf(coord))`, invoke `implementDependency` against a mock `DependencyHandlerScope`, assert the exact coordinate string was passed to `add()`.

**Property 2 test** — Generate random non-empty subsets of the seven Firebase flag names. For each subset, configure a `FirebaseExtension` with those flags set to `true`, apply `AndroidFirebaseConventionPlugin` to a test project, assert `firebase-bom` appears exactly once in `implementation`.

**Property 3 test** — Same generator as Property 2. Assert the set of non-BOM Firebase artifact IDs in `implementation` equals the expected set derived from the enabled flags.

**Property 4 test** — Generate random non-empty subsets of Firebase flags. Apply `AndroidFirebaseConventionPlugin` to a library module (no `com.android.application`). Assert `com.google.gms.google-services` and `com.google.firebase.crashlytics` are never in the applied plugin set.

**Property 5 test** — Generate random combinations of `hilt ∈ {true, false}` and `compose ∈ {true, false}`. Apply `io.github.appspiriment.library` with those flags. Assert the applied plugin set and dependency set equal the expected union. Assert `hilt-navigation-compose` and `kotlinx-serialization` plugin appear at most once.

**Property 6 test** — Generate random TOML strings with a `[versions]` section containing N random key-value pairs. Run the `updateLibFileVersion` parsing logic. Assert the output `versions` list contains exactly the N keys.

**Property 7 test** — Generate random version strings (alphanumeric + dots + dashes). Run the TOML substitution logic. Assert every `LIBVERSION` occurrence is replaced and no other text is changed.

### Integration / Smoke Tests

- Verify `gradle/appspirimentlibs.versions.toml` contains no `[libraries]` or `[bundles]` sections (file parse check)
- Verify `gradle/appspirimentlibs.versions.toml` `[plugins]` section contains exactly the four expected aliases
- Verify `project-template/root/gradle/appspirimentlibs.versions.toml` is structurally identical to `gradle/appspirimentlibs.versions.toml`
- Verify `conventions/build.gradle.kts` `gradlePlugin` block does not register `library-hilt`, `library-compose`, or `library-hilt-compose`
