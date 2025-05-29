pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GeoMarker"
include(":app")
include(":core-map")
include(":feature-geomarker")
include(":core-permissions")
include(":core-location")
include(":feature-addmarker")
include(":core-extensions")
include(":core-database")
include(":core-domain")
include(":feature-bottomsheet-navigation")
include(":feature-listmarkers")
include(":feature-detailsmarker")
