
package com.appspiriment.conventions
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import kotlin.jvm.optionals.getOrNull

private var projectConfiguration : ProjectConfiguration? = null

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.requiredLibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("appspirimentlibs")

fun Project.androidApp(block: ApplicationExtension.() -> Unit){
    extensions.configure<ApplicationExtension>{
        block()
    }
}

fun Project.androidLibrary(block: LibraryExtension.() -> Unit){
    extensions.configure<LibraryExtension>{
        block()
    }
}

val Project.projectConfigs : ProjectConfiguration get() = projectConfiguration ?: this.requiredLibs.run {
    ProjectConfiguration(
        minSdk = getVersion("minSdk").toInt(),
        targetSdk = getVersion("targetSdk").toInt(),
        compileSdk = getVersion("compileSdk").toInt(),
        javaVersion = getVersion("javaVersion").toInt().let{
            JavaVersion.values()[it]
        },
    )
}.also { projectConfiguration = it }

data class ProjectConfiguration(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdk: Int,
    val javaVersion: JavaVersion
)