import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Properties

plugins {
    alias(libs.plugins.gradle.plugin.publish)
    `kotlin-dsl`
    signing
    `version-catalog`
    `maven-publish`
}

// ────────────────────────────────────────────────
// 1. VERSIONING
// Version is read from pluginversion.properties at configuration time (read-only).
// The DEV counter is bumped by the dedicated `bumpDevVersion` task at execution time.
//
// Correct dev publish workflow:
//   Step 1: ./gradlew publishDev          — bumps the DEV counter in pluginversion.properties
//   Step 2: ./gradlew publishToMavenLocal — publishes with the new (bumped) version
//
// This two-step approach is necessary because `version` is captured at configuration time.
// Running both in a single invocation would publish the pre-bump version.
// ────────────────────────────────────────────────
val versionPropsFile = rootProject.file("pluginversion.properties")

fun readVersion(): String {
    val props = Properties().apply {
        if (versionPropsFile.exists()) versionPropsFile.inputStream().use { load(it) }
    }
    val major = props.getProperty("MAJOR", "0.0.14")
    val dev = props.getProperty("DEV", "1").toInt()
    return if (project.hasProperty("isRelease")) major
    else "$major.dev-${dev.toString().padStart(2, '0')}"
}

val currentVersion = readVersion()
group = "io.github.appspiriment"
version = currentVersion

// ────────────────────────────────────────────────
// 2. VERSION BUMP TASK (Execution Phase — safe)
// Increments DEV counter in pluginversion.properties.
//
// Only declares outputs (not inputs) so Gradle never considers it up-to-date
// and always runs it when requested. Declaring the same file as both input and
// output would cause Gradle to skip the task on the second run.
// ────────────────────────────────────────────────
val bumpDevVersion = tasks.register("bumpDevVersion") {
    group = "versioning"
    description = "Increments the DEV counter in pluginversion.properties."

    outputs.file(versionPropsFile)

    doLast {
        val props = Properties().apply {
            versionPropsFile.inputStream().use { load(it) }
        }
        val major = props.getProperty("MAJOR", "0.0.14")
        val dev = props.getProperty("DEV", "1").toInt() + 1
        props.setProperty("DEV", dev.toString())
        versionPropsFile.outputStream().use { props.store(it, null) }
        val newVersion = "$major.dev-${dev.toString().padStart(2, '0')}"
        logger.lifecycle("🚀 Version bumped to: $newVersion")
        logger.lifecycle("ℹ️  Run './gradlew publishToMavenLocal' to publish with the new version.")
    }
}

// ────────────────────────────────────────────────
// 3. CONSTANTS.KT GENERATOR TASK
// Reads appspirimentlibs.versions.toml, replaces LIBVERSION placeholder,
// and generates a Constants.kt file baked into the plugin JAR.
// This is how the plugin knows what TOML content to write into consumer projects.
// ────────────────────────────────────────────────
val generatedSourceDir = layout.buildDirectory.dir("generated/appspiriment/kotlin")

val updateLibFileVersion = tasks.register("updateLibFileVersion") {
    group = "versioning"
    description = "Generates Constants.kt with the baked-in TOML catalog and plugin version."

    val tomlFile = rootProject.file("gradle/appspirimentlibs.versions.toml")
    val constantsFile = generatedSourceDir.map {
        it.file("com/appspiriment/conventions/extensions/Constants.kt")
    }

    inputs.file(tomlFile)
    inputs.property("pluginVersion", currentVersion)
    outputs.dir(generatedSourceDir)

    doLast {
        if (!tomlFile.exists()) {
            logger.warn("⚠️ appspirimentlibs.versions.toml not found — skipping Constants.kt generation")
            return@doLast
        }

        val rawToml = tomlFile.readText()
        val processedToml = rawToml.replace("LIBVERSION", currentVersion)
        val tomlLines = processedToml.lines()

        var currentSection = ""
        val versionRefs = mutableListOf<String>()
        val pluginRefs = mutableListOf<String>()
        val libraryRefs = mutableListOf<String>()

        tomlLines.forEach { line: String ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[") && trimmed.endsWith("]") -> {
                    currentSection = trimmed
                }
                trimmed.isEmpty() || trimmed.startsWith("#") -> { /* skip */ }
                else -> when (currentSection) {
                    "[versions]" -> {
                        val key = trimmed.substringBefore("=").trim()
                        if (key.isNotEmpty()) versionRefs.add("\"$key\"")
                    }
                    "[plugins]" -> {
                        val idMatch = Regex("""id\s*=\s*"([^"]+)"""").find(trimmed)
                        val id = idMatch?.groupValues?.get(1) ?: trimmed.substringBefore("=").trim()
                        if (id.isNotEmpty() && !id.contains("{")) pluginRefs.add("\"$id\"")
                    }
                    "[libraries]" -> {
                        val g = Regex("""group\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
                        val n = Regex("""name\s*=\s*"([^"]+)"""").find(trimmed)?.groupValues?.get(1)
                        if (g != null && n != null) libraryRefs.add("Pair(\"$g\", \"$n\")")
                    }
                }
            }
        }

        // Escape the TOML content for embedding as a Kotlin string literal
        val escapedToml = processedToml
            .replace("\\", "\\\\")
            .replace("\$", "\${'$'}")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val file = constantsFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.appspiriment.conventions.extensions

            import com.appspiriment.conventions.extensions.AppspirimentLibRef

            internal const val appspirimentTomlName = "appspirimentlibs"
            internal const val libVersion = "$currentVersion"
            internal const val appspirimentTomlContents = "$escapedToml"

            internal val appspirimentLibRefs = AppspirimentLibRef(
                versions = listOf(${versionRefs.joinToString(", ")}),
                plugins = listOf(${pluginRefs.joinToString(", ")}),
                libraries = listOf(${libraryRefs.joinToString(", ")})
            )
            """.trimIndent()
        )

        logger.lifecycle("✅ Constants.kt generated with version: $currentVersion")
    }
}

// ────────────────────────────────────────────────
// 4. THEME TEMPLATE SYNC
// At plugin-build time, reads the three resource XMLs (colours day/night + dimens)
// directly from the sibling compose-utils source tree and packs them into the plugin
// JAR as classpath resources under appspiriment/templates/.
//
// ScaffoldComposeThemeTask (run in consumer apps) extracts these via
// ClassLoader.getResourceAsStream() and writes them to the app's src/main/res
// so they can be edited to customise the theme.
//
// Falls back to the committed copies in src/theme-templates/ when compose-utils
// is not checked out alongside this project (e.g. isolated plugin CI build).
// ────────────────────────────────────────────────
val composeUtilsResDir = rootProject.file("../AppUtilLibs/compose-utils/src/main/res")
val generatedResourcesDir = layout.buildDirectory.dir("generated-resources")

val generateThemeTemplates = tasks.register<Sync>("generateThemeTemplates") {
    group = "appspiriment"
    description = "Packs compose-utils theme templates into the plugin JAR. " +
        "Source: sibling compose-utils when available, committed defaults otherwise."

    if (composeUtilsResDir.isDirectory) {
        from(composeUtilsResDir) {
            include("values/colors.xml")
            include("values-night/colors.xml")
            include("values/dimens.xml")
            rename("colors.xml", "appspiriment_colors.xml")
            rename("dimens.xml", "appspiriment_dimens.xml")
            into("appspiriment/templates")  // classpath prefix inside the JAR
        }
        doLast {
            logger.lifecycle("✅ Theme templates synced from compose-utils source.")
        }
    } else {
        // Sibling project not checked out — use the last-committed static copies.
        from(layout.projectDirectory.dir("src/theme-templates"))
        doLast {
            logger.lifecycle(
                "ℹ️  compose-utils source not found at ${composeUtilsResDir.path}; " +
                    "using committed theme-template defaults."
            )
        }
    }

    into(generatedResourcesDir)
}

// ────────────────────────────────────────────────
// 5. PLUGIN DEFINITIONS
// Note: AndroidProjectConventionPlugin has been removed.
// Project bootstrapping is now handled by the Android Studio project template
// (see project-template/ directory). This eliminates all configuration-phase
// file I/O and makes the plugin set fully configuration-cache compatible.
// ────────────────────────────────────────────────
gradlePlugin {
    plugins {
        create("androidApplication") {
            id = "io.github.appspiriment.application"
            displayName = "Appspiriment Application"
            description = "Standardized setup for Android application modules (Compose + Hilt by default)."
            implementationClass = "com.appspiriment.conventions.plugins.AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "io.github.appspiriment.library"
            displayName = "Appspiriment Library"
            description = "Minimal Android library module setup."
            implementationClass = "com.appspiriment.conventions.plugins.AndroidLibraryConventionPlugin"
        }
        create("androidHiltLibrary") {
            id = "io.github.appspiriment.library-hilt"
            displayName = "Appspiriment Library (Hilt)"
            description = "Android library module with Hilt dependency injection."
            implementationClass = "com.appspiriment.conventions.plugins.AndroidLibraryHiltConventionPlugin"
        }
        create("androidComposeLibrary") {
            id = "io.github.appspiriment.library-compose"
            displayName = "Appspiriment Library (Compose)"
            description = "Android library module with Jetpack Compose UI."
            implementationClass = "com.appspiriment.conventions.plugins.AndroidLibraryComposeConventionPlugin"
        }
        create("androidHiltComposeLibrary") {
            id = "io.github.appspiriment.library-hilt-compose"
            displayName = "Appspiriment Library (Hilt + Compose)"
            description = "Android library module with both Hilt and Jetpack Compose."
            implementationClass = "com.appspiriment.conventions.plugins.AndroidLibraryHiltComposeConventionPlugin"
        }
        create("androidDataLayerLibrary") {
            id = "io.github.appspiriment.data"
            displayName = "Appspiriment Data Layer"
            description = "Data layer setup with opt-in Room, Retrofit, DataStore, Security, and WorkManager."
            implementationClass = "com.appspiriment.conventions.plugins.feature.AndroidDataLayerConventionPlugin"
        }
    }
}

// ────────────────────────────────────────────────
// 6. KOTLIN & COMPILATION
// ────────────────────────────────────────────────
kotlin {
    sourceSets.main {
        kotlin.srcDir(updateLibFileVersion)
        // Adds the theme-template XMLs as classpath resources in the plugin JAR.
        // The lazy map ensures processResources depends on generateThemeTemplates.
        resources.srcDir(generateThemeTemplates.map { it.destinationDir })
    }
}

val javaVersion = libs.versions.javaVersion.get().toInt()
java { toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) } }

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll("-Xcontext-parameters", "-opt-in=kotlin.RequiresOptIn")
    }
}

// ────────────────────────────────────────────────
// 7. DEPENDENCIES
// ────────────────────────────────────────────────
dependencies {
    compileOnly(gradleApi())
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    testImplementation(gradleTestKit())
    testImplementation(libs.junit.test)
}

// ────────────────────────────────────────────────
// 8. PUBLISHING
// ────────────────────────────────────────────────
publishing {
    publications.withType<MavenPublication> {
        version = currentVersion
    }
}

tasks.withType<Jar>().matching { it.name.contains("sourcesJar", ignoreCase = true) }.configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(updateLibFileVersion)
}

/**
 * Step 1 of the dev publish workflow: bumps the DEV counter in pluginversion.properties.
 *
 * After running this task, run `./gradlew publishToMavenLocal` (Step 2) to publish
 * the artifact with the newly bumped version. The two-step split is required because
 * `version` is captured at Gradle configuration time — a single invocation cannot
 * both bump and publish the new version in the same run.
 *
 * Usage:
 *   ./gradlew publishDev          # Step 1: bump version
 *   ./gradlew publishToMavenLocal # Step 2: publish with new version
 */
tasks.register("publishDev") {
    group = "publishing"
    description = "Step 1: Bumps the DEV version counter. Then run publishToMavenLocal to publish."
    dependsOn(bumpDevVersion)
    doLast {
        logger.lifecycle("✅ Version bumped. Now run: ./gradlew publishToMavenLocal")
    }
}

// ────────────────────────────────────────────────
// 9. SIGNING (release builds only)
// ────────────────────────────────────────────────
signing {
    val isRelease = project.hasProperty("isRelease")
    if (isRelease) {
        useGpgCmd()
        sign(publishing.publications)
    }
    isRequired = isRelease
}
