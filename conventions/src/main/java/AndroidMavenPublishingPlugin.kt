import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidMavenPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val publishingGradle = "\nplugins {\n" +
                    "    alias(appspirimentlibs.plugins.appspiriment.library)\n" +
                    "    alias(libs.plugins.vanniktech.publish)\n" +
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