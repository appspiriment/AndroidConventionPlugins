import com.appspiriment.conventions.applyPluginFromLibs
import com.appspiriment.conventions.copyAppspirimentLibs
import com.appspiriment.conventions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.version

class AndroidSettingsConventionPlugin : Plugin<Project> {

    private val includePluginInClassPaths = versionsRefs.run {
        listOf(
            "google-android-application",
            Pair("com.android.library", getValue("agp")),
            Pair("org.jetbrains.kotlin.android", getValue("kotlinVersion")),
            Pair("org.jetbrains.kotlin.jvm", getValue("kotlinVersion")),
            Pair("com.google.devtools.ksp", getValue("ksp")),
            Pair("org.jetbrains.kotlin.plugin.compose", getValue("kotlinVersion")),
            Pair("com.google.dagger.hilt.android", getValue("hilt")),
            Pair(
                "org.jetbrains.kotlin.plugin.serialization",
                getValue("kotlinserializeplugin")
            )
        )
    }

    override fun apply(target: Project) {
        with(target) {
            //Plugin Management
            with(pluginManager) {
                    applyPluginFromLibs(
                        requiredLibs to includePluginInClassPaths
                    )
                    apply() {
                        includePluginInClassPaths.forEach {
                            rootProject.buildFile.id(it.first) version it.second apply false
                        }
                    }
                }
            }


            settings.buildscript.sourceFile?.let{file ->
                file.readLines().run{
                    if(joinToString().contains("$appspirimentTomlName.versions.toml").not()) {
                        this.toMutableList().apply{
                            add(" dependencyResolutionManagement {")
                            add("    versionCatalogs {")
                            add("        create(\"$appspirimentTomlName\") {")
                            add("            from(files(\"gradle/$appspirimentTomlName.versions.toml\"))")
                            add("        }\n    }\n}")
                        }.joinToString("\n").let{
                            file.writeText(it)
                        }
                    }
                }
            }
            copyAppspirimentLibs(baseDir = target.buildscript.sourceFile?.parentFile)
        }
    }
}