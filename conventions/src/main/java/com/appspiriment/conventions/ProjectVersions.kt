
package com.appspiriment.conventions

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import kotlin.jvm.optionals.getOrNull

var LibVersions : ProjectVersions? = null

val Project.libVersions : ProjectVersions get() = LibVersions ?: configs.also { LibVersions = it }

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.configs : ProjectVersions get() = this.libs.run {
    ProjectVersions(
//        agpVersion = libs.getVersion("agp"),
//        hiltVersion = libs.getVersion("hilt"),
//        kspVersion = libs.getVersion("ksp"),
        minSdk = getVersion("minSdk").toInt(),
        targetSdk = getVersion("targetSdk").toInt(),
        compileSdk = getVersion("compileSdk").toInt(),
        javaVersion = getVersion("javaVersion").toInt().let{
            JavaVersion.values()[it]
        },
    )
}

internal fun VersionCatalog.getVersion(key: String): String{
    return findVersion(key).getOrNull()?.toString() ?: throw Exception("$key version not found - add version under 'key' in libs")
}

data class ProjectVersions(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
//    val agpVersion: String,
//    val hiltVersion: String,
//    val kspVersion: String,
    val javaVersion: JavaVersion
)