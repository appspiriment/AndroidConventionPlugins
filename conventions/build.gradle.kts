import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
    signing
}
group = "com.appspiriment.plugins"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}
dependencies {
    compileOnly(libs.android.gradle.plugin)
//    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

group = "com.appspiriment.conventions"
version = "0.1.1"
signing {
    sign(publishing.publications)
}
gradlePlugin {
    plugins {
        website = "https://github.com/arunkarshan/AndroidConventionPlugins"
        vcsUrl = "https://github.com/arunkarshan/AndroidConventionPlugins"
        create("androidApplication") {
            id = "com.appspiriment.application"
            displayName = "Android Convention Plugin"
            description = "This plugin applies the required configurations for an Android application module, along with Hilt DI. It also manage the versioning of the app by updating the version on each build using version.properties. The required libraries like android plugin and kotlin plugin needs to be added in the libs.versions.toml."
            tags = listOf("android", "application", "conventions")
            implementationClass = "AndroidConventionPlugin"
        }
    }
}
