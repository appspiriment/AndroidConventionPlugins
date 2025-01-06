import com.android.tools.r8.internal.fo
import com.appspiriment.conventions.Dependency
import com.appspiriment.conventions.applyPluginFromLibs
import com.appspiriment.conventions.copyAppspirimentLibs
import com.appspiriment.conventions.implementDependency
import com.appspiriment.conventions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File

class AndroidProjectConventionPlugin : Plugin<Project> {

    private val includePluginInClassPaths = versionsRefs.run {
        listOf(
            "google.android.application",
            "google.android.library",
            "kotlin.android",
            "kotlin.jvm",
            "devtools.ksp",
            "dagger.hilt.android",
            "kotlinx.serialization",
        )
    }

    override fun apply(target: Project) {

        File(target.rootDir.path + "/settings.gradle.kts").let { file ->
            file.readLines().toMutableList().run {
                if (any{it.contains("$appspirimentTomlName.versions.toml")}.not()) {
                        val versionCatalogDeclaration = "    versionCatalogs {\n" +
                        "        create(\"appspirimentlibs\") {\n" +
                        "            from(files(\"gradle/appspirimentlibs.versions.toml\"))\n" +
                        "        }\n" +
                        "    }\n\n"
                    this.joinToString("\n").let {
                        this.add(
                            index = indexOfFirst { line -> line.contains("dependencyResolutionManagement")} + 1,
                            element = versionCatalogDeclaration
                        )
                        file.writeText(this.joinToString("\n"))
                    }
                }
            }
        }

        target.rootProject.buildscript.sourceFile?.let { file ->
            file.readLines().toMutableList().let{lines ->
                val plugins = includePluginInClassPaths.mapNotNull{
                    "    alias(appspirimentlibs.plugins.$it) apply false".takeUnless {
                        lines.any {line-> line.contains(it)}
                    }
                }
                val firstPluginLine = lines.indexOfFirst{ it.contains("com.appspiriment.project") }
                lines.removeAt(firstPluginLine)
                lines.add( firstPluginLine, plugins.joinToString("\n")).let{
                    file.writeText(lines.joinToString("\n"))
                }
            }
        }

        target.rootDir.absolutePath.let { File("$it/app/build.gradle.kts") }.let{file ->
            val appId = file.readLines().toMutableList().firstOrNull{line -> line.contains("applicationId")}?.split("=")?.get(1)?.trim()?:"com.example.application"

            file.writeText(getDefaultAppGradle(appId))
        }
        copyAppspirimentLibs(baseDir = target.buildscript.sourceFile?.parentFile)
    }
}
