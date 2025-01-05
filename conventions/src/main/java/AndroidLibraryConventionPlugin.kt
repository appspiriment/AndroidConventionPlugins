import com.appspiriment.conventions.androidLibrary
import com.appspiriment.conventions.projectConfigs
import com.appspiriment.conventions.configureKotlinAndroid
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl

open class AndroidLibraryConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-google-android-library",
    )

    private val composeLibrary: List<Dependency> =
        listOf(
            Dependency(type = ImplType.BUDNDLE, config = Impl, aliases = listOf("android-compose")),
            Dependency(
                type = ImplType.DEPENDENCY, config = Impl, aliases = listOf(
                    "lottie-compose", "hilt-compose-navigation"
                )
            ),
            Dependency(
                type = ImplType.PLATFORM,
                config = Impl,
                aliases = listOf("androidx-compose-bom")
            )
        )

    override fun apply(target: Project) {
        with(target) {
            project.extensions.create("configureLibrary", ConfigureLibrary::class.java)

            extensions.configure(ConfigureLibrary::class.java) {
                super.applyPlugin(
                    target = target,
                    requiredPluginList = requiredPluginList.run{
                        if (isComposeLibrary) plus("kotlinx-serialization") else this
                    },
                    requiredDependencies = if (isComposeLibrary) composeLibrary else emptyList()
                )

                androidLibrary {
                    configureKotlinAndroid(commonExtension = this, version = versions)
                }
            }
        }
    }
}

open class ConfigureLibrary(
    var isComposeLibrary: Boolean = false
)