package com.appspiriment.conventions.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

internal const val APPSPIRIMENT_EXTENSION_NAME = "appspiriment"
internal const val DATA_LAYER_EXTENSION_NAME = "dataLayer"

/**
 * Base extension for Appspiriment convention plugins.
 * Uses Gradle Property API for lazy evaluation and configuration caching.
 */
interface AppspirimentExtension {
    val enableUtils: Property<Boolean>
    val enableMinify: Property<Boolean>
    val addDevSuffixToDebug: Property<Boolean>
}



// This is the secret sauce: An extension function on Project
fun Project.dataLayer(configure: Action<DataLayerExtension>) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(DATA_LAYER_EXTENSION_NAME, configure)
}

abstract class DataLayerExtension @Inject constructor(objects: ObjectFactory) {
    val room = objects.newInstance(RoomConfig::class.java)
    fun room(action: Action<RoomConfig>) = action.execute(room)

    val retrofit = objects.newInstance(RetrofitConfig::class.java)
    fun retrofit(action: Action<RetrofitConfig>) = action.execute(retrofit)

    val security = objects.newInstance(SimpleConfig::class.java)
    fun security(action: Action<SimpleConfig>) = action.execute(security)

    val dataStore = objects.newInstance(SimpleConfig::class.java)
    fun dataStore(action: Action<SimpleConfig>) = action.execute(dataStore)

    val workManager = objects.newInstance(SimpleConfig::class.java)
    fun workManager(action: Action<SimpleConfig>) = action.execute(workManager)
}

interface RoomConfig {
    val enabled: Property<Boolean>
    val usePaging: Property<Boolean>
}

interface RetrofitConfig {
    val enabled: Property<Boolean>
    val useChucker: Property<Boolean>
    val useKotlinSerialization: Property<Boolean>
}

interface SimpleConfig {
    val enabled: Property<Boolean>
}