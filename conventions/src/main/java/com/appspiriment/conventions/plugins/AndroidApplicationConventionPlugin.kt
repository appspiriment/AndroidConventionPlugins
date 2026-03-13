package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.appspiriment.conventions.extensions.appspirimentLibs
import com.appspiriment.conventions.extensions.hiltDependencies
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

// AndroidAppConventionPlugin.kt
class AndroidApplicationConventionPlugin : AndroidBaseConventionPlugin() {

    override val Project.commonExtension get() = extensions.getByType<ApplicationExtension>()

    override fun apply(target: Project) {
        // 1. Apply the Android Application plugin **first** — explicitly
        target.run {
            plugins.apply(
                target.appspirimentLibs.findPlugin("google-android-application").get()
                    .get().pluginId
            )

            setupHilt()
            setupCompose()
            super.apply(this)
        }
    }
}


