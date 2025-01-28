import com.appspiriment.conventions.AppspirimentExtension
import com.appspiriment.conventions.androidLibrary
import com.appspiriment.conventions.configureAndroid
import org.gradle.api.Project

open class AndroidLibraryConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-android-library",
    )
    override val Project.configurationLambda: (AppspirimentExtension) -> Unit
        get() = {
            androidLibrary {
                configureAndroid(
                    commonExtension = this,
                    version = versions,
                    config = it
                )
            }
        }

    override fun apply(target: Project) {
        applyPlugin(
            target = target,
            requiredPluginList = requiredPluginList,
        )
    }
}
