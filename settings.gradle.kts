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
//    versionCatalogs {
//        create("requiredlibs") {
//            from(files("gradle/requiredlibs.versions.toml"))
//        }
//    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

//
//plugins{
//    id("com.appspiriment.settings") version "0.1.6"
//}
rootProject.name = "AppsUtils"
include(":conventions")
include(":utils")
