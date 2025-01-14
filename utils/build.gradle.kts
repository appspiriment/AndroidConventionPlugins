plugins {
    alias(libs.plugins.google.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

signing {
    sign(publishing.publications)
}

android {
    namespace = "io.github.appspiriment.utils"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        aarMetadata {
            minCompileSdk = 34
        }
        testFixtures {
            enable = true
        }
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        libs.versions.javaVersion.get().toInt().let { JavaVersion.toVersion(it) }.let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }
    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit.test)
}


publishing {

    publications {
        create<MavenPublication>("mavenUtils") {
            groupId = "io.github.appspirimentlabs"
            artifactId = "utils"
            version = libs.versions.appspirimentUtils.get()
            pom {
                name = "Appspiriment Utils"
                description = "A library with common util functions and extension methods to help kotlin developers easily use them."
                url = "https://github.com/appspirimentlabs/AndroidConventionPlugins/tree/main/utils"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "appspirimentlabs"
                        name = "Appspiriment Labs"
                        email = "appspiriment@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://example.com/my-library.git"
                    developerConnection = "scm:git:ssh://example.com/my-library.git"
                    url = "http://example.com/my-library/"
                }
            }


            afterEvaluate { from(components["release"])}
        }
    }
}