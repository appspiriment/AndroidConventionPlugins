// ─────────────────────────────────────────────────────────────────────────────
// Root build file — Appspiriment Convention Plugins
//
// Current plugin version: 0.0.14.dev-208
//
// To upgrade to a newer version:
//   1. Check releases: https://github.com/appspiriment/AndroidConventionPlugins/releases
//   2. Run: ./gradlew upgradeAppspiriment -PnewVersion=<version>
//      OR manually update `appspiriment` in gradle/appspirimentlibs.versions.toml
//   3. Sync Gradle.
// ─────────────────────────────────────────────────────────────────────────────
plugins {
    alias(appspirimentlibs.plugins.google.android.application) apply false
    alias(appspirimentlibs.plugins.google.android.library) apply false
    alias(appspirimentlibs.plugins.kotlin.android) apply false
    alias(appspirimentlibs.plugins.kotlin.compose) apply false
    alias(appspirimentlibs.plugins.kotlin.jvm) apply false
    alias(appspirimentlibs.plugins.devtools.ksp) apply false
    alias(appspirimentlibs.plugins.dagger.hilt.android) apply false
    alias(appspirimentlibs.plugins.kotlinx.serialization) apply false
    alias(appspirimentlibs.plugins.appspiriment.application) apply false
    alias(appspirimentlibs.plugins.appspiriment.library) apply false
    alias(appspirimentlibs.plugins.appspiriment.library.hilt) apply false
    alias(appspirimentlibs.plugins.appspiriment.library.compose) apply false
    alias(appspirimentlibs.plugins.appspiriment.library.hilt.compose) apply false
    alias(appspirimentlibs.plugins.appspiriment.data) apply false
}

// ─────────────────────────────────────────────────────────────────────────────
// Upgrade task — updates the appspiriment version in the TOML catalog.
// Usage: ./gradlew upgradeAppspiriment -PnewVersion=0.1.0
// ─────────────────────────────────────────────────────────────────────────────
tasks.register("upgradeAppspiriment") {
    group = "appspiriment"
    description = "Updates the appspiriment plugin version in appspirimentlibs.versions.toml. " +
            "Pass -PnewVersion=<version> to specify the target version."

    doLast {
        val newVersion = project.findProperty("newVersion") as String?
            ?: error("Please provide -PnewVersion=<version>. " +
                    "Check https://github.com/appspiriment/AndroidConventionPlugins/releases")

        val tomlFile = file("gradle/appspirimentlibs.versions.toml")
        if (!tomlFile.exists()) {
            error("gradle/appspirimentlibs.versions.toml not found.")
        }

        val original = tomlFile.readText()
        val versionLineRegex = Regex("""^appspiriment\s*=\s*"[^"]*"""", RegexOption.MULTILINE)

        if (!versionLineRegex.containsMatchIn(original)) {
            error("Could not find 'appspiriment = \"...\"' in appspirimentlibs.versions.toml")
        }

        val updated = original.replace(versionLineRegex, """appspiriment = "$newVersion"""")
        tomlFile.writeText(updated)

        logger.lifecycle("✅ Upgraded appspiriment to $newVersion in gradle/appspirimentlibs.versions.toml")
        logger.lifecycle("   Sync Gradle to apply the update.")
    }
}
