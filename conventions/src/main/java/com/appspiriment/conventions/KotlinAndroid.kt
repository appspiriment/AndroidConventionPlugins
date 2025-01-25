package com.appspiriment.conventions

import appspirimentLibRefs
import appspirimentTomlContents
import appspirimentTomlName
import com.android.build.api.dsl.CommonExtension
import com.android.tools.r8.internal.co
import libVersion
import org.gradle.api.Project
import org.gradle.internal.impldep.com.jcraft.jsch.ConfigRepository.defaultConfig
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

internal fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    version: ModuleVersionInfo,
    config:AppspirimentExtension
){
    commonExtension.run{
        configureKotlinAndroid(commonExtension = commonExtension, version = version)
        commonExtension.apply {
            buildFeatures {
                compose = config.enableCompose
            }
        }
        if (config.enableCompose) {
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    freeCompilerArgs.apply {
                        addAll(buildComposeMetricsParameters())
                    }
                }
            }
        }
        defaultConfig.apply {
            namespace = config.namespace
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = config.minifyRelease
                }
            }
        }
    }
}
private fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    version: ModuleVersionInfo
) {
    commonExtension.apply {
        compileSdk = projectConfigs.compileSdk

        defaultConfig.apply {
            minSdk = projectConfigs.minSdk
            buildConfigField("int", "VERSIONCODE", "${version.releaseCode}")

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables {
                useSupportLibrary = true
            }

            compileOptions {
                sourceCompatibility = projectConfigs.javaVersion
                targetCompatibility = projectConfigs.javaVersion
            }

            val proguardFile = "proguard-rules.pro"
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        proguardFile
                    )
                }
                getByName("debug") {
                    isMinifyEnabled = false
                }
            }
        }

        buildFeatures {
            buildConfig = true
        }
        //configureFlavors(this)
        //configureGradleManagedDevices(this)
        packaging {
            resources {
                resources.excludes.add("META-INF/*")
            }
        }

        compileOptions {

            sourceCompatibility = projectConfigs.javaVersion
            targetCompatibility = projectConfigs.javaVersion
            isCoreLibraryDesugaringEnabled = false
        }

    }


    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(projectConfigs.javaVersion.toString()))
            val warningsAsErrors: String? by project
            allWarningsAsErrors = warningsAsErrors.toBoolean()
            freeCompilerArgs.apply {
                addAll(
                    listOf(
                        "-opt-in=kotlin.RequiresOptIn",
                        // Enable experimental coroutines APIs, including Flow
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                        "-opt-in=kotlinx.coroutines.FlowPreview",
                    )
                )
            }
        }
    }
}

internal fun getVersionCodes(): ModuleVersionInfo {
    val propsFile = File("version.properties").apply {
        if (!exists()) writeText(
            "MAJOR=1\nPATCH=0\nREVISION=0\nRELEASE_REVISION=0"
        )
    }

    if (propsFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        val major = props["MAJOR"].toString().padStart(2, '0')
        var patch = props["PATCH"].toString().padStart(2, '0')
        val revisionStr = props["REVISION"].toString()
        val releaseRevisionStr = props["RELEASE_REVISION"].toString()
        val revision = revisionStr.toInt().let {
            if (it >= 9999) 0.also {
                patch = "${patch.toInt() + 1}"
            } else it
        }.toString().padStart(revisionStr.length, '0')

        val verCode = "$major$patch$revision".toInt()
        val releaseVerCode = "$major$patch$releaseRevisionStr".toInt()
        val (debugVerName, releaseVerName) = "$major.$patch".let {
            Pair("$it.$revision", "$it.0")
        }

        return ModuleVersionInfo(
            debugCode = verCode,
            debugName = debugVerName,
            releaseCode = releaseVerCode,
            releaseName = releaseVerName
        ).also {
            propsFile.writeText("MAJOR=$major\nPATCH=$patch\nREVISION=${revision.toInt() + 1}\nRELEASE_REVISION=$releaseRevisionStr")
        }
    } else {
        throw FileNotFoundException("Could not read version.properties!")
    }
}

internal fun Project.copyAppspirimentLibs() {
    File(rootDir.path + "/gradle/$appspirimentTomlName.versions.toml").run {
        if (
            !exists() ||
            readLines().firstOrNull { it.contains("appspiriment =") }?.contains("\"$libVersion\"") != true
        ) {
            writeText(appspirimentTomlContents)
            if(appspirimentTomlContents != "libs"){
                File(rootDir.path + "/gradle/libs.versions.toml").let{ libFile ->
                    libFile.readLines().toMutableList().apply {
                        appspirimentLibRefs.run {
                            versions.plus(plugins).forEach {
                                removeIf { line -> line.contains("\"$it\"") }
                            }
                            libraries.forEach {
                                removeIf { line -> line.contains("\"${it.first}\"") && line.contains("\"${it.second}\"") }
                            }
                        }
                        libFile.writeText(joinToString("\n"))
                    }
                }
            }
        }
    }
}


private fun Project.buildComposeMetricsParameters(): List<String> {
    val metricParameters = mutableListOf<String>()
    val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
    val enableMetrics = (enableMetricsProvider.orNull == "true")
    if (enableMetrics) {
        val metricsFolder = File(project.buildDir, "compose-metrics")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
        )
    }

    val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
    val enableReports = (enableReportsProvider.orNull == "true")
    if (enableReports) {
        val reportsFolder = File(project.buildDir, "compose-reports")
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
        )
    }
    return metricParameters.toList()
}


data class ModuleVersionInfo(
    val debugCode: Int,
    val debugName: String,
    val releaseCode: Int,
    val releaseName: String
)