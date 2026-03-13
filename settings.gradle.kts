pluginManagement {
    repositories {

        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        mavenLocal()
    }

}
dependencyResolutionManagement {
    versionCatalogs {
        create("appspirimentlibs") {
            from(files("gradle/appspirimentlibs.versions.toml"))
        }
        create("kmp") {
            from(files("gradle/kmp.versions.toml"))
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Android Convention Plugins"
include(":conventions")
include(":resources")
