package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.appspiriment.conventions.extensions.AppspirimentExtension
import com.appspiriment.conventions.extensions.Dependency
import com.appspiriment.conventions.extensions.EXTENSION_NAME
import com.appspiriment.conventions.extensions.applyPluginFromLibs
import com.appspiriment.conventions.extensions.baseDependencies
import com.appspiriment.conventions.extensions.basePluginList
import com.appspiriment.conventions.extensions.composeDependencies
import com.appspiriment.conventions.extensions.composePluginList
import com.appspiriment.conventions.extensions.configureAndroid
import com.appspiriment.conventions.extensions.copyAppspirimentLibs
import com.appspiriment.conventions.extensions.getVersionCodes
import com.appspiriment.conventions.extensions.hiltDependencies
import com.appspiriment.conventions.extensions.hiltPluginList
import com.appspiriment.conventions.extensions.implementDependency
import com.appspiriment.conventions.extensions.requiredLibs
import com.appspiriment.conventions.extensions.utilDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

abstract class AndroidConventionPlugin : Plugin<Project> {
    private val versions = getVersionCodes()
    abstract val Project.commonExtension: CommonExtension<*, *, *, *, *, *>

    protected fun applyPlugin(
        target: Project,
        requireHilt: Boolean = false,
        requireCompose: Boolean = true,
        requiredPluginList: List<String>? = null,
        requiredDependencies: List<Dependency>? = null,
    ) {
        with(target) {
            val config = extensions.create(EXTENSION_NAME, AppspirimentExtension::class.java)

            copyAppspirimentLibs()

            buildList {
                addAll(basePluginList)
                requiredPluginList?.let { addAll(it) }
                if (requireHilt) addAll(hiltPluginList)
                if (requireCompose) addAll(composePluginList)
            }.let {
                pluginManager.applyPluginFromLibs(requiredLibs to it)
            }

            configureAndroid(
                commonExtension = commonExtension,
                enableCompose = requireCompose,
                version = versions
            )

            dependencies {
                implementDependency(libs = requiredLibs, dependencyList = baseDependencies)
                requiredDependencies?.let {
                    implementDependency(libs = requiredLibs, dependencyList = it)
                }
                if (requireHilt) {
                    implementDependency(libs = requiredLibs, dependencyList = hiltDependencies)
                }
                if (requireCompose) {
                    implementDependency(libs = requiredLibs, dependencyList = composeDependencies)
                }
            }

            afterEvaluate {
                dependencies {
                    if (config.enableUtils) {
                        implementDependency(libs = requiredLibs, dependencyList = utilDependencies)
                    }
                }
                commonExtension.apply {
                    defaultConfig.apply {
                        buildTypes {
                            getByName("release") {
                                isMinifyEnabled = config.enableMinify
                            }
                        }
                    }
                }
            }
        }
    }
}
