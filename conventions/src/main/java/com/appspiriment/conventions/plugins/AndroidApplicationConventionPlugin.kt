package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

open class AndroidApplicationConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-android-application",
        "appspiriment-hilt",
        "kotlinx-serialization",
    )
    override val Project.commonExtension: CommonExtension<*, *, *, *, *, *>
        get() = extensions.getByType(
            ApplicationExtension::class.java
        )

    override fun apply(target: Project) {
        applyPlugin(
            target = target,
            requireCompose = true,
            requiredPluginList = requiredPluginList,
        )
    }
}



