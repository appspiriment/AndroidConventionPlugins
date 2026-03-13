package com.appspiriment.conventions.plugins.feature

import com.android.build.gradle.LibraryExtension
import com.appspiriment.conventions.extensions.*
import com.appspiriment.conventions.plugins.AndroidLibraryHiltConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * World-class Data Layer Convention Plugin.
 */
class AndroidDataLayerConventionPlugin : AndroidLibraryHiltConventionPlugin() {

    override fun apply(target: Project) {
        with(target) {
            // 1. Create the configuration extension
            // DATA_LAYER_EXTENSION_NAME should be "dataLayer"
            val dataConfig = extensions.create<DataLayerExtension>(DATA_LAYER_EXTENSION_NAME)

            // 2. Apply base shared logic (Kotlin, SDKs, Hilt)
            super.apply(this)

            // 3. Architecturally enforce: No Compose in Data Layer modules
            configure<LibraryExtension> {
                buildFeatures { compose = false }
            }

            // 4. Configure dependencies based on the extension
            // We use 'dependencies' block directly.
            // Note: If you use regular 'val', you might need 'afterEvaluate'.
            // If you use 'Property', you can use 'dataConfig.room.enabled.get()'

            afterEvaluate {
                dependencies {
                    val libs = appspirimentLibs

                    // --- PERSISTENCE (ROOM) ---
                    // --- PERSISTENCE (ROOM) ---
                    if (dataConfig.room.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(aliases = listOf("androidx-room-runtime", "androidx-room-ktx"))
                        ))
                        add("ksp", libs.findLibrary("androidx-room-compiler").get())

                        if (dataConfig.room.usePaging.getOrElse(false)) {
                            // IMPORTANT: Use 'implementation' (via implementDependency)
                            // so the KSP processor can see the LimitOffsetPagingSource type
                            implementDependency(libs, listOf(
                                Dependency(aliases = listOf("androidx-room-paging"))
                            ))
                        }
                    }

                    // --- SECURITY ---
                    if (dataConfig.security.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(aliases = listOf("androidx-security-crypto", "tink-android"))
                        ))
                    }

                    // --- DATASTORE ---
                    if (dataConfig.dataStore.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(Dependency(aliases = listOf("datastore-preferences"))))
                    }

                    // --- WORK MANAGER ---
                    if (dataConfig.workManager.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(aliases = listOf("androidx-work-runtime-ktx", "androidx-hilt-work"))
                        ))
                        add("ksp", libs.findLibrary("androidx-hilt-compiler").get())
                    }

                    // --- NETWORKING (RETROFIT) ---
                    if (dataConfig.retrofit.enabled.getOrElse(false)) {
                        implementDependency(libs, listOf(
                            Dependency(aliases = listOf("retrofit-core", "okhttp-logging", "converter-gson"))
                        ))

                        if (dataConfig.retrofit.useChucker.getOrElse(false)) {
                            add("debugImplementation", libs.findLibrary("chucker-library").get())
                            add("releaseImplementation", libs.findLibrary("chucker-library-no-op").get())
                        }

                        if (dataConfig.retrofit.useKotlinSerialization.getOrElse(false)) {
                            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
                            implementDependency(libs, listOf(
                                Dependency(aliases = listOf("retrofit-converter-kotlinx-serialization"))
                            ))
                        }
                    }
                }
            }
        }
    }
}