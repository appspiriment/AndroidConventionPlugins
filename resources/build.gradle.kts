import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`                    // if you want to use Kotlin DSL style
    `java-library`
    alias(libs.plugins.vanniktech.publish)
}
sourceSets {
    main {
        resources {
            srcDirs("src/main/res")
        }
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(appspirimentlibs.versions.javaVersion.get())
        )
    }
}

dependencies {
    // No runtime dependencies needed for colors/dimens/themes only

    // If you later decide to depend on Material Components for theme bridging:
    // implementation(libs.google.material)

    // ────────────────────────────────────────────────
    // Testing – only enable if you add actual tests in this module
    // ────────────────────────────────────────────────
    // testImplementation(libs.junit.test)
    // androidTestImplementation(libs.androidx.test.junit)
    // androidTestImplementation(libs.androidx.espresso.core)
    // androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // debugImplementation(libs.androidx.compose.ui.test.manifest)
}


mavenPublishing {
    coordinates(
        artifactId = "resources",
        version = libs.versions.appspirimentResource.get()
    )

    pom {
        name = "Appspiriment Default Resources"
        group = "io.github.appspiriment"
        description = "Default Resources"
        url = "https://github.com/appspiriment/AndroidUtils"
    }
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    signAllPublications()
}