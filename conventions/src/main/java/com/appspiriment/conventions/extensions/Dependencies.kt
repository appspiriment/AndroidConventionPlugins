package com.appspiriment.conventions.extensions

import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME

val basePluginList get()= listOf(
    "kotlin-android",
)

val composePluginList get() = listOf(
    "kotlin-compose",
    "kotlinx-serialization",
)
val hiltPluginList get() = listOf(
    "dagger-hilt-android",
    "devtools-ksp",
    "kotlin-parcelize"
)

val hiltDependencies: List<Dependency> get()  =
    listOf(
        Dependency(
            type = ImplType.DEPENDENCY,
            config = IMPLEMENTATION_CONFIGURATION_NAME,
            aliases = listOf("hilt-android", "hilt-compose-navigation")
        ),
        Dependency(
            type = ImplType.DEPENDENCY,
            config = "ksp",
            aliases = listOf("hilt-compiler")
        ),
    )

val baseDependencies: List<Dependency> get() =
    listOf(
        Dependency(type = ImplType.BUNDLE, config = IMPLEMENTATION_CONFIGURATION_NAME, aliases = listOf("android-base")),
        Dependency(
            type = ImplType.BUNDLE,
            config = TEST_IMPLEMENTATION_CONFIGURATION_NAME,
            aliases = listOf("unit-test")
        ),
        Dependency(
            type = ImplType.BUNDLE,
            config = "androidTestImplementation",
            aliases = listOf("android-test")
        ),
    )

val utilDependencies: List<Dependency> get() = listOf(
    Dependency(
        type = ImplType.DEPENDENCY,
        config = "debugImplementation",
        aliases = listOf("appspiriment-utils-dev")
    ),
    Dependency(
        type = ImplType.DEPENDENCY,
        config = "releaseImplementation",
        aliases = listOf("appspiriment-utils-prod")
    ),
)


val composeDependencies: List<Dependency> get() =
    listOf(
        Dependency(
            type = ImplType.PLATFORM,
            config = IMPLEMENTATION_CONFIGURATION_NAME,
            aliases = listOf("androidx-compose-bom")
        ),
        Dependency(
            type = ImplType.BUNDLE,
            config = IMPLEMENTATION_CONFIGURATION_NAME,
            aliases = listOf("android-compose")
        ),
        Dependency(
            type = ImplType.DEPENDENCY,
            config = "debugImplementation",
            aliases = listOf("androidx-ui-tooling", "androidx-ui-test-manifest")
        ),
        Dependency(
            type = ImplType.PLATFORM,
            config = "androidTestImplementation",
            aliases = listOf("androidx-compose-bom")
        ),
        Dependency(
            type = ImplType.DEPENDENCY,
            config = "androidTestImplementation",
            aliases = listOf("androidx-ui-test-junit4")
        ),
        Dependency(
            type = ImplType.DEPENDENCY,
            config = IMPLEMENTATION_CONFIGURATION_NAME,
            aliases = listOf(
                "lottie-compose", "hilt-compose-navigation"
            )
        ),
    )