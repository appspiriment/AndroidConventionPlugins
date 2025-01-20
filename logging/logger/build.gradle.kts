
plugins {
    alias(libs.plugins.appspiriment.publish)
}

android{
    namespace = "io.github.appspiriment.logger"
}

mavenPublishing {
    coordinates(
        artifactId = "logger",
        version = libs.versions.appspirimentUtils.get()
    )

    pom {
        name = "Appspiriment Logger"
        description = "A library with basicl ogging functions. This should only be used in debugImplementation, never in release mode."
        url = "https://github.com/appspiriment/AndroidConventionPlugins/tree/main/logger"
    }
    signAllPublications()
}