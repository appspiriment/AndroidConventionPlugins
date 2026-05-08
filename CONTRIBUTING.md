# Contributing to Appspiriment Convention Plugins

## Project Structure

```
conventions/          — The Gradle plugin module (published to Maven Central)
  src/main/           — Plugin source code
  src/test/           — Gradle TestKit integration tests
resources/            — Android library with default MD3 color/dimen resources
project-template/     — Android Studio project template (replaces the old project plugin)
gradle/
  libs.versions.toml          — Version catalog for THIS project's own build
  appspirimentlibs.versions.toml — The catalog DISTRIBUTED to consumer projects
  kmp.versions.toml           — Reserved for future KMP support
```

## Dev Workflow

### Running Tests

```bash
./gradlew :conventions:test
```

### Publishing a Dev Snapshot to MavenLocal

The publish workflow is intentionally two steps because `version` is captured at
Gradle configuration time. A single invocation cannot bump and publish the new version.

```bash
# Step 1: Bump the DEV counter in pluginversion.properties
./gradlew publishDev

# Step 2: Publish with the new version
./gradlew publishToMavenLocal
```

After publishing, consumer projects that use `mavenLocal()` will pick up the new version.

### Publishing a Release

```bash
./gradlew publishToMavenCentral -PisRelease
```

This uses the `MAJOR` version from `pluginversion.properties` and signs with GPG.

### Updating the Distributed Catalog

The `appspirimentlibs.versions.toml` is the catalog shipped to consumer projects.
When you update it:

1. Edit `gradle/appspirimentlibs.versions.toml`
2. Also update `project-template/root/gradle/appspirimentlibs.versions.toml` to keep them in sync
3. The `updateLibFileVersion` task bakes the catalog into `Constants.kt` at build time

### Adding a New Plugin Capability

The current capability system uses `PluginCapability` enum in `AndroidBaseConventionPlugin`.
To add a new capability (e.g. Firebase):

1. Add `FIREBASE` to the `PluginCapability` enum
2. Add a `setupFirebase()` helper in `AndroidBaseConventionPlugin`
3. Add the dependency list to `Dependencies.kt`
4. Create a new concrete plugin class (e.g. `AndroidLibraryFirebaseConventionPlugin`)
5. Register it in `conventions/build.gradle.kts` under `gradlePlugin { plugins { } }`
6. Add the plugin alias to `gradle/appspirimentlibs.versions.toml`
7. Add the alias to `project-template/root/build.gradle.kts`

### Version Catalog Sync Rule

`gradle/libs.versions.toml` and `gradle/appspirimentlibs.versions.toml` must stay in sync
for toolchain versions (Kotlin, AGP, KSP, Hilt, SDK levels). When updating one, update both.

## Code Style

- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties`)
- KDoc on all public and `internal` API
- Comments on intentional exclusions in version catalogs (e.g. why `multidex` is absent)
- No hardcoded Maven coordinates in plugin source — always use catalog aliases
