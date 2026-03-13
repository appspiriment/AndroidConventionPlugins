package com.appspiriment.conventions.extensions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.appspirimentLibs
    get() = extensions.getByType<VersionCatalogsExtension>().named(appspirimentTomlName)

val Project.projectConfigs: ProjectConfiguration
    get() = appspirimentLibs.run {
        ProjectConfiguration(
            minSdk = getVersion("minSdk").toInt(),
            targetSdk = getVersion("targetSdk").toInt(),
            compileSdk = getVersion("compileSdk").toInt(),
            javaVersion = JavaVersion.toVersion(getVersion("javaVersion").toInt())
        )
    }

data class ProjectConfiguration(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val javaVersion: JavaVersion
)