import com.appspiriment.conventions.AppspirimentExtension
import org.gradle.api.Project

open class AndroidApplicationConventionPlugin : AndroidConventionPlugin() {

    private val requiredPluginList = listOf(
        "google-android-application",
        "kotlinx-serialization",
    )
    override val Project.configurationLambda: (AppspirimentExtension) -> Unit
        get() = {  }

    override fun apply(target: Project) {
        applyPlugin(
            target = target,
            requireHilt = true,
            requireCompose = true,
            requiredPluginList = requiredPluginList,
        )
    }
}



