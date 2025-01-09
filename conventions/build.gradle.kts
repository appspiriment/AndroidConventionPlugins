import org.jetbrains.kotlin.build.joinToReadableString
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
    signing
    `version-catalog`
}

group = "com.appspiriment.plugins"
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

group = "com.appspiriment.conventions"
version = pluginMainVersion

signing {
    sign(publishing.publications)
}

gradlePlugin {

    plugins {
        website = "https://github.com/arunkarshan/AndroidConventionPlugins"
        vcsUrl = "https://github.com/arunkarshan/AndroidConventionPlugins"
        create("androidApplication") {
            id = "com.appspiriment.application"
            displayName = "Android Application Convention Plugin"
            description =
                "This plugin applies the required configurations for an Android application module, along with Hilt DI. It also manage the versioning of the app by updating the version on each build using version.properties. The required libraries like android plugin and kotlin plugin will be added to requiredlibs.versions.toml."
            tags = listOf("android", "application", "conventions")
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "com.appspiriment.library"
            displayName = "Android Library Convention Plugin"
            description =
                "This plugin applies the required configurations for an Android Library module, along with Hilt DI. It also manage the versioning of the app by updating the version on each build using version.properties. It is also configurable with compose libraries. The required libraries will be added to requiredlibs.versions.toml."
            tags = listOf("android", "library", "conventions")
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        create("androidProject") {
            id = "com.appspiriment.project"
            displayName = "Android Project Root Convention Plugin"
            description =
                "This plugin applies the required configurations for Gradle also creating requiredlibs.versions.toml."
            tags = listOf("android", "Settings", "conventions")
            implementationClass = "AndroidProjectConventionPlugin"
        }
    }
}
publishing {
    repositories {
        mavenLocal()
    }
}

tasks.register("updateLibVersion") {
//
//    File(project.projectDir.path + "/src/main/java/com/appspiriment/conventions/LibsVersions.kt").run {
//        val lines = mutableListOf<String>().apply { addAll(readLines()) }
//        lines[lines.indexOfFirst { it.startsWith("const val LIB_VERSION") }] =
//            "const val LIB_VERSION = \"${libs.versions.appspiriment.get()}\""
//        writeText(lines.joinToString("\n"))
//
//    }

    File(project.parent?.projectDir?.path + "/gradle/requiredlibs.versions.toml").readLines().let {
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
//                     "internal val versionsRefs = mapOf(\n$versionRefs\n)\n\n" +
                        "internal val appspirimentLibRefs = AppspirimentLibRef(\n    versions= $versionRefs,\n    plugins= $pluginRefs,\n    libraries= $libraryRefs\n)"
            )
        }
    }
}
tasks.first().finalizedBy(tasks.named("updateLibVersion").get())