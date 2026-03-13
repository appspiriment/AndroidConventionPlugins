package com.appspiriment.conventions.extensions

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project
import kotlin.jvm.optionals.getOrElse

/**
 * Safely retrieves a version from the catalog or throws a descriptive error.
 */
internal fun VersionCatalog.getVersion(alias: String): String {
    return findVersion(alias).getOrElse {
        throw IllegalStateException("Version alias '$alias' not found in catalog '${this.name}'")
    }.toString()
}

/**
 * Applies plugins from the catalog. Throws if any plugin is missing.
 */
internal fun PluginManager.applyPluginFromLibs(vararg pluginGroups: Pair<VersionCatalog, List<String>>) {
    pluginGroups.forEach { (catalog, aliases) ->
        aliases.forEach { alias ->
            val plugin = catalog.findPlugin(alias).orElseThrow {
                IllegalStateException("Plugin alias '$alias' not found in Version Catalog")
            }.get()

            // Gradle's PluginManager.hasPlugin is efficient,
            // but we can just use 'apply' directly for most built-in plugins.
            // However, for KSP/Hilt, checking first prevents 're-configuration' overhead.
            if (!hasPlugin(plugin.pluginId)) {
                apply(plugin.pluginId)
            }
        }
    }
}

/**
 * Applies a list of dependencies using the catalog.
 */
internal fun DependencyHandlerScope.implementDependency(
    libs: VersionCatalog,
    dependencyList: List<Dependency>
) {
    dependencyList.forEach { dep ->
        when (dep.type) {
            ImplType.BUNDLE -> implement(libs, dep.config, dep.aliases, isBundle = true)
            ImplType.DEPENDENCY -> implement(libs, dep.config, dep.aliases)
            ImplType.PROJECT -> dep.aliases.forEach { alias ->
                add(dep.config, project(alias))
            }
            ImplType.PLATFORM -> implement(libs, dep.config, dep.aliases, isPlatform = true)
        }
    }
}

private fun DependencyHandlerScope.implement(
    libs: VersionCatalog,
    config: String,
    aliases: List<String>,
    isBundle: Boolean = false,
    isPlatform: Boolean = false
) {
    aliases.forEach { alias ->
        libs.run {
            if (isBundle) {
                findBundle(alias).getOrElse {
                    throw IllegalStateException("Bundle '$alias' not found in catalog '${name}'")
                }?.let { add(config, it) }
            } else {
                findLibrary(alias).getOrElse {
                    throw IllegalStateException("Library '$alias' not found in catalog '${name}'")
                }?.let {
                    add(config, if (isPlatform) platform(it) else it)
                }
            }
        }
    }
}

data class Dependency(
    val type: ImplType = ImplType.DEPENDENCY,
    val config: String = IMPLEMENTATION_CONFIGURATION_NAME,
    val aliases: List<String>
)

enum class ImplType { BUNDLE, DEPENDENCY, PROJECT, PLATFORM }
