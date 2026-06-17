package com.appspiriment.conventions.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ────────────────────────────────────────────────────────────────────────────────
// Extension Functions
// ────────────────────────────────────────────────────────────────────────────────

internal fun Project.configureAndroidEarly(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = projectConfigs.compileSdk

        defaultConfig {
            minSdk = projectConfigs.minSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables.useSupportLibrary = true
        }

        if (this is ApplicationExtension) {
            defaultConfig.targetSdk = projectConfigs.targetSdk
        }

        compileOptions {
            sourceCompatibility = projectConfigs.javaVersion
            targetCompatibility = projectConfigs.javaVersion
        }
    }
}


internal fun Project.configureAndroidLate(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    addDevSuffixToDebug: Boolean
) {
    // Kotlin compiler options
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-Xannotation-default-target=param-property",
                "-Xcontext-parameters"
            )
        }
    }
}


/**
 * Generates a version name suffix in the format `.yyyyMMdd.HHmmss`.
 */
internal fun buildDateSuffix(): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss", Locale.ENGLISH)
    return ".${formatter.format(now)}"
}
