package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.appspiriment.conventions.extensions.AppspirimentExtension
import com.appspiriment.conventions.extensions.Dependency
import com.appspiriment.conventions.extensions.EXTENSION_NAME
import com.appspiriment.conventions.extensions.ImplType
import com.appspiriment.conventions.extensions.applyPluginFromLibs
import com.appspiriment.conventions.extensions.configureAndroid
import com.appspiriment.conventions.extensions.copyAppspirimentLibs
import com.appspiriment.conventions.extensions.getVersionCodes
import com.appspiriment.conventions.extensions.implementDependency
import com.appspiriment.conventions.extensions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME as TestImpl

abstract class AndroidConventionPlugin : Plugin<Project> {
    protected val versions = getVersionCodes()

    private val _requiredPluginList = listOf(
        "kotlin-android",
    )

    private val _requiredComposePluginList = listOf(
        "kotlin-compose",
        "kotlinx-serialization",
    )

    private val _requiredDependencies: List<Dependency> =
        listOf(
            Dependency(type = ImplType.BUNDLE, config = Impl, aliases = listOf("android-base")),
            Dependency(
                type = ImplType.BUNDLE,
                config = TestImpl,
                aliases = listOf("unit-test")
            ),
            Dependency(
                type = ImplType.BUNDLE,
                config = "androidTestImplementation",
                aliases = listOf("android-test")
            ),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "debugImplementation",
                aliases = listOf("androidx-ui-test-manifest")
            ),
        )

    private val _utilDependencies: List<Dependency> = listOf(
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


    private val _composeDependencies: List<Dependency> =
        listOf(
            Dependency(
                type = ImplType.PLATFORM,
                config = Impl,
                aliases = listOf("androidx-compose-bom")
            ),
            Dependency(type = ImplType.BUNDLE, config = Impl, aliases = listOf("android-compose")),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "debugImplementation",
                aliases = listOf("androidx-ui-tooling")
            ),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = Impl,
                aliases = listOf("androidx-ui-tooling-preview")
            ),
            Dependency(
                type = ImplType.PLATFORM,
                config = "androidTestImplementation",
                aliases = listOf("androidx-compose-bom")
            ),
            Dependency(
                type = ImplType.DEPENDENCY, config = Impl, aliases = listOf(
                    "lottie-compose", "hilt-compose-navigation"
                )
            ),
        )

    protected fun applyPlugin(
        target: Project,
        requireCompose: Boolean = false,
        requiredPluginList: List<String>? = null,
        requiredDependencies: List<Dependency>? = null,
    ) {
        with(target) {
            val config = extensions.create(EXTENSION_NAME, AppspirimentExtension::class.java)

            copyAppspirimentLibs()

            buildList {
                addAll(_requiredPluginList)
                requiredPluginList?.let{addAll(it)}
                if (requireCompose) { addAll(_requiredComposePluginList) }
            }.let {
                pluginManager.applyPluginFromLibs(requiredLibs to it)
            }

            configureAndroid(commonExtension = commonExtension, enableCompose = requireCompose, version = versions)

            dependencies {
                buildList {
                    addAll(_requiredDependencies)
                    requiredDependencies?.let { addAll(it) }
                    if (requireCompose) { addAll(_composeDependencies) }
                    if (config.enableUtils) { addAll(_utilDependencies) }
                }.let {
                    implementDependency(libs = requiredLibs, dependencyList = it)
                }
            }
        }
    }

    abstract val Project.commonExtension: CommonExtension<*,*,*,*,*,*>
}
