
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
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    // Enable GPG signing for all publications
    signAllPublications()
}