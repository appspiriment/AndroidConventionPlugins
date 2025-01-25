import com.appspiriment.conventions.Dependency
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME as Impl

class AndroidRoomConventionPlugin : AndroidConventionPlugin() {

    private val requiredDependencyList = listOf(
        Dependency(config = Impl, aliases = listOf("android-room")),
        Dependency(config = Impl, aliases = listOf("room.ktx")),
        Dependency(config = "ksp", aliases = listOf("room.compiler")),
    )

    override fun apply(target: Project) {
        with(target) {
            applyPlugin(
                target = target,
                requireHilt = true,
                requiredDependencies = requiredDependencyList
            )

            extensions.configure<KspExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                 arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
            }
        }
    }

    /**
     * https://issuetracker.google.com/issues/132245929
     * [Export schemas](https://developer.android.com/training/data-storage/room/migrating-db-versions#export-schemas)
     */
    class RoomSchemaArgProvider(
        @get:InputDirectory
        @get:PathSensitive(PathSensitivity.RELATIVE)
        val schemaDir: File,
    ) : CommandLineArgumentProvider {
        init {
            if(!schemaDir.exists()) schemaDir.mkdir()
        }
        override fun asArguments() = listOf("room.schemaLocation=${schemaDir.path}")
    }
}