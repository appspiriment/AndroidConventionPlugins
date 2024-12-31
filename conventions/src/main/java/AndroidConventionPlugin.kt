
import com.appspiriment.conventions.getVersionCodes
import com.appspiriment.conventions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import kotlin.jvm.optionals.getOrNull


open class AndroidConventionPlugin : Plugin<Project> {

    protected val versions = getVersionCodes()


    private val _pluginList = listOf(
        "kotlin-android",
        "dagger-hilt-android",
        "devtools-ksp",
    )
    private val _kspList = listOf(
        "hilt-compiler"
    )
    private val _dependencyImplList = listOf(
        "hilt-android",

        //Android Base
        "androidx-core-ktx",
        "androidx-lifecycle-runtime-ktx",
        "kotlinx-coroutines-android",
        "multidex"
    )

    override fun apply(target: Project) {
        applyPlugin(target)
    }

    private fun applyPlugin(target: Project) {
        with(target) {
            project.extensions.create("AppspirimentExtension", AppspirimentExtension::class.java)

            extensions.configure(AppspirimentExtension::class.java) {
                with(pluginManager) {
                    pluginList.plus(_pluginList).forEach {
                        libs.findPlugin(it).ifPresentOrElse({ plugin ->
                            apply(plugin.get().pluginId)
                        }) {
                            throw Exception("Plugin $it not found in libs")
                        }
                    }
                }

                dependencies {
                    implement(
                        libs,
                        JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                        dependencyBundleList,
                        isBundle = true
                    )
                    implement(
                        libs,
                        JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                        _dependencyImplList.plus(dependencyImplList)
                    )
                    implement(libs, JavaPlugin.API_CONFIGURATION_NAME, dependencyApiList)
                    implement(
                        libs,
                        JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                        platformImplList,
                        isPlatform = true
                    )
                    implementProject(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, projectImplList)
                    implementProject(JavaPlugin.API_CONFIGURATION_NAME, projectApiList)
                    implement(libs, JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, testImplList)
                    implement(
                        libs,
                        JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME,
                        testBundleImplList,
                        isBundle = true
                    )

                    _kspList.plus(kspList).takeIf { it.isNotEmpty() }?.forEach {
                        "ksp"(libs.findLibrary(it).get())
                    }
                }
            }
        }
    }

    private fun DependencyHandlerScope.implementBundles(libs: VersionCatalog, config:String, aliases: List<String>){
        aliases.forEach { alias ->
            libs.findBundle(alias).getOrNull()?.let{
                add(config, it)
            } ?: throw Exception("Dependency Bundle $alias not found in libs")
        }
    }
    private fun DependencyHandlerScope.implementProject(
        config:String,
        aliases: List<String>
    ) {
        aliases.forEach {
            add(config, project(it))
        }
    }
    private fun DependencyHandlerScope.implement(
        libs: VersionCatalog,
        config:String,
        aliases: List<String>,
        isBundle: Boolean = false,
        isPlatform: Boolean = false,
    ){
        aliases.forEach { alias ->
            libs.run{
                if(isBundle) findBundle(alias) else findLibrary(alias)
            }.getOrNull()?.let{
                add(config, if(isPlatform) platform(it) else it)
            } ?: throw Exception("Dependency $alias not found in libs")
        }
    }
}

open class AppspirimentExtension(
    var pluginList : List<String> = emptyList(),
    var dependencyBundleList  : List<String> = emptyList(),
    var dependencyImplList  : List<String> = emptyList(),
    var dependencyApiList  : List<String> = emptyList(),
    var testBundleImplList  : List<String> = emptyList(),
    var testImplList  : List<String> = emptyList(),
    var platformImplList  : List<String> = emptyList(),
    var projectImplList  : List<String> = emptyList(),
    var projectApiList  : List<String> = emptyList(),
    var kspList  : List<String> = emptyList(),
)
