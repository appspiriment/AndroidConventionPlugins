package com.appspiriment.conventions.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.tools.r8.internal.re
import com.appspiriment.conventions.extensions.appspirimentLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

open class AndroidLibraryConventionPlugin : AndroidBaseConventionPlugin() {
    override val Project.commonExtension get() = extensions.getByType<LibraryExtension>()
    open val setupHilt : Boolean = false
    open val setupCompose : Boolean = false

    override fun apply(target: Project) {
        // Explicitly apply the Android Application plugin first
        target.run{
            plugins.apply(target.appspirimentLibs.findPlugin("google-android-library").get().get().pluginId)
            if(setupHilt) setupHilt()
            if(setupCompose) setupCompose()
            super.apply(this)
        }
    }
}

// AndroidLibraryHiltConventionPlugin.kt
open class AndroidLibraryHiltConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupHilt: Boolean = true
}

// AndroidLibraryComposeConventionPlugin.kt
class AndroidLibraryComposeConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupCompose: Boolean = true
}

// AndroidLibraryHiltComposeConventionPlugin.kt
class AndroidLibraryHiltComposeConventionPlugin : AndroidLibraryConventionPlugin() {
    override val setupHilt: Boolean = true
    override val setupCompose: Boolean = true
}

