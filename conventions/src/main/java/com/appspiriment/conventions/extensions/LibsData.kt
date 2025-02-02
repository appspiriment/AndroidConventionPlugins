package com.appspiriment.conventions.extensions

data class AppspirimentLibRef(
    val versions: List<String>,
    val plugins: List<String> = emptyList(),
    val libraries: List<Pair<String,String>> = emptyList(),
    val bundles: List<String> = emptyList(),
)

data class VersionRefs(val name: String, val value: String)
data class LibraryRefs(val name: String, val group: String, val artifact: String, val version: String)
data class BundleRefs(val name: String, val libs: List<String>)
data class PluginRefs(val name: String, val id: String, val version: String)