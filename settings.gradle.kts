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

rootProject.name = "TalkLens"
include(":app")

// Core modules
include(":core:common")
include(":core:designsystem")
include(":core:navigation")
include(":core:model")

// Data modules
include(":data")

// Domain modules
include(":domain")

// Feature modules
include(":feature:setup")
include(":feature:camera")
include(":feature:gallery")
include(":feature:settings")
include(":feature:translation")
