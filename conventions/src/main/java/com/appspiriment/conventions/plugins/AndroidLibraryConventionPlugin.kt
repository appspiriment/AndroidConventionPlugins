package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.tools.r8.internal.re
import org.gradle.api.Project

open class AndroidBaseLibraryConventionPlugin : AndroidConventionPlugin() {
    open val requireHilt = false
    open val requireCompose = false
    private val requiredPluginList = listOf(
        "google-android-library",
    )
    override val Project.commonExtension: CommonExtension<*, *, *, *, *, *>
        get() = extensions.getByType(LibraryExtension::class.java)

    override fun apply(target: Project) {
        applyPlugin(
            target = target,
            requireHilt = requireHilt,
            requireCompose = requireCompose,
            requiredPluginList = requiredPluginList,
        )
    }
}

class AndroidHiltLibraryConventionPlugin : AndroidBaseLibraryConventionPlugin() {
    override val requireHilt: Boolean = true
}
class AndroidComposeLibraryConventionPlugin : AndroidBaseLibraryConventionPlugin() {
    override val requireCompose: Boolean = true
}
class AndroidHiltComposeLibraryConventionPlugin : AndroidBaseLibraryConventionPlugin() {
    override val requireHilt: Boolean = true
    override val requireCompose: Boolean = true
}
