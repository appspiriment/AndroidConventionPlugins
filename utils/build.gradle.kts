import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.google.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.28.0"
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
//    publishing {
//        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
//        }
//    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit.test)
}

//publishing {
//
//    publications {
//        create<MavenPublication>("mavenUtils") {
//            groupId = "io.github.appspiriment"
//            artifactId = "utils"
//            version = libs.versions.appspirimentUtils.get()
//            pom {
//                name = "Appspiriment Utils"
//                description = "A library with common util functions and extension methods to help kotlin developers easily use them."
//                url = "https://github.com/appspiriment/AndroidConventionPlugins/tree/main/utils"
//                licenses {
//                    license {
//                        name = "The Apache License, Version 2.0"
//                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
//                    }
//                }
//                developers {
//                    developer {
//                        id = "appspiriment"
//                        name = "Appspiriment Labs"
//                        email = "appspiriment@gmail.com"
//                    }
//                }
//                scm {
//                    connection = "scm:git:git://example.com/my-library.git"
//                    developerConnection = "scm:git:ssh://example.com/my-library.git"
//                    url = "http://example.com/my-library/"
//                }
//            }
//
//
//            afterEvaluate { from(components["release"])}
//        }
//    }
//
//
//    // Here we define some repositories that we can publish our outputs to.
//    repositories {
//        // Specifying that this is a custom maven repository.
//        maven {
//            // This is the name of the repo that is used as the value of ${target}
//            // from above.
//            name = "OSSRH"
//
//            // Self-explanatory.
//            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
//
//            // These need to be defined in ~/.gradle/gradle.properties:
//            // ossrhUsername=<your sonatype jira username>
//            // ossrhPassword=<your sonatype jira password>
//            credentials {
//                username = project.findProperty("ossrhUsername") as String?
//                password = (project.findProperty("ossrhPassword") as String?).also{
//                    println("PasswordToken - $it")
//                }
//            }
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
//        }
//    }
//}

mavenPublishing {
    coordinates(
        groupId = "io.github.appspiriment",
        artifactId = "utils",
        version = libs.versions.appspirimentUtils.get()
    )

    pom {
        name = "Appspiriment Utils"
        description = "A library with common util functions and extension methods to help kotlin developers easily use them."
        url = "https://github.com/appspiriment/AndroidConventionPlugins/tree/main/utils"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "appspiriment"
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

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}