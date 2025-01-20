import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
    signing
    `version-catalog`
}

val javaVersion = libs.versions.javaVersion.get()
java {
    JavaVersion.toVersion(javaVersion).let {
        sourceCompatibility = it
        targetCompatibility = it
    }
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javaVersion
    }
}

val pluginMainVersion = libs.versions.appspiriment.get()

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

signing {
    sign(publishing.publications)
}

gradlePlugin {

    plugins {
        group = "io.github.appspiriment.conventions"
        version = pluginMainVersion
        website = "https://github.com/appspiriment/AndroidConventionPlugins"
        vcsUrl = "https://github.com/appspiriment/AndroidConventionPlugins"
        create("androidApplication") {
            id = "io.github.appspiriment.application"
            displayName = "Android Application Convention Plugin"
            description =
                "This Gradle plugin configures an Android application module with the necessary setup for Hilt Dependency Injection (DI). It automatically manages versioning by updating the version in a `version.properties` file with each build. The plugin streamlines the setup for the Android application, enabling seamless integration with Hilt DI and automatic version management. \n" +
                        "\n" +
                        "Warning: Modifying or changing the `appspirimentlibs.versions.toml` file manually can cause the plugin to fail, as it may get overwritten during plugin updates. For any version changes or dependency additions, it is recommended to update the default `libs.versions.toml` file instead."
            tags = listOf("android", "application", "conventions")
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "io.github.appspiriment.library"
            displayName = "Android Library Convention Plugin"
            description =
                "This Gradle plugin configures an Android Library module with Hilt Dependency Injection (DI) using KSP (Kotlin Symbol Processing) and does not configure KAPT. It automatically applies the Hilt plugin to the library, enabling DI functionality with KSP. The plugin features a configurable extension, `configureLibrary`, which allows you to enable Compose capabilities by setting `isComposeLibrary` to true within the extension. Additionally, the plugin updates the `appspirimentlibs.versions.toml` file to manage and include the required dependencies for the library. This plugin streamlines the setup for Android libraries, providing seamless integration with Hilt (via KSP) and optional support for Compose."
            tags = listOf("android", "library", "conventions")
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        create("androidProject") {
            id = "io.github.appspiriment.project"
            displayName = "Android Project Root Convention Plugin"
            description =
                "This project plugin simplifies the initial setup and version management for an Android application. It removes unnecessary configurations from the app module, creates the `appspirimentlibs.versions.toml` file corresponding to the plugin version, and adds the required `appspiriment` plugin exclusively to the app module (without affecting any other application modules or libraries).\n" +
                        "\n" +
                        "Additionally, the plugin configures the `appspirimentlibs.versions.toml` in the `settings.gradle.kts`. **Note:** If your project is using a `settings.gradle` file in Groovy, it will automatically convert it to the Kotlin DSL (`settings.gradle.kts`).\n" +
                        "\nYou just need to add the plugin with correct version to the root build.gradle.kts (don't use alias, add it directly at first) and sync.\n" +
                        "This plugin is ideal for setting up a new project with a clean configuration or updating plugin versions. To update versions, simply update the project plugin version and sync. Be aware that this plugin will remove all configurations in the app module's Gradle file, so ensure that no additional configurations are lost."
            tags = listOf("android", "Settings", "conventions")
            implementationClass = "AndroidProjectConventionPlugin"
        }
        create("androidRoom") {
            id = "io.github.appspiriment.room"
            displayName = "Android Room Convention Plugin"
            description =
                "This plugin simplifies the setup of Room for an Android module by automatically applying the required libraries and configurations. It fetches the necessary Room versions from the `appspirimentlibs.versions.toml` file, so ensure that this file is present in your project before applying the plugin. \n" +
                        "\n" +
                        "Once applied, the plugin configures the module with all the dependencies and settings needed to integrate Room seamlessly. This helps streamline the setup process, ensuring consistency and compatibility with the versions specified in your `appspirimentlibs.versions.toml` file."
            tags = listOf("android", "room", "conventions")
            implementationClass = "AndroidRoomConventionPlugin"
        }
        create("androidPublish") {
            id = "io.github.appspiriment.mavenpublish"
            displayName = "Android Maven Publish Plugin"
            description =
                "This plugin simplifies the setup of library modules to be published to Maven Central, it still need to be configured. This plugin utilises 'vanniktech maven publish' plugin."
            tags = listOf("android", "publish")
            implementationClass = "AndroidMavenPublishingPlugin"
        }
    }
}
//publishing {
//    repositories {
//        mavenLocal()
//        maven {
//            name = "AndroidConventionPlugins"
//            /** Configure path of your package repository on Github
//             *  Replace GITHUB_USERID with your/organisation Github userID and REPOSITORY with the repository name on GitHub
//             */
//            url = uri("https://maven.pkg.github.com/appspiriment/AndroidConventionPlugins")
//
//            credentials {
//                val githubProperties = Properties()
//                githubProperties.apply{
//                    load(FileInputStream(rootProject.file("github.properties")))
//                }.run{
//                    username = get("gpr.usr")?.toString() ?: System.getenv("GPR_USER")
//                    password = get("gpr.key")?.toString() ?: System.getenv("GPR_API_KEY")
//                }
//            }
//        }
//    }
//}

tasks.register("updateLibVersion") {

    File(project.parent?.projectDir?.path + "/gradle/appspirimentlibs.versions.toml").readLines().let {
        fun List<String>.asString() = "listOf(\n${joinToString(",\n" )})"
        val versionRefs = it.subList(it.indexOf("[versions]") + 1, it.indexOf("[libraries]"))
            .filter { line -> !line.startsWith("#") && line.isNotBlank() }
            .map { item ->
                item.split("=").run {
                    "        \"${first().trim()}\""
                }
            }.asString()

        val pluginRefs = it.subList(it.indexOf("[plugins]") + 1, it.size-1)
            .filter { line -> !line.startsWith("#") && line.isNotBlank() }
            .map {line ->
                line.split("{")[1].split("id =")[1].split("\"")[1].trim().let{id->
                    "        \"$id\""
                }
            }.asString()

        val libraryRefs = it.subList(it.indexOf("[libraries]") + 1, it.indexOf("[bundles]"))
            .filter { line -> !line.startsWith("#") && line.isNotBlank() && line.contains("group =") && line.contains("name =")}
            .map {line ->
                val group = line.split("{")[1].split("group =")[1].split("\"")[1].trim()
                val artifact = line.split("{")[1].split("name =")[1].split("\"")[1].trim()
                "        Pair(\"$group\", \"$artifact\")"
            }.asString()
//
        val libsContent =
            it.joinToString("\\n").replace("LIBVERSION", libs.versions.appspiriment.get())
                .replace("\"", "\\\"")

        File(project.projectDir.path + "/src/main/java/com/appspiriment/conventions/Constants.kt").run {
            val baseAppspirimentTomlName = "appspirimentlibs"
            writeText(
                "import com.appspiriment.conventions.AppspirimentLibRef\nimport com.appspiriment.conventions.PluginRefs\nimport com.appspiriment.conventions.VersionRefs\n\n" +
                        "internal const val appspirimentTomlName = \"$baseAppspirimentTomlName\"\n\n" +
                        "internal const val libVersion = \"${libs.versions.appspiriment.get()}\"\n\n" +
                        "internal const val appspirimentTomlContents = \"$libsContent\"\n\n" +
                        "internal fun getDefaultAppGradle(appId: String) = \"plugins {\\n    alias($baseAppspirimentTomlName.plugins.appspiriment.application)\\n}\\nandroidApplication {\\n    appId = \\\"\$appId\\\"\\n}\"\n\n" +
                        "internal val appspirimentLibRefs = AppspirimentLibRef(\n    versions= $versionRefs,\n    plugins= $pluginRefs,\n    libraries= $libraryRefs\n)"
            )
        }
    }
}
tasks.first().finalizedBy(tasks.named("updateLibVersion").get())