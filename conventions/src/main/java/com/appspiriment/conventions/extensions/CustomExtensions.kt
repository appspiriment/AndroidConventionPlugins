package com.appspiriment.conventions.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

internal const val APPSPIRIMENT_EXTENSION_NAME = "appspiriment"
internal const val DATA_LAYER_EXTENSION_NAME = "dataLayer"

/**
 * Extension for Appspiriment convention plugins.
 * Exposed as the `appspiriment { }` block in module build scripts.
 *
 * Uses Gradle's Property API for lazy evaluation and configuration-cache compatibility.
 * Declared as an abstract class so Gradle can instantiate it via `extensions.create`
 * with full managed-property support (consistent with [DataLayerExtension]).
 *
 * Example usage:
 * ```kotlin
 * appspiriment {
 *     enableUtils.set(false)            // default: true
 *     enableMinify.set(true)            // default: false
 *     addDevSuffixToDebug.set(false)    // default: true (app modules only)
 *     scaffoldThemeResources.set(false) // default: true (app + compose modules only)
 * }
 * ```
 */
abstract class AppspirimentExtension @Inject constructor(objects: ObjectFactory) {
    /** Whether to add appspiriment-utils, logutils-dev/prod. Default: true. */
    abstract val enableUtils: Property<Boolean>
    /** Whether to enable R8 minification in the release build type. Default: false. */
    abstract val enableMinify: Property<Boolean>
    /**
     * Whether to append a `.dev` suffix to `applicationId` and a `.yyyyMMdd.HHmmss`
     * timestamp to `versionName` in debug builds.
     * Example: applicationId `com.example.app.dev`, versionName `1.0.0.20260504.143022`.
     * Default: true. Application modules only.
     */
    abstract val addDevSuffixToDebug: Property<Boolean>

    /**
     * When `true` (default), the `scaffoldAppspirimentResources` task is registered and
     * wired to `preBuild` for app modules that use Compose.  On first build it writes:
     *
     * - `src/main/res/values/appspiriment_colors.xml`         — day theme colours
     * - `src/main/res/values-night/appspiriment_colors.xml`   — night theme colours
     * - `src/main/res/values/appspiriment_dimens.xml`         — font sizes & spacing
     *
     * These files use the same resource names as the compose-utils AAR, so editing them
     * overrides the library's defaults app-wide (no Kotlin code changes required).
     *
     * **Files are never overwritten once they exist** — your edits are permanent.
     * Set to `false` to opt out entirely and manage theme resources by hand.
     */
    abstract val scaffoldThemeResources: Property<Boolean>
}

/**
 * Convenience extension function to configure the [DataLayerExtension] from a build script.
 *
 * Example usage:
 * ```kotlin
 * dataLayer {
 *     room { enabled.set(true) }
 *     retrofit { enabled.set(true); useChucker.set(true) }
 * }
 * ```
 */
fun Project.dataLayer(configure: Action<DataLayerExtension>) {
    extensions.configure(DATA_LAYER_EXTENSION_NAME, configure)
}

/**
 * Configuration extension for the `io.github.appspiriment.data` plugin.
 * Each nested block is opt-in — nothing is added unless explicitly enabled.
 */
abstract class DataLayerExtension @Inject constructor(objects: ObjectFactory) {
    val room: RoomConfig = objects.newInstance(RoomConfig::class.java)
    fun room(action: Action<RoomConfig>) = action.execute(room)

    val retrofit: RetrofitConfig = objects.newInstance(RetrofitConfig::class.java)
    fun retrofit(action: Action<RetrofitConfig>) = action.execute(retrofit)

    val security: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun security(action: Action<SimpleConfig>) = action.execute(security)

    val dataStore: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun dataStore(action: Action<SimpleConfig>) = action.execute(dataStore)

    val workManager: SimpleConfig = objects.newInstance(SimpleConfig::class.java)
    fun workManager(action: Action<SimpleConfig>) = action.execute(workManager)
}

/**
 * Configuration for Room persistence.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class RoomConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Room. Adds room-runtime, room-ktx (implementation) and room-compiler (ksp). */
    abstract val enabled: Property<Boolean>
    /** Also add room-paging for Paging 3 integration. */
    abstract val usePaging: Property<Boolean>
}

/**
 * Configuration for Retrofit networking.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class RetrofitConfig @Inject constructor(objects: ObjectFactory) {
    /** Enable Retrofit. Adds retrofit-core, okhttp, okhttp-logging, converter-gson. */
    abstract val enabled: Property<Boolean>
    /** Add Chucker as debugImplementation / no-op as releaseImplementation. */
    abstract val useChucker: Property<Boolean>
    /** Use kotlinx.serialization converter instead of Gson. Applies the serialization plugin. */
    abstract val useKotlinSerialization: Property<Boolean>
}

/**
 * Simple on/off configuration for a single feature.
 * Declared as an abstract class so Gradle can instantiate it via [ObjectFactory.newInstance].
 */
abstract class SimpleConfig @Inject constructor(objects: ObjectFactory) {
    abstract val enabled: Property<Boolean>
}
