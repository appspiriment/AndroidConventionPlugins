package com.appspiriment.conventions.extensions

// ADD these critical imports for Kotlin 2.0+
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

// ────────────────────────────────────────────────────────────────────────────────
// Extension Functions (moved here for single-file convenience)
// ────────────────────────────────────────────────────────────────────────────────

internal fun Project.configureAndroidEarly(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = projectConfigs.compileSdk


        defaultConfig {
            minSdk = projectConfigs.minSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables.useSupportLibrary = true
        }

        if(commonExtension is ApplicationExtension){
            commonExtension.defaultConfig {
                targetSdk = projectConfigs.targetSdk
            }
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
    commonExtension.apply {
        if (this is ApplicationExtension) {
            defaultConfig {
                buildTypes.getByName("debug") {
                    if (addDevSuffixToDebug) {
                        versionNameSuffix = getVersionCodeSuffix()
                        applicationIdSuffix = ".dev"
                    }
                }
            }
        }
    }
    // Kotlin compiler options
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                // ──────────────────────────────────────────────────────────
                // ADD THESE TWO LINES HERE
                // ──────────────────────────────────────────────────────────
                "-Xannotation-default-target=param-property", // Silences the Hilt/UseCase warnings
                "-Xcontext-parameters" // Replaces the deprecated -Xcontext-receivers
            )
        }
    }

    // Kotlin compiler options (late is fine)
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-Xcontext-receivers"
            )
        }
    }
}


internal fun getVersionCodeSuffix(): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatterBuilder()
        .appendPattern(".dev.yyMMdd.HHmmss")
        .toFormatter(Locale.ENGLISH)
    return formatter.format(now)
}