import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
    `kotlin-dsl`
    signing
    `version-catalog`
    `maven-publish`
}

// ────────────────────────────────────────────────
// 1. VERSIONING LOGIC (Configuration Phase Bump)
// ────────────────────────────────────────────────
val versionPropsFile = File(rootDir, "pluginversion.properties")

fun getAndMaybeBumpVersion(): String {
    val props = Properties().apply {
        if (versionPropsFile.exists()) versionPropsFile.inputStream().use { load(it) }
    }
    val major = props.getProperty("MAJOR", "0.0.14")
    var dev = props.getProperty("DEV", "1").toInt()

    // BUMP LOGIC: This happens during the Configuration phase to align all tasks
    val isPublishTask = gradle.startParameter.taskNames.any { it.contains("publishDev") }
    if (isPublishTask) {
        dev++
        props.setProperty("DEV", dev.toString())
        versionPropsFile.outputStream().use { props.store(it, null) }
        println(
            "🚀 [Configuration] Version bumped to: $major.dev-${
                dev.toString().padStart(2, '0')
            }"
        )
    }

    return if (!project.hasProperty("isRelease")) "$major.dev-${
        dev.toString().padStart(2, '0')
    }" else major
}

val calculatedVersion = getAndMaybeBumpVersion()
group = "io.github.appspiriment"
version = calculatedVersion

// ────────────────────────────────────────────────
// 2. GENERATOR TASK (Constants.kt with Baked-In Version)
// ────────────────────────────────────────────────
val generatedSourceDir = layout.buildDirectory.dir("generated/appspiriment/kotlin")


val updateLibFileVersion = tasks.register("updateLibFileVersion") {
    group = "versioning"
    val tomlFile = File(rootDir, "gradle/appspirimentlibs.versions.toml")
    val constantsFile = generatedSourceDir.map { it.file("com/appspiriment/conventions/extensions/Constants.kt") }

    inputs.file(tomlFile)
    inputs.property("pluginVersion", calculatedVersion)
    outputs.dir(generatedSourceDir)

    doLast {
        if (!tomlFile.exists()) return@doLast

        // 1. Read the TOML as a String and perform the replacement
        val rawToml = tomlFile.readText()
        val processedToml = rawToml.replace("LIBVERSION", calculatedVersion)

        // 2. Parse the lines from the processed STRING
        // FIX: Use .lines() for String, not .readLines() (which is for File)
        val tomlLines = processedToml.lines()

        var currentSection = ""
        val versionRefs = mutableListOf<String>()
        val pluginRefs = mutableListOf<String>()
        val libraryRefs = mutableListOf<String>()

        // FIX: Added explicit type (String) to avoid ambiguity
        tomlLines.forEach { line: String ->
            val trimmed = line.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed
                return@forEach
            }
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach

            when (currentSection) {
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

        // 3. Escape for Kotlin Source
        val escapedToml = processedToml
            .replace("\\", "\\\\")
            .replace("$", "\${'$'}")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val file = constantsFile.get().asFile
        file.parentFile.mkdirs()

        file.writeText("""
            package com.appspiriment.conventions.extensions
            import com.appspiriment.conventions.extensions.AppspirimentLibRef

            internal const val appspirimentTomlName = "appspirimentlibs"
            internal const val libVersion = "$calculatedVersion"
            internal const val appspirimentTomlContents = "$escapedToml"

            internal fun getDefaultAppGradle() = ""${'"'}
                plugins { alias(appspirimentlibs.plugins.appspiriment.application) }
                android {
                    namespace = "com.example.app"
                    defaultConfig { applicationId = "com.example.app" }
                }
            ""${'"'}"

            internal val appspirimentLibRefs = AppspirimentLibRef(
                versions = listOf(${versionRefs.joinToString(", ")}),
                plugins = listOf(${pluginRefs.joinToString(", ")}),
                libraries = listOf(${libraryRefs.joinToString(", ")})
            )
        """.trimIndent())

        logger.lifecycle("✅ Constants.kt generated with baked-in version: $calculatedVersion")
    }
}

// ────────────────────────────────────────────────
// 3. PLUGIN DEFINITIONS
// ────────────────────────────────────────────────
gradlePlugin {
    plugins {
        create("androidProject") {
            id = "io.github.appspiriment.project"
            displayName = "Appspiriment Project Root"
            description =
                "Initializes project, writes Version Catalog, and sets root configurations."
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidProjectConventionPlugin"
        }
        create("androidApplication") {
            id = "io.github.appspiriment.application"
            displayName = "Appspiriment Application"
            description =
                "Standardized setup for Android Apps (Compose, Hilt, Room, and Versioning)."
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "io.github.appspiriment.library"
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidLibraryConventionPlugin"
        }
        create("androidHiltLibrary") {
            id = "io.github.appspiriment.library-hilt"
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidLibraryHiltConventionPlugin"
        }
        create("androidComposeLibrary") {
            id = "io.github.appspiriment.library-compose"
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidLibraryComposeConventionPlugin"
        }
        create("androidHiltComposeLibrary") {
            id = "io.github.appspiriment.library-hilt-compose"
            implementationClass =
                "com.appspiriment.conventions.plugins.AndroidLibraryHiltComposeConventionPlugin"
        }
        create("androidDataLayerLibrary") {
            id = "io.github.appspiriment.data"
            implementationClass =
                "com.appspiriment.conventions.plugins.feature.AndroidDataLayerConventionPlugin"
        }
    }
}

// ────────────────────────────────────────────────
// 4. KOTLIN & COMPILATION
// ────────────────────────────────────────────────
kotlin {
    sourceSets.main {
        kotlin.srcDir(updateLibFileVersion)
    }
}

val javaVersion = libs.versions.javaVersion.get().toInt()
java { toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) } }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll("-Xcontext-receivers", "-opt-in=kotlin.RequiresOptIn")
    }
}

// ────────────────────────────────────────────────
// 5. DEPENDENCIES & PUBLISHING
// ────────────────────────────────────────────────
dependencies {
    compileOnly(gradleApi())
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
}

publishing {
    publications.withType<MavenPublication> {
        version = calculatedVersion
    }
}

// ────────────────────────────────────────────────
// 6. ORCHESTRATION & SIGNING
// ────────────────────────────────────────────────
tasks.withType<Jar>().matching { it.name.contains("sourcesJar", ignoreCase = true) }.configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(updateLibFileVersion)
}

tasks.register("publishDev") {
    group = "publishing"
    dependsOn("publishToMavenLocal")
    doLast {
        logger.lifecycle("🏆 Published version: $calculatedVersion to MavenLocal")
    }
}

signing {
    val isRelease = gradle.startParameter.taskNames.any { it.contains("Release") }
    if (isRelease) {
        useGpgCmd(); sign(publishing.publications)
    }
    isRequired = isRelease
}