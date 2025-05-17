import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
    signing
    `version-catalog`
}

val pluginVersion = "0.0.7"
val isDevBuild = false


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

val pluginMainVersion = getPluginDevVersion()

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
            implementationClass = "com.appspiriment.conventions.plugins.AndroidApplicationConventionPlugin"
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
            implementationClass = "com.appspiriment.conventions.plugins.AndroidProjectConventionPlugin"
        }

        create("androidRoom") {
            id = "io.github.appspiriment.room"
            displayName = "Android Room Convention Plugin"
            description =
                "This plugin simplifies the setup of Room for an Android module by automatically applying the required libraries and configurations. It fetches the necessary Room versions from the `appspirimentlibs.versions.toml` file, so ensure that this file is present in your project before applying the plugin. \n" +
                        "\n" +
                        "Once applied, the plugin configures the module with all the dependencies and settings needed to integrate Room seamlessly. This helps streamline the setup process, ensuring consistency and compatibility with the versions specified in your `appspirimentlibs.versions.toml` file."
            tags = listOf("android", "room", "conventions")
            implementationClass = "com.appspiriment.conventions.plugins.feature.AndroidRoomConventionPlugin"
        }

        //Library Group
        create("androidBaseLibrary") {
            id = "io.github.appspiriment.library-base"
            displayName = "Android Library Convention Plugin"
            description =
                "The Android Base Library Plugin is a foundational plugin designed to standardize the configuration of all your Android library modules. It automates the setup of core settings, ensuring consistency across your project. This plugin handles essential tasks such as applying the Android and Kotlin plugins, setting the compile and minimum SDK versions, configuring build types (debug/release), defining Java and Kotlin compilation options, and setting up default source sets and resource configurations. By using this plugin, you can significantly reduce boilerplate code and maintain a uniform structure across all your library modules."
            tags = listOf("android", "library", "conventions")
            implementationClass = "com.appspiriment.conventions.plugins.AndroidBaseLibraryConventionPlugin"
        }
        create("androidHiltLibrary") {
            id = "io.github.appspiriment.library-hilt"
            displayName = "Android Library Convention Plugin with Hilt"
            description =
                "The Android Compose Library Plugin streamlines the setup of Jetpack Compose and Hilt within your Android library modules. Leveraging the core configurations established by the Android Base Library Plugin, this plugin not only takes care of the fundamental settings like applying the Android and Kotlin plugins, setting SDK versions, and defining build types, but also automates the configuration of Compose build features, sets the correct Kotlin compiler extension version, and adds all the essential Compose dependencies. This includes UI, tooling, and testing libraries. Furthermore, it integrates Hilt by applying the necessary Hilt plugins and adding the required Hilt dependencies. By using this plugin, you can quickly enable Compose and Hilt in your library modules and ensure that all the necessary components are correctly configured, allowing you to start building beautiful and reactive UIs with ease while benefiting from dependency injection. It ensures that your library modules are consistently configured with the base settings, Hilt, and Compose."
            tags = listOf("android", "library", "conventions")
            implementationClass = "com.appspiriment.conventions.plugins.AndroidHiltLibraryConventionPlugin"
        }
        create("androidComposeLibrary") {
            id = "io.github.appspiriment.library-compose"
            displayName = "Android Library Convention Plugin with Hilt"
            description =
                "The Android Compose Library Plugin streamlines the setup of Jetpack Compose and Hilt within your Android library modules. Leveraging the core configurations established by the Android Base Library Plugin, this plugin not only takes care of the fundamental settings like applying the Android and Kotlin plugins, setting SDK versions, and defining build types, but also automates the configuration of Compose build features, sets the correct Kotlin compiler extension version, and adds all the essential Compose dependencies. This includes UI, tooling, and testing libraries. Furthermore, it integrates Hilt by applying the necessary Hilt plugins and adding the required Hilt dependencies. By using this plugin, you can quickly enable Compose and Hilt in your library modules and ensure that all the necessary components are correctly configured, allowing you to start building beautiful and reactive UIs with ease while benefiting from dependency injection. It ensures that your library modules are consistently configured with the base settings, Hilt, and Compose."
            tags = listOf("android", "library", "conventions")
            implementationClass = "com.appspiriment.conventions.plugins.AndroidComposeLibraryConventionPlugin"
        }
        create("androidLibrary") {
            id = "io.github.appspiriment.library"
            displayName = "Android Library Convention Plugin with Hilt"
            description =
                "The Android Compose Library Plugin streamlines the setup of Jetpack Compose and Hilt within your Android library modules. Leveraging the core configurations established by the Android Base Library Plugin, this plugin not only takes care of the fundamental settings like applying the Android and Kotlin plugins, setting SDK versions, and defining build types, but also automates the configuration of Compose build features, sets the correct Kotlin compiler extension version, and adds all the essential Compose dependencies. This includes UI, tooling, and testing libraries. Furthermore, it integrates Hilt by applying the necessary Hilt plugins and adding the required Hilt dependencies. By using this plugin, you can quickly enable Compose and Hilt in your library modules and ensure that all the necessary components are correctly configured, allowing you to start building beautiful and reactive UIs with ease while benefiting from dependency injection. It ensures that your library modules are consistently configured with the base settings, Hilt, and Compose."
            tags = listOf("android", "library", "conventions")
            implementationClass = "com.appspiriment.conventions.plugins.AndroidHiltComposeLibraryConventionPlugin"
        }
    }
}



tasks.register("updateLibFileVersion") {

    File(project.rootDir.path + "/gradle/appspirimentlibs.versions.toml").readLines().let {
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
            it.joinToString("\\n").replace("LIBVERSION", getPluginDevVersion())
                .replace("\"", "\\\"")

        File(project.projectDir.path + "/src/main/java/com/appspiriment/conventions/extensions/Constants.kt").run {
            val baseAppspirimentTomlName = "appspirimentlibs"
            writeText(
                "import com.appspiriment.conventions.extensions.AppspirimentLibRef\n\n" +
                        "internal const val appspirimentTomlName = \"$baseAppspirimentTomlName\"\n\n" +
                        "internal const val libVersion = \"${getPluginDevVersion()}\"\n\n" +
                        "internal const val appspirimentTomlContents = \"$libsContent\"\n\n" +
                        "internal fun getDefaultAppGradle() = \"plugins {\\n    alias($baseAppspirimentTomlName.plugins.appspiriment.application)\\n}\\n\\nandroid {\\n    namespace = \\\"com.example.app\\\"\\n    defaultConfig {\\n        applicationId = \\\"com.example.app\\\"\\n    }\\n}\\n\"\n\n" +
                        "internal val appspirimentLibRefs = AppspirimentLibRef(\n    versions= $versionRefs,\n    plugins= $pluginRefs,\n    libraries= $libraryRefs\n)"
            )
        }
    }
}
tasks.first().finalizedBy(tasks.named("updateLibFileVersion").get())



tasks.register("updateVersionForPortal") {
    File("pluginversion.properties").apply {
        val props = Properties()
        props.load(FileInputStream(this))
        props.getOrDefault("LASTDEV",1).let {
            writeText(
                "MAJOR=${pluginVersion}\nLASTDEV=$it"
            )
        }
    }
}
tasks.register("updateVersionForLocal") {
    File("pluginversion.properties").apply {
        val props = Properties()
        props.load(FileInputStream(this))
        props.getOrDefault("DEV",props.getOrDefault("LASTDEV",1)).toString().toInt().inc().let {
            writeText("MAJOR=${pluginVersion}\nDEV=${it}\nLASTDEV=$it")
        }
    }
}

internal fun getPluginDevVersion(): String {
    val propsFile = File("pluginversion.properties")
    if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        val major = props["MAJOR"].toString()
        val dev = if (isDevBuild && props.containsKey("DEV")) {
            props["DEV"].toString().padStart(2, '0').let { ".dev-$it" }
        } else null

        return dev?.let { "$major$it" } ?: major
    } else {
        throw FileNotFoundException("Could not read pluginversion.properties!")
    }
}