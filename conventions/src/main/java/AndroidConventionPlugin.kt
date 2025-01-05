import com.appspiriment.conventions.AppspirimentExtension
import com.appspiriment.conventions.Dependency
import com.appspiriment.conventions.ImplType
import com.appspiriment.conventions.applyPluginFromLibs
import com.appspiriment.conventions.copyAppspirimentLibs
import com.appspiriment.conventions.getVersionCodes
import com.appspiriment.conventions.implementDependency
import com.appspiriment.conventions.libs
import com.appspiriment.conventions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME as TestImpl

abstract class AndroidConventionPlugin : Plugin<Project> {

    protected val versions = getVersionCodes()

    private val _requiredPluginList = listOf(
        "kotlin-android",
        "dagger-hilt-android",
        "devtools-ksp",
        "kotlin-parcelize"
    )
    private val _requiredDependencies: List<Dependency> =
        listOf(
            Dependency(type = ImplType.BUDNDLE, config = Impl, aliases = listOf("android-base")),
            Dependency(
                type = ImplType.BUDNDLE,
                config = TestImpl,
                aliases = listOf("android-test")
            ),
            Dependency(type = ImplType.DEPENDENCY, config = Impl, aliases = listOf("hilt-android")),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "ksp",
                aliases = listOf("hilt-compiler")
            ),
        )

    override fun apply(target: Project) {
        applyPlugin(target)
    }

    protected fun applyPlugin(
        target: Project,
        requiredPluginList: List<String> = emptyList(),
        requiredDependencies: List<Dependency> = emptyList()
    ) {
        with(target) {
            copyAppspirimentLibs(baseDir = project.parent?.projectDir)
            val libs = project.libs
            val requiredLibs = project.requiredLibs
            project.extensions.create("configureModule", AppspirimentExtension::class.java)
            extensions.configure(AppspirimentExtension::class.java) {
                with(pluginManager) {
                    applyPluginFromLibs(
                        requiredLibs to requiredPluginList.plus(_requiredPluginList),
                        libs to pluginList
                    )
                }

                dependencies {
                    implementDependency(
                        libs = requiredLibs,
                        dependencyList = _requiredDependencies.plus(requiredDependencies)
                    )
                    implementDependency(libs = libs, dependencyList = dependencyList)
                }
            }
        }
    }


}

