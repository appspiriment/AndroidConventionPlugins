package com.appspiriment.conventions.extensions

/**
 * Holds parsed references from the appspirimentlibs version catalog.
 * Used by the code generator task to embed catalog metadata into Constants.kt.
 */
data class AppspirimentLibRef(
    val versions: List<String>,
    val plugins: List<String> = emptyList(),
    val libraries: List<Pair<String, String>> = emptyList(),
    val bundles: List<String> = emptyList(),
)
