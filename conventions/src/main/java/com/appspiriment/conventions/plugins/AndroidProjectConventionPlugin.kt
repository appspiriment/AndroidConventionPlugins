package com.appspiriment.conventions.plugins

import com.appspiriment.conventions.extensions.appspirimentTomlContents
import com.appspiriment.conventions.extensions.appspirimentTomlName
import com.appspiriment.conventions.extensions.getDefaultAppGradle
import com.appspiriment.conventions.extensions.libVersion
import com.appspiriment.conventions.utils.documentation.ensureProjectGuide
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class AndroidProjectConventionPlugin : Plugin<Project> {

    private val includePluginInClassPaths = listOf(
        "google.android.application",
        "google.android.library",
        "kotlin.android",
        "kotlin.compose",
        "kotlin.jvm",
        "devtools.ksp",
        "dagger.hilt.android",
        "kotlinx.serialization",
        // Adding the internal convention plugins here:
        "appspiriment.application",
        "appspiriment.library",
        "appspiriment.library.hilt",
        "appspiriment.library.compose",
        "appspiriment.library.hilt.compose",
        "appspiriment.data"
    )

    override fun apply(target: Project) {
        val rootDir = target.rootDir
        val gradleDir = File(rootDir, "gradle")

        // 1. Ensure the version catalog TOML file exists and is up-to-date
        val tomlFile = File(gradleDir, "$appspirimentTomlName.versions.toml")
        ensureTomlFileUpToDate(tomlFile)

        // 2. Ensure the catalog is declared in settings.gradle.kts
        val settingsFile = File(rootDir, "settings.gradle.kts")
        ensureCatalogDeclaredInSettings(settingsFile)

        // 3. Update gradle.properties (enable KSP2 if missing)
        updateGradleProperties(rootDir)

        // 4. Update root build.gradle.kts with plugin aliases and nice header
        updateRootBuildScript(target)

        // 5. If there's an :app module, apply default convention plugin if missing
        val appBuildFile = File(rootDir, "app/build.gradle.kts")
        if (appBuildFile.exists() && !appBuildFile.readText().contains("plugins.appspiriment.application")) {
            appBuildFile.writeText(getDefaultAppGradle())
        }
        // 6. Project Plugin Documentation

        target.ensureProjectGuide()
    }

    // Your simplified Plugin code:
    private fun ensureTomlFileUpToDate(tomlFile: File) {
        val parentDir = tomlFile.parentFile
        if (!parentDir.exists()) parentDir.mkdirs()

        // No replacement needed here anymore!
        val expectedContent = appspirimentTomlContents.trim()

        if (!tomlFile.exists() || tomlFile.readText().trim() != expectedContent) {
            tomlFile.writeText(expectedContent + "\n")
        }
    }

    private fun ensureCatalogDeclaredInSettings(settingsFile: File) {
        if (!settingsFile.exists()) return

        var content = settingsFile.readText()

        if (content.contains("$appspirimentTomlName.versions.toml")) return

        val insertion = """
            |    versionCatalogs {
            |        create("$appspirimentTomlName") {
            |            from(files("gradle/$appspirimentTomlName.versions.toml"))
            |        }
            |    }
        """.trimMargin()

        if (content.contains("dependencyResolutionManagement {")) {
            content = content.replace(
                "dependencyResolutionManagement {",
                "dependencyResolutionManagement {\n$insertion"
            )
        } else {
            content += "\n\ndependencyResolutionManagement {\n$insertion\n}\n"
        }

        settingsFile.writeText(content)
    }

    private fun updateGradleProperties(rootDir: File) {
        val propsFile = File(rootDir, "gradle.properties")
        val kspLine = "ksp.useKSP2=true"

        if (!propsFile.exists()) {
            propsFile.writeText("$kspLine\n")
            return
        }

        val lines = propsFile.readLines()
        if (lines.any { it.trim() == kspLine }) return

        propsFile.appendText("\n$kspLine\n")
    }

    private fun updateRootBuildScript(target: Project) {
        val buildFile = target.rootProject.buildFile
        if (!buildFile.exists()) return

        val originalContent = buildFile.readText()

        // 1. Generate the Header (Instructions only)
        val header = """
            // ╔════════════════════════════════════════════════════════════╗
            // ║              Appspiriment Convention Plugins               ║
            // ║                                                            ║
            // ║  Current version: $libVersion                              ║
            // ║                                                            ║
            // ║  To check for updates or upgrade:                          ║
            // ║  1. Visit: https://github.com/appspiriment/AndroidConventionPlugins/releases
            // ║  2. Copy the latest version number (e.g. "0.1.0")          ║
            // ║  3. Update/Uncomment the line inside plugins { } block:    ║
            // ║ id("io.github.appspiriment.project") version "$libVersion" ║
            // ║                                                            ║
            // ║  Or run: ./gradlew dependencyUpdates                       ║
            // ║     (after adding com.github.ben-manes.versions plugin)    ║
            // ║                                                            ║
            // ║  Full changelog & documentation:                           ║
            // ║  https://github.com/appspiriment/AndroidConventionPlugins  ║
            // ╚════════════════════════════════════════════════════════════╝
        """.trimIndent()

        // 2. Remove old headers and existing Appspiriment plugin references to prevent clutter
        val mainPluginId = "io.github.appspiriment.project"
        var content = originalContent.replace(Regex("// ╔[\\s\\S]*?╝\\n?", RegexOption.MULTILINE), "")

        // Remove existing project plugin lines (commented or not) to update the version number
        content = content.replace(Regex(".*$mainPluginId.*\\n?"), "")

        // 3. Ensure plugins block exists
        if (!content.contains("plugins {")) {
            content = "plugins {\n}\n$content"
        }

        // 4. Build the new injection block
        val versionLockLine = "    // id(\"$mainPluginId\") version \"$libVersion\""

        val pluginAliases = includePluginInClassPaths
            .filter { !content.contains("alias($appspirimentTomlName.plugins.$it)") }
            .joinToString("\n") { "    alias($appspirimentTomlName.plugins.$it) apply false" }

        // Inject the version lock and aliases at the top of the plugins block
        val replacement = StringBuilder().apply {
            append("plugins {\n")
            append("$versionLockLine\n")
            if (pluginAliases.isNotBlank()) {
                append("$pluginAliases\n")
            }
        }.toString()

        val finalContent = content.replace("plugins {", replacement)

        // 5. Final Write
        buildFile.writeText("$header\n\n${finalContent.trim()}\n")
    }
}