import com.appspiriment.conventions.Dependency
import com.appspiriment.conventions.ImplType
import com.appspiriment.conventions.androidApp
import com.appspiriment.conventions.configureAndroidKotlinAndCompose
import com.appspiriment.conventions.projectConfigs
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl

open class AndroidApplicationConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-android-application",
        "kotlinx-serialization"
    )

    private val requiredDependencies: List<Dependency> =
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
        super.applyPlugin(
            target = target,
            requiredPluginList = requiredPluginList,
            requiredDependencies = requiredDependencies
        )
        with(target) {
            androidApp {
//                val registerExtension: ExtensionAware.() -> Unit = {
//                    extensions.add("androidApplication", ConfigureApplication::class.java)
//                }
//
//                buildTypes.all { registerExtension() }
//                productFlavors.all { registerExtension() }

                configureAndroidKotlinAndCompose(commonExtension = this, version = versions)
                project.extensions.create("androidApplication", ConfigureApplication::class.java).apply {
                    namespace = appId
                    defaultConfig.apply {
                        targetSdk = projectConfigs.targetSdk
                        applicationId = appId
                        multiDexEnabled = true
                    }
                }

                buildTypes {
                    debug {
                        applicationIdSuffix = ".dev"
                        isMinifyEnabled = false
                        isShrinkResources = false
                        defaultConfig.versionCode = versions.debugCode
                        defaultConfig.versionName = versions.debugName
                    }
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        defaultConfig.versionCode = versions.releaseCode
                        defaultConfig.versionName = versions.releaseName
                    }
                }
            }
        }
    }
}

open class ConfigureApplication(
    var appId: String= "com.example.application"
)