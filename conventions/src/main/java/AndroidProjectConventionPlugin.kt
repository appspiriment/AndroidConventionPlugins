import com.appspiriment.conventions.copyAppspirimentLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class AndroidProjectConventionPlugin : Plugin<Project> {

    private val includePluginInClassPaths = listOf(
        "google.android.application",
        "google.android.library",
        "kotlin.android",
        "kotlin.compose",
        "kotlin.jvm",
        "devtools.ksp",
        "dagger.hilt.android",
        "kotlinx.serialization",
    )
    override fun apply(target: Project) {

        @Suppress("KotlinConstantConditions")
        if(appspirimentTomlName !="libs") {
            File(target.rootDir.path + "/settings.gradle.kts").let { file ->
                file.readLines().toMutableList().run {
                    if (any { it.contains("$appspirimentTomlName.versions.toml") }.not()) {
                        val versionCatalogDeclaration = "    versionCatalogs {\n" +
                                "        create(\"$appspirimentTomlName\") {\n" +
                                "            from(files(\"gradle/$appspirimentTomlName.versions.toml\"))\n" +
                                "        }\n" +
                                "    }\n\n"
                        this.joinToString("\n").let {
                            this.add(
                                index = indexOfFirst { line -> line.contains("dependencyResolutionManagement") } + 1,
                                element = versionCatalogDeclaration
                            )
                            file.writeText(this.joinToString("\n"))
                        }
                    }
                }
            }
        }

        target.rootDir.path.let { path ->
            val ksp2Line = "ksp.useKSP2=true"
            File("$path/gradle.properties").let{file->
                file.readLines().apply {
                    if(any{it.contains(ksp2Line)}.not()) {
                        file.writeText(toMutableList().apply { add(ksp2Line) }.joinToString("\n"))
                    }
                }
            }

        }
        target.rootProject.buildscript.sourceFile?.let { file ->
            file.readLines().toMutableList().let{lines ->
                val plugins = includePluginInClassPaths.mapNotNull{
                    "    alias($appspirimentTomlName.plugins.$it) apply false".takeUnless {
                        lines.any {line-> line.contains(it)}
                    }
                }
                val firstPluginLine = lines.indexOfFirst{ it.contains("io.github.appspiriment.project")
                        || it.contains("appspirimentlibs.plugins.appspiriment.project")}
                lines.removeAt(firstPluginLine)
                lines.add( firstPluginLine, plugins.joinToString("\n"))
                lines.add(firstPluginLine,"//    id(\"io.github.appspiriment.project\") version \"$libVersion\"")
//                lines.add(firstPluginLine,"//    alias(appspirimentlibs.plugins.appspiriment.project)")
                file.writeText(lines.joinToString("\n"))
            }
        }

        target.rootDir.absolutePath.let { File("$it/app/build.gradle.kts") }.let{file ->
            if(!file.readText().contains("plugins.appspiriment.application")) {
                val appId = file.readLines().toMutableList()
                    .firstOrNull { line -> line.contains("applicationId") }?.split("=")?.get(1)
                    ?.trim()?.replace("\"", "") ?: "com.example.application"
                file.writeText(getDefaultAppGradle(appId))
            }
        }
        copyAppspirimentLibs(baseDir = target.buildscript.sourceFile?.parentFile)
    }
}
