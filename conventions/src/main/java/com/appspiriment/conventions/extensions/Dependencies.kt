package com.appspiriment.conventions.extensions

import org.gradle.api.artifacts.dsl.DependencyHandler

const val IMPLEMENTATION_CONFIGURATION_NAME = "implementation"
const val TEST_IMPLEMENTATION_CONFIGURATION_NAME = "testImplementation"

val basePluginList: List<String> = listOf(
    "org.jetbrains.kotlin.android",
)

val composePluginList: List<String> = listOf(
    "org.jetbrains.kotlin.plugin.compose",
    "org.jetbrains.kotlin.plugin.serialization"
)

val hiltPluginList: List<String> = listOf(
    "com.google.dagger.hilt.android",
    "com.google.devtools.ksp",
    "org.jetbrains.kotlin.plugin.serialization"
)

/**
 * Hilt core dependencies.
 */
val hiltDependencies: List<Dependency> = listOf(
    Dependency(
        notation = "com.google.dagger:hilt-android",
        versionRef = "hilt"
    ),
    Dependency(
        notation = "org.jetbrains.kotlinx:kotlinx-serialization-json",
        versionRef = "kotlinserialize"
    ),
    Dependency(
        config = "ksp",
        notation = "com.google.dagger:hilt-android-compiler",
        versionRef = "hilt"
    )
)

/**
 * Compose dependencies.
 */
val composeDependencies: List<Dependency> = listOf(
    Dependency(
        type = ImplType.PLATFORM,
        notation = "androidx.compose:compose-bom",
        versionRef = "composeBom"
    ),
    Dependency(
        type = ImplType.BUNDLE,
        notation = "android-compose"
    ),
    Dependency(
        config = "debugImplementation",
        notation = "androidx.compose.ui:ui-tooling",
        versionRef = "material-icons" // Uses material-icons version ref for UI items
    ),
    Dependency(
        config = "debugImplementation",
        notation = "androidx.compose.ui:ui-test-manifest",
        versionRef = "material-icons"
    ),
    Dependency(
        type = ImplType.PLATFORM,
        config = "androidTestImplementation",
        notation = "androidx.compose:compose-bom",
        versionRef = "composeBom"
    ),
    Dependency(
        config = "androidTestImplementation",
        notation = "androidx.compose.ui:ui-test-junit4",
        versionRef = "material-icons"
    ),
    Dependency(
        notation = "androidx.hilt:hilt-navigation-compose",
        versionRef = "composeHiltNavigation"
    ),
)

/**
 * Base dependencies added to every module.
 */
val baseDependencies: List<Dependency> = listOf(
    Dependency(
        type = ImplType.BUNDLE,
        notation = "android-base"
    ),
    Dependency(
        type = ImplType.BUNDLE,
        config = TEST_IMPLEMENTATION_CONFIGURATION_NAME,
        notation = "unit-test"
    )
)

/**
 * Optional Appspiriment utility dependencies.
 */
val utilDependencies: List<Dependency> = listOf(
    Dependency(
        notation = "io.github.appspiriment:utils",
        versionRef = "appspirimentUtils"
    ),
    Dependency(
        config = "debugImplementation",
        notation = "io.github.appspiriment:logutils-dev",
        versionRef = "appspirimentLogUtils"
    ),
    Dependency(
        config = "releaseImplementation",
        notation = "io.github.appspiriment:logutils-prod",
        versionRef = "appspirimentLogUtils"
    )
)

/**
 * Optional Compose utility dependencies.
 */
val composeUtilDependencies: List<Dependency> = listOf(
    Dependency(
        notation = "io.github.appspiriment:compose-utils",
        versionRef = "appspirimentComposeUtils"
    ),
    Dependency(
        notation = "com.airbnb.android:lottie-compose",
        versionRef = "lottie"
    )
)
