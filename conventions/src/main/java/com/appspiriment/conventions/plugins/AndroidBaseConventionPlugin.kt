package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.appspiriment.conventions.extensions.APPSPIRIMENT_EXTENSION_NAME
import com.appspiriment.conventions.extensions.AppspirimentExtension
import com.appspiriment.conventions.extensions.applyPluginFromLibs
import com.appspiriment.conventions.extensions.appspirimentLibs
import com.appspiriment.conventions.extensions.baseDependencies
import com.appspiriment.conventions.extensions.basePluginList
import com.appspiriment.conventions.extensions.composeDependencies
import com.appspiriment.conventions.extensions.composePluginList
import com.appspiriment.conventions.extensions.configureAndroidEarly
import com.appspiriment.conventions.extensions.configureAndroidLate
import com.appspiriment.conventions.extensions.hiltDependencies
import com.appspiriment.conventions.extensions.hiltPluginList
import com.appspiriment.conventions.extensions.implementDependency
import com.appspiriment.conventions.extensions.utilDependencies
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

// ────────────────────────────────────────────────────────────────────────────────
// Abstract Base Convention Plugin
// ────────────────────────────────────────────────────────────────────────────────

abstract class AndroidBaseConventionPlugin : Plugin<Project> {

    abstract val Project.commonExtension: CommonExtension<*, *, *, *, *, *>

    override fun apply(target: Project) {
        with(target) {
            // 1. Create extension early so user can configure it immediately
            extensions.create(APPSPIRIMENT_EXTENSION_NAME, AppspirimentExtension::class.java)

            // 2. Apply mandatory plugins early (kotlin-android etc.)
            applyMandatoryPlugins()

            // 3. Set SDKs and other EARLY immutable properties
            configureAndroidEarly(commonExtension)

            // 4. Apply core dependencies (always needed)
            applyCoreDependencies()

            // 5. Defer everything that depends on user config or can be set late
            afterEvaluate {
                val config = extensions.getByType<AppspirimentExtension>()

                // Late Android configuration (build features, dev suffix, etc.)
                configureAndroidLate(
                    commonExtension = commonExtension,
                    addDevSuffixToDebug = config.addDevSuffixToDebug.orNull ?: false
                )

                // Optional common features
                if (config.enableUtils.orNull ?: true) {
                    applyUtilsDependencies()
                }

                // Finalize build types
                finalizeAndroidConfiguration(config.enableMinify.orNull ?: false)
            }
        }
    }

    private fun Project.applyMandatoryPlugins() {
        pluginManager.applyPluginFromLibs(appspirimentLibs to basePluginList)
    }

    private fun Project.applyCoreDependencies() {
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = baseDependencies)
        }
    }

    // ────────────────────────────────────────────────
    // Explicit setup helpers — called by concrete subclasses
    // ────────────────────────────────────────────────

    protected fun Project.setupHilt() {
        pluginManager.applyPluginFromLibs(appspirimentLibs to hiltPluginList)
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = hiltDependencies)
        }
    }

    protected fun Project.setupCompose() {
        pluginManager.applyPluginFromLibs(appspirimentLibs to composePluginList)
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = composeDependencies)
        }
    }

    private fun Project.applyUtilsDependencies() {
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = utilDependencies)
        }
    }

    private fun Project.finalizeAndroidConfiguration(enableMinify: Boolean) {
        commonExtension.buildTypes.getByName("release") {
            isMinifyEnabled = enableMinify
        }

        if (plugins.hasPlugin("com.google.devtools.ksp")) {
            extensions.configure<KspExtension> {
                // Add any global KSP args here if needed (e.g. for Room schema)
            }
        }
    }
}

