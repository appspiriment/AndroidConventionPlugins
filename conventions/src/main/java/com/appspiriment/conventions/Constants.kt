import com.appspiriment.conventions.AppspirimentLibRef
import com.appspiriment.conventions.PluginRefs
import com.appspiriment.conventions.VersionRefs

internal const val appspirimentTomlName = "appspirimentlibs"

internal const val libVersion = "0.0.3.dev-61"

internal const val appspirimentTomlContents = "# This Version Catalog serves the Appspiriment Plugins\n# Please be careful in changing this as removing any may cause the plugin to fail\n# The versions can be updated but be careful about the compatibility\n\n[versions]\n#App Versions\nminSdk = \"26\"\ntargetSdk = \"35\"\ncompileSdk = \"35\"\njavaVersion = \"21\"\n\nappspiriment = \"0.0.3.dev-61\"\nappspirimentUtils = \"0.0.2\"\n\n#Classpath Version\nagp = \"8.4.2\"\nkotlin = \"2.0.0\"\nksp = \"2.0.0-1.0.21\"\nhilt = \"2.49\"\nkotlinserializeplugin = \"2.0.0\"\n#Classpath Version End\n\ncoreKtx = \"1.15.0\"\nroom = \"2.6.1\"\nmultidex = \"2.0.1\"\nkotlinserialize = \"1.6.3\"\nkotlinxCoroutines = \"1.7.3\"\n\ngoogleServices = \"4.4.2\"\n\n#Android Compose  Dep Version\nlifecycleRuntimeKtx = \"2.8.7\"\nactivityCompose = \"1.9.3\"\ncomposeBom = \"2024.12.01\"\ncomposeNavigation = \"2.8.5\"\ncomposeHiltNavigation = \"1.2.0\"\ncomposeMaterial3 = \"1.3.1\"\nmaterial3 = \"1.7.6\"\nlifecycle = \"2.8.7\"\n\n#Tools\nlottie = \"6.0.0\"\n\n#Test Versions\njunit = \"4.13.2\"\njunitVersion = \"1.2.1\"\nespressoCore = \"3.6.1\"\n\nfirebasecrashlyticsplugin = \"3.0.2\"\nuiTextGoogleFonts = \"1.7.6\"\n\n[libraries]\nandroidx-core-ktx = { group = \"androidx.core\", name = \"core-ktx\", version.ref = \"coreKtx\" }\nandroidx-lifecycle-runtime-ktx = { group = \"androidx.lifecycle\", name = \"lifecycle-runtime-ktx\", version.ref = \"lifecycleRuntimeKtx\" }\nkotlinx-coroutines-android = { group = \"org.jetbrains.kotlinx\", name = \"kotlinx-coroutines-android\", version.ref = \"kotlinxCoroutines\" }\nmultidex = { module = \"androidx.multidex:multidex\", version.ref = \"multidex\" }\n\n\n#Compose Dependecies\nandroidx-activity-compose = { group = \"androidx.activity\", name = \"activity-compose\", version.ref = \"activityCompose\" }\nandroidx-compose-bom = { group = \"androidx.compose\", name = \"compose-bom\", version.ref = \"composeBom\" }\nandroidx-ui = { group = \"androidx.compose.ui\", name = \"ui\" }\nandroidx-ui-graphics = { group = \"androidx.compose.ui\", name = \"ui-graphics\" }\nandroidx-ui-tooling = { group = \"androidx.compose.ui\", name = \"ui-tooling\" }\nandroidx-ui-tooling-preview = { group = \"androidx.compose.ui\", name = \"ui-tooling-preview\" }\nandroidx-material3 = { group = \"androidx.compose.material3\", name = \"material3\", version.ref = \"composeMaterial3\" }\nandroidx-compose-material-iconsExtended = { group = \"androidx.compose.material\", name = \"material-icons-extended\", version.ref = \"composeMaterial3\" }\nandroidx-compose-navigation = { group = \"androidx.navigation\", name = \"navigation-compose\", version.ref = \"composeNavigation\" }\nandroidx-lifecycle-viewModelCompose = { group = \"androidx.lifecycle\", name = \"lifecycle-viewmodel-compose\", version.ref = \"lifecycle\" }\nandroidx-lifecycle-runtimeCompose = { group = \"androidx.lifecycle\", name = \"lifecycle-runtime-compose\", version.ref = \"lifecycle\" }\nkotlinx-serialize = { module = \"org.jetbrains.kotlinx:kotlinx-serialization-json\", version.ref = \"kotlinserialize\" }\n\nandroidx-ui-text-google-fonts = { group = \"androidx.compose.ui\", name = \"ui-text-google-fonts\", version.ref = \"uiTextGoogleFonts\" }\n\n#Hilt Dependencies\nhilt-android = { group = \"com.google.dagger\", name = \"hilt-android\", version.ref = \"hilt\" }\nhilt-compiler = { group = \"com.google.dagger\", name = \"hilt-android-compiler\", version.ref = \"hilt\" }\nhilt-compose-navigation = { group = \"androidx.hilt\", name = \"hilt-navigation-compose\", version.ref = \"composeHiltNavigation\" }\n\n#Room\nroom-ktx = { group = \"androidx.room\", name = \"room-ktx\", version.ref = \"room\" }\nroom-runtime = { group = \"androidx.room\", name = \"room-runtime\", version.ref = \"room\" }\nroom-compiler = { group = \"androidx.room\", name = \"room-compiler\", version.ref = \"room\" }\nroom-common = { group = \"androidx.room\", name = \"room-common\", version.ref = \"room\" }\n\n#Lottie\nlottie = { module = \"com.airbnb.android:lottie\", version.ref = \"lottie\" }\nlottie-compose = { module = \"com.airbnb.android:lottie-compose\", version.ref = \"lottie\" }\n\n#Utils\nappspiriment-utils-dev = { group = \"io.github.appspiriment\", name = \"utils-dev\", version.ref=\"appspirimentUtils\" }\nappspiriment-utils-prod = { group = \"io.github.appspiriment\", name = \"utils-prod\", version.ref=\"appspirimentUtils\" }\n\n#Test Dependencies\nandroidx-junit = { group = \"androidx.test.ext\", name = \"junit\", version.ref = \"junitVersion\" }\njunit-test = { group = \"junit\", name = \"junit\", version.ref = \"junit\" }\nandroidx-espresso-core = { group = \"androidx.test.espresso\", name = \"espresso-core\", version.ref = \"espressoCore\" }\nandroidx-ui-test-junit4 = { group = \"androidx.compose.ui\", name = \"ui-test-junit4\"}\nandroidx-ui-test-manifest = { group = \"androidx.compose.ui\", name = \"ui-test-manifest\" }\n\n[bundles]\nandroid-base = [\n    \"androidx-core-ktx\",\n    \"androidx-lifecycle-runtime-ktx\",\n    \"kotlinx-coroutines-android\",\n    \"multidex\"\n]\nandroid-compose = [\n    \"androidx-activity-compose\",\n    \"androidx-ui\",\n    \"androidx-ui-graphics\",\n    \"androidx-material3\",\n    \"androidx-compose-material-iconsExtended\",\n    \"androidx-compose-navigation\",\n    \"androidx-lifecycle-viewModelCompose\",\n    \"androidx-lifecycle-runtimeCompose\",\n    \"androidx-ui-text-google-fonts\",\n    \"kotlinx-serialize\"\n]\n\nandroid-tooling = [\n    \"androidx-ui-tooling\",\n    \"androidx-ui-tooling-preview\",\n]\n\nandroid-test = [\n    \"androidx-junit\",\n    \"junit-test\",\n    \"androidx-espresso-core\",\n]\n\ncompose-test = [\n    \"androidx-ui-test-junit4\",\n    \"androidx-ui-test-manifest\",\n]\n\n[plugins]\ngoogle-android-application = { id = \"com.android.application\", version.ref = \"agp\" }\ngoogle-android-library = { id = \"com.android.library\", version.ref = \"agp\" }\nkotlin-android = { id = \"org.jetbrains.kotlin.android\", version.ref = \"kotlin\" }\nkotlin-compose = { id = \"org.jetbrains.kotlin.plugin.compose\", version.ref = \"kotlin\" }\ndagger-hilt-android = { id = \"com.google.dagger.hilt.android\", version.ref = \"hilt\" }\nkotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }\ndevtools-ksp = { id = \"com.google.devtools.ksp\", version.ref = \"ksp\" }\ngoogle-services = { id = \"com.google.gms.google-services\", version.ref = \"googleServices\" }\n\nkotlinx-serialization = { id = \"org.jetbrains.kotlin.plugin.serialization\", version.ref = \"kotlinserializeplugin\" }\nkotlin-parcelize = { id = \"org.jetbrains.kotlin.plugin.parcelize\", version.ref = \"kotlin\" }\nfirebase-crashlytics = { id = \"com.google.firebase.crashlytics\", version.ref = \"firebasecrashlyticsplugin\" }\n\n#Custom Project Plugins\nappspiriment-project = { id = \"io.github.appspiriment.project\", version.ref = \"appspiriment\" }\nappspiriment-application = { id = \"io.github.appspiriment.application\", version.ref = \"appspiriment\" }\nappspiriment-library = { id = \"io.github.appspiriment.library\", version.ref = \"appspiriment\" }\nappspiriment-room = { id = \"io.github.appspiriment.room\", version.ref = \"appspiriment\" }\nappspiriment-publish = { id = \"io.github.appspiriment.mavenpublish\", version.ref = \"appspiriment\" }"

internal fun getDefaultAppGradle(appId: String) = "plugins {\n    alias(appspirimentlibs.plugins.appspiriment.application)\n}\nandroidApplication {\n    appId = \"$appId\"\n}"

internal val appspirimentLibRefs = AppspirimentLibRef(
    versions= listOf(
        "minSdk",
        "targetSdk",
        "compileSdk",
        "javaVersion",
        "appspiriment",
        "appspirimentUtils",
        "agp",
        "kotlin",
        "ksp",
        "hilt",
        "kotlinserializeplugin",
        "coreKtx",
        "room",
        "multidex",
        "kotlinserialize",
        "kotlinxCoroutines",
        "googleServices",
        "lifecycleRuntimeKtx",
        "activityCompose",
        "composeBom",
        "composeNavigation",
        "composeHiltNavigation",
        "composeMaterial3",
        "material3",
        "lifecycle",
        "lottie",
        "junit",
        "junitVersion",
        "espressoCore",
        "firebasecrashlyticsplugin",
        "uiTextGoogleFonts"),
    plugins= listOf(
        "com.android.application",
        "com.android.library",
        "org.jetbrains.kotlin.android",
        "org.jetbrains.kotlin.plugin.compose",
        "com.google.dagger.hilt.android",
        "org.jetbrains.kotlin.jvm",
        "com.google.devtools.ksp",
        "com.google.gms.google-services",
        "org.jetbrains.kotlin.plugin.serialization",
        "org.jetbrains.kotlin.plugin.parcelize",
        "com.google.firebase.crashlytics",
        "io.github.appspiriment.project",
        "io.github.appspiriment.application",
        "io.github.appspiriment.library",
        "io.github.appspiriment.room"),
    libraries= listOf(
        Pair("androidx.core", "core-ktx"),
        Pair("androidx.lifecycle", "lifecycle-runtime-ktx"),
        Pair("org.jetbrains.kotlinx", "kotlinx-coroutines-android"),
        Pair("androidx.activity", "activity-compose"),
        Pair("androidx.compose", "compose-bom"),
        Pair("androidx.compose.ui", "ui"),
        Pair("androidx.compose.ui", "ui-graphics"),
        Pair("androidx.compose.ui", "ui-tooling"),
        Pair("androidx.compose.ui", "ui-tooling-preview"),
        Pair("androidx.compose.material3", "material3"),
        Pair("androidx.compose.material", "material-icons-extended"),
        Pair("androidx.navigation", "navigation-compose"),
        Pair("androidx.lifecycle", "lifecycle-viewmodel-compose"),
        Pair("androidx.lifecycle", "lifecycle-runtime-compose"),
        Pair("androidx.compose.ui", "ui-text-google-fonts"),
        Pair("com.google.dagger", "hilt-android"),
        Pair("com.google.dagger", "hilt-android-compiler"),
        Pair("androidx.hilt", "hilt-navigation-compose"),
        Pair("androidx.room", "room-ktx"),
        Pair("androidx.room", "room-runtime"),
        Pair("androidx.room", "room-compiler"),
        Pair("androidx.room", "room-common"),
        Pair("io.github.appspiriment", "utils-dev"),
        Pair("io.github.appspiriment", "utils-prod"),
        Pair("androidx.test.ext", "junit"),
        Pair("junit", "junit"),
        Pair("androidx.test.espresso", "espresso-core"),
        Pair("androidx.compose.ui", "ui-test-junit4"),
        Pair("androidx.compose.ui", "ui-test-manifest"))
)