import com.appspiriment.conventions.Dependency
import com.appspiriment.conventions.ImplType
import com.appspiriment.conventions.androidLibrary
import com.appspiriment.conventions.applyPluginFromLibs
import com.appspiriment.conventions.configureKotlinAndroid
import com.appspiriment.conventions.getVersionCodes
import com.appspiriment.conventions.implementDependency
import com.appspiriment.conventions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME as TestImpl

class AndroidMavenPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager){
                applyPluginFromLibs(
                    libs to  listOf(
                        "google-android-library",
                        "kotlin-android",
                        "vanniktech-publish",
                    )
                )
            }

            androidLibrary {
                configureKotlinAndroid(commonExtension = this, version = getVersionCodes())
                dependencies {
                    implementDependency(
                        libs = libs,
                        dependencyList = listOf(
                            Dependency(
                                type = ImplType.DEPENDENCY,
                                config = TestImpl,
                                aliases = listOf("junit-test")
                            ),
                        )
                    )
                }
            }
            val publishingGradle = "\nplugins {\n" +
                    "    alias(libs.plugins.appspiriment.publish)\n" +
                    "}\n" +
                    "\n" +
                    "android{\n" +
                    "    namespace = \"io.github.appspiriment.\"\n" +
                    "}\n\n" +
                    "mavenPublishing {\n" +
                    "    coordinates(\n" +
                    "        artifactId = \"\",\n" +
                    "        version = libs.versions.appspirimentUtils.get()\n" +
                    "    )\n" +
                    "\n" +
                    "    pom {\n" +
                    "        name = \"Appspiriment \"\n" +
                    "        description = \"A library \"\n" +
                    "        url = \"https://github.com/appspiriment/AndroidConventionPlugins/tree/main/\"\n" +
                    "    }\n" +
                    "    signAllPublications()\n" +
                    "}"
            target.buildscript.sourceFile?.let { file ->
                file.readLines().toMutableList().let{lines ->
                   if(lines.any{it.contains("mavenPublishing")}.not() && lines.any{it.contains("coordinates(")}.not()) {
                       file.writeText(publishingGradle)
                   }
                }
            }
        }
    }
}