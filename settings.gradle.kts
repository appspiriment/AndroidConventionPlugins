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
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AppsUtils"
include(":conventions")
