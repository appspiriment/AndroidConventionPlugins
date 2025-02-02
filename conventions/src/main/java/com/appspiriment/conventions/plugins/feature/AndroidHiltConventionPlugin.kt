package com.appspiriment.conventions.plugins.feature

import com.appspiriment.conventions.extensions.Dependency
import com.appspiriment.conventions.extensions.ImplType
import com.appspiriment.conventions.extensions.applyPluginFromLibs
import com.appspiriment.conventions.extensions.getVersionCodes
import com.appspiriment.conventions.extensions.implementDependency
import com.appspiriment.conventions.extensions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl

abstract class AndroidHiltConventionPlugin : Plugin<Project> {
    protected val versions = getVersionCodes()

    private val _requiredPluginList = listOf(
        "dagger-hilt-android",
        "devtools-ksp",
        "kotlin-parcelize"
    )

    private val _requiredDependencies: List<Dependency> =
        listOf(
            Dependency(type = ImplType.DEPENDENCY, config = Impl, aliases = listOf("hilt-android")),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "ksp",
                aliases = listOf("hilt-compiler")
            ),
        )


    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPluginFromLibs(requiredLibs to _requiredPluginList)

            dependencies {
                implementDependency(libs = requiredLibs, dependencyList = _requiredDependencies)
            }
        }
    }
}