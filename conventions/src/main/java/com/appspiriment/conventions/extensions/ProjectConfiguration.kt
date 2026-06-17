package com.appspiriment.conventions.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.appspirimentLibs
    get() = extensions.getByType<VersionCatalogsExtension>().named(appspirimentTomlName)

/**
 * Returns the [ProjectConfiguration] for this project, computing it once and caching
 * the result in the project's extra properties. This avoids repeated catalog lookups
 * on every access — [appspirimentLibs] itself calls [VersionCatalogsExtension.named]
 * each time, so caching here is meaningful for plugins that read configs frequently.
 */
val Project.projectConfigs: ProjectConfiguration
    get() {
        val key = "appspiriment.projectConfigs"
        return if (extensions.extraProperties.has(key)) {
            @Suppress("UNCHECKED_CAST")
            extensions.extraProperties.get(key) as ProjectConfiguration
        } else {
            val config = appspirimentLibs.run {
                ProjectConfiguration(
                    minSdk = getVersion("minSdk").toInt(),
                    targetSdk = getVersion("targetSdk").toInt(),
                    compileSdk = getVersion("compileSdk").toInt(),
                    javaVersion = JavaVersion.toVersion(getVersion("javaVersion").toInt())
                )
            }
            extensions.extraProperties.set(key, config)
            config
        }
    }

data class ProjectConfiguration(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val javaVersion: JavaVersion
)
