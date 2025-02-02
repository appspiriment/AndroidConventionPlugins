package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
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
            requireCompose = requireCompose,
            requiredPluginList = requiredPluginList,
        )
    }
}

class AndroidComposeLibraryConventionPlugin : AndroidBaseLibraryConventionPlugin() {
    override val requireHilt: Boolean = true
    override val requireCompose: Boolean = true
}
