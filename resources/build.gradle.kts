plugins {
    alias(libs.plugins.google.android.library)
    alias(libs.plugins.vanniktech.publish)
}

android {
    namespace = "io.github.appspiriment.resources"
    compileSdk = appspirimentlibs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = appspirimentlibs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

mavenPublishing {
    coordinates(
        artifactId = "resources",
        version = libs.versions.appspirimentResource.get()
    )
    pom {
        name = "Appspiriment Default Resources"
        group = "io.github.appspiriment"
        description = "Default Material Design 3 color tokens and dimension resources for Android."
        url = "https://github.com/appspiriment/AndroidConventionPlugins"
    }
    signAllPublications()
}
