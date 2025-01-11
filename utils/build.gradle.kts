plugins {
    alias(libs.plugins.google.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "io.github.appspiriment.utils"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat.v7)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit.test)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("utils") {
            groupId = "io.github.appspiriment"
            artifactId = "utils"
            version = "0.0.1"
            pom {
                name = "Appspiriment Utils"
                description = "A library with common util functions and extension methods to help kotlin developers easily use them."
                url = "http://www.example.com/library"
                properties = mapOf(
                    "myProp" to "value",
                    "prop.with.dots" to "anotherValue"
                )
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "johnd"
                        name = "John Doe"
                        email = "john.doe@example.com"
                    }
                }
                scm {
                    connection = "scm:git:git://example.com/my-library.git"
                    developerConnection = "scm:git:ssh://example.com/my-library.git"
                    url = "http://example.com/my-library/"
                }
            }
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar"))}
        }
    }
}