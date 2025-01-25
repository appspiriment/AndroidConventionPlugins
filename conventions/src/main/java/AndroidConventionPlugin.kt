import com.appspiriment.conventions.AppspirimentExtension
import com.appspiriment.conventions.Dependency
import com.appspiriment.conventions.EXTENSION_NAME
import com.appspiriment.conventions.ImplType
import com.appspiriment.conventions.applyPluginFromLibs
import com.appspiriment.conventions.appspirimentConfig
import com.appspiriment.conventions.copyAppspirimentLibs
import com.appspiriment.conventions.getVersionCodes
import com.appspiriment.conventions.implementDependency
import com.appspiriment.conventions.requiredLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.dependencies
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME as TestImpl

abstract class AndroidConventionPlugin : Plugin<Project> {
    protected val versions = getVersionCodes()

    private val _requiredPluginList = listOf(
        "kotlin-android",
    )

    private val _requiredHiltPluginList = listOf(
        "dagger-hilt-android",
        "devtools-ksp",
        "kotlin-parcelize"
    )

    private val _requiredComposePluginList = listOf(
        "kotlin-compose",
        "kotlinx-serialization",
    )

    private val _requiredDependencies: List<Dependency> =
        listOf(
            Dependency(type = ImplType.BUDNDLE, config = Impl, aliases = listOf("android-base")),
            Dependency(
                type = ImplType.BUDNDLE,
                config = TestImpl,
                aliases = listOf("android-test")
            ),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "debugImplementation",
                aliases = listOf("appspiriment-utils-dev")
            ),
            Dependency(
                type = ImplType.DEPENDENCY,
                config = "releaseImplementation",
                aliases = listOf("appspiriment-utils-prod")
            ),
        )

    private val _hiltDependencies: List<Dependency> = listOf(
        Dependency(type = ImplType.DEPENDENCY, config = Impl, aliases = listOf("hilt-android")),
        Dependency(
            type = ImplType.DEPENDENCY,
            config = "ksp",
            aliases = listOf("hilt-compiler")
        ),
    )


    private val _composeDependencies: List<Dependency> =
        listOf(
            Dependency(
                type = ImplType.PLATFORM,
                config = Impl,
                aliases = listOf("androidx-compose-bom")
            ),
            Dependency(type = ImplType.BUDNDLE, config = Impl, aliases = listOf("android-compose")),
            Dependency(
                type = ImplType.BUDNDLE,
                config = TEST_IMPLEMENTATION_CONFIGURATION_NAME,
                aliases = listOf("compose-test")
            ),
            Dependency(
                type = ImplType.DEPENDENCY, config = Impl, aliases = listOf(
                    "lottie-compose", "hilt-compose-navigation"
                )
            ),
        )

    protected fun applyPlugin(
        target: Project,
        requireHilt: Boolean = false,
        requireCompose: Boolean = false,
        requiredPluginList: List<String>? = null,
        requiredDependencies: List<Dependency>? = null,
        configureKotlin: Project.(AppspirimentExtension) -> Unit = {},
    ) {
        with(target) {
            copyAppspirimentLibs()

            var pluginList =  requiredPluginList?.apply { plus(_requiredPluginList) } ?: _requiredPluginList
            var dependencyList =
                _requiredDependencies.apply { requiredDependencies?.let { plus(it) } }

            extensions.create(EXTENSION_NAME, AppspirimentExtension::class.java).apply {
                pluginList = pluginList.apply {
                    if (requireHilt || requireCompose || enableCompose || enableHilt) {
                        plus(_requiredHiltPluginList)
                    }
                    if (requireCompose || enableCompose) plus(_requiredComposePluginList)
                }
                dependencyList = dependencyList.apply {
                    if (requireHilt || requireCompose || enableCompose || enableHilt) {
                        plus(_hiltDependencies)
                    }
                    if (requireCompose || enableCompose) plus(_composeDependencies)
                }
                pluginManager.applyPluginFromLibs(requiredLibs to pluginList)
                configureKotlin(this)
            }
            dependencies {
                add(Impl, requiredLibs.findLibrary("lottie-compose").get().get())
                implementDependency(
                    libs = requiredLibs,
                    dependencyList = dependencyList
                )
            }
        }
    }

    open fun Project.configure(config: AppspirimentExtension) {}
}
