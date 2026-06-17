package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.appspiriment.conventions.extensions.APPSPIRIMENT_EXTENSION_NAME
import com.appspiriment.conventions.extensions.AppspirimentExtension
import com.appspiriment.conventions.extensions.applyPluginFromLibs
import com.appspiriment.conventions.extensions.appspirimentLibs
import com.appspiriment.conventions.extensions.baseDependencies
import com.appspiriment.conventions.extensions.basePluginList
import com.appspiriment.conventions.extensions.composeDependencies
import com.appspiriment.conventions.extensions.composePluginList
import com.appspiriment.conventions.extensions.composeUtilDependencies
import com.appspiriment.conventions.extensions.configureAndroidEarly
import com.appspiriment.conventions.extensions.configureAndroidLate
import com.appspiriment.conventions.extensions.hiltDependencies
import com.appspiriment.conventions.extensions.hiltPluginList
import com.appspiriment.conventions.extensions.implementDependency
import com.appspiriment.conventions.extensions.utilDependencies
import com.appspiriment.conventions.extensions.buildDateSuffix
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

// ────────────────────────────────────────────────────────────────────────────────
// Capability enum — tracks which optional features a plugin has activated.
// Subclasses call setupHilt() / setupCompose() before super.apply(), which
// registers the capability. afterEvaluate then reads the set to decide which
// optional util dependencies to add. This replaces the previous mutable boolean
// flag approach, making capability tracking explicit and side-effect-free.
// ────────────────────────────────────────────────────────────────────────────────
enum class PluginCapability { HILT, COMPOSE }

// ────────────────────────────────────────────────────────────────────────────────
// Abstract Base Convention Plugin
// ────────────────────────────────────────────────────────────────────────────────

abstract class AndroidBaseConventionPlugin : Plugin<Project> {

    abstract val Project.commonExtension: CommonExtension<*, *, *, *, *, *>

    /**
     * Capabilities registered by subclasses before calling [apply].
     * Using a mutable set here is safe because plugin instances are created
     * fresh per project by Gradle — there is no cross-project sharing.
     */
    private val capabilities = mutableSetOf<PluginCapability>()

    override fun apply(target: Project) {
        with(target) {
            // 1. Create extension early so user can configure it immediately
            extensions.create(APPSPIRIMENT_EXTENSION_NAME, AppspirimentExtension::class.java)

            // 2. Apply mandatory plugins early (kotlin-android etc.)
            applyMandatoryPlugins()

            // 3. Set SDKs and other EARLY immutable properties
            configureAndroidEarly(commonExtension)

            // 4. Configure debug suffixes via Variant API
            extensions.findByType<ApplicationAndroidComponentsExtension>()?.onVariants { variant ->
                if (variant.buildType == "debug") {
                    val config = extensions.getByType<AppspirimentExtension>()
                    if (config.addDevSuffixToDebug.getOrElse(true)) {
                        variant.applicationId.set(variant.applicationId.map { "$it.dev" })
                        variant.outputs.forEach { output ->
                            // Append the suffix to whatever version name is currently set
                            output.versionName.set(output.versionName.map { current ->
                                "$current${buildDateSuffix()}"
                            })
                        }
                    }
                }
            }

            // 5. Apply core dependencies (always needed)
            applyCoreDependencies()

            // 6. Defer everything that depends on user config or can be set late.
            // afterEvaluate is required here to read user-configured extension values
            // after the consuming build script has been evaluated.
            afterEvaluate {
                val config = extensions.getByType<AppspirimentExtension>()

                // Late Android configuration (build features, etc.)
                configureAndroidLate(
                    commonExtension = commonExtension,
                    addDevSuffixToDebug = config.addDevSuffixToDebug.orNull ?: true
                )

                // Optional utility dependencies
                if (config.enableUtils.orNull ?: true) {
                    applyUtilsDependencies()
                    // Add compose-specific utils only when Compose capability is active
                    if (PluginCapability.COMPOSE in capabilities) {
                        applyComposeUtilsDependencies()
                    }
                }

                // Scaffold editable theme resources (colours + dimens) for app modules
                // that use Compose. Only runs when the target files are absent.
                val shouldScaffold = config.scaffoldThemeResources.orNull ?: true
                if (shouldScaffold
                    && PluginCapability.COMPOSE in capabilities
                    && plugins.hasPlugin("com.android.application")
                ) {
                    registerScaffoldTask()
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
    // Capability setup helpers — called by concrete subclasses before super.apply()
    // ────────────────────────────────────────────────

    protected fun Project.setupHilt() {
        capabilities += PluginCapability.HILT
        pluginManager.applyPluginFromLibs(appspirimentLibs to hiltPluginList)
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = hiltDependencies)
        }
    }

    protected fun Project.setupCompose() {
        capabilities += PluginCapability.COMPOSE
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

    private fun Project.applyComposeUtilsDependencies() {
        dependencies {
            implementDependency(libs = appspirimentLibs, dependencyList = composeUtilDependencies)
        }
    }

    private fun Project.finalizeAndroidConfiguration(enableMinify: Boolean) {
        commonExtension.buildTypes.getByName("release") {
            isMinifyEnabled = enableMinify
        }

        if (plugins.hasPlugin("com.google.devtools.ksp")) {
            extensions.configure<KspExtension> {
                // Global KSP args can be added here if needed (e.g. Room schema export path)
            }
        }
    }

    // ────────────────────────────────────────────────
    // compose-utils theme scaffold
    // ────────────────────────────────────────────────

    /**
     * Registers the [ScaffoldComposeThemeTask] and wires it before `preBuild`.
     *
     * The task is idempotent — it only writes files that are absent. Gradle's
     * UP-TO-DATE check uses a lightweight marker file in `build/appspiriment/` so
     * the task is skipped entirely on subsequent builds. Running `./gradlew clean`
     * removes the marker but leaves the already-generated `src/main/res` files
     * untouched, so the task re-runs once and immediately becomes UP-TO-DATE again.
     *
     * The task can also be invoked manually at any time:
     * ```
     * ./gradlew scaffoldAppspirimentResources
     * ```
     */
    private fun Project.registerScaffoldTask() {
        val scaffoldTask = tasks.register<ScaffoldComposeThemeTask>(
            "scaffoldAppspirimentResources"
        ) {
            group = "appspiriment"
            description = "Generates editable compose-utils theme XML files " +
                "(colours + dimens) into src/main/res. " +
                "Existing files are never overwritten."

            moduleDir.set(projectDir.absolutePath)
            markerFile.set(
                layout.buildDirectory.file("appspiriment/theme-scaffolded.txt")
            )
        }

        // Wire before preBuild so the files exist before AAPT runs.
        tasks.named("preBuild").configure { dependsOn(scaffoldTask) }
    }
}
