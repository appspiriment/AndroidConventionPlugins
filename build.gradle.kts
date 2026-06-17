// Top-level build file for the Appspiriment Convention Plugins project.
// Module-specific configuration is in each module's build.gradle.kts.
plugins {
    alias(libs.plugins.google.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.google.firebase.appdistribution) apply false
    alias(libs.plugins.google.firebase.perf) apply false
    alias(libs.plugins.google.android.test) apply false
}
