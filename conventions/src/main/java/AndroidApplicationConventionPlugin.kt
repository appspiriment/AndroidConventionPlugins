import com.appspiriment.conventions.androidApp
import com.appspiriment.conventions.configureAndroid
import com.appspiriment.conventions.projectConfigs
import org.gradle.api.Project

open class AndroidApplicationConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-android-application",
        "kotlinx-serialization",
    )

    override fun apply(target: Project) {
        applyPlugin(
            target = target,
            requireHilt = true,
            requireCompose = true,
            requiredPluginList = requiredPluginList,
        ) {
            androidApp {
                configureAndroid(
                    commonExtension = this,
                    version = versions,
                    config = it
                )

                defaultConfig.apply {
                    targetSdk = projectConfigs.targetSdk
                    applicationId = namespace
                    multiDexEnabled = true
                }
                buildTypes {
                    debug {
                        applicationIdSuffix = ".dev"
                        isShrinkResources = false
                        defaultConfig.versionCode = versions.debugCode
                        defaultConfig.versionName = versions.debugName
                    }
                    release {
                        isShrinkResources = true
                        defaultConfig.versionCode = versions.releaseCode
                        defaultConfig.versionName = versions.releaseName
                    }
                }
            }
        }
    }
}



