// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id 'maven-publish'
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"]) // Use the release variant's artifacts

                groupId = "your_group_id" // Replace with your group ID
                artifactId = "android-convention-plugin" // Replace with your artifact ID
                version = "1.0.0" // Replace with your version

            }
        }
        repositories {
            mavenLocal() // Publish to your local Maven repository
        }
    }
}