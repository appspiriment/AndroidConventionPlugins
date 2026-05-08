# Appspiriment Project Template

This template bootstraps a new Android project pre-wired with the Appspiriment Convention Plugins. It replaces the old `io.github.appspiriment.project` plugin, which mutated files at Gradle sync time.

## How to Use

### Option A — Android Studio (Recommended)

1. In Android Studio, go to **File → New → Project from Version Control** (or use the template wizard if you've installed the `.zip` template).
2. Copy the contents of `project-template/root/` into your new project root.
3. Replace all occurrences of `com.example.app` with your actual package name.
4. Sync Gradle.

### Option B — Manual

```bash
# 1. Copy the template files into your new project
cp -r project-template/root/. /path/to/your/new/project/

# 2. Replace the placeholder package name
find /path/to/your/new/project -type f -name "*.kt" -o -name "*.kts" -o -name "*.xml" | \
  xargs sed -i 's/com\.example\.app/com.yourcompany.yourapp/g'

# 3. Open in Android Studio and sync
```

## Upgrading the Plugin Version

Run this from your project root:

```bash
./gradlew upgradeAppspiriment -PnewVersion=<new-version>
```

Then sync Gradle. That's it — no other files need to change.

Check available versions at: https://github.com/appspiriment/AndroidConventionPlugins/releases

## Template Structure

```
root/
├── gradle/
│   └── appspirimentlibs.versions.toml   ← The shared version catalog
├── app/
│   └── build.gradle.kts                 ← Uses appspiriment.application plugin
├── build.gradle.kts                     ← Root build file with upgradeAppspiriment task
├── settings.gradle.kts                  ← Registers the appspirimentlibs catalog
└── gradle.properties                    ← Recommended Gradle settings
```

## Adding More Modules

For a feature module with Hilt + Compose:
```kotlin
// feature-login/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library.hilt.compose)
}
android { namespace = "com.yourcompany.yourapp.feature.login" }
```

For a data layer module:
```kotlin
// data/build.gradle.kts
plugins {
    alias(appspirimentlibs.plugins.appspiriment.library.hilt)
    alias(appspirimentlibs.plugins.appspiriment.data)
}
android { namespace = "com.yourcompany.yourapp.data" }

dataLayer {
    room { enabled.set(true) }
    retrofit { enabled.set(true); useChucker.set(true) }
}
```
