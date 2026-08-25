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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CMS"

// ── Libraries ──
include(":core")            // plain-JVM portable subset (domain, DTOs, direct-Postgrest repos, auth)
include(":mobile-shared")   // Android library: Room, Hilt, Android UI kit — consumed by the 3 mobile apps
include(":desktop-shared")  // Compose-Desktop library: DI, desktop repos, UI kit — consumed by the 3 desktop apps

// ── Android apps ──
include(":mobile-admin")
include(":mobile-teacher")
include(":mobile-student")

// ── Desktop apps ──
include(":desktop-admin")
include(":desktop-teacher")
include(":desktop-student")
