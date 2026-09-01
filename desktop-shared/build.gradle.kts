plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(17)
}

// Reuse the platform-neutral Room model and Room-backed repositories from the mobile source
// tree. This keeps desktop and mobile on one schema instead of maintaining separate file caches.
sourceSets {
    named("main") {
        kotlin.srcDirs(
            "../mobile-shared/src/main/java/com/mbd/cmscommon/auth",
            "../mobile-shared/src/main/java/com/mbd/cmscommon/data/local",
            "../mobile-shared/src/main/java/com/mbd/cmscommon/data/mapper",
            "../mobile-shared/src/main/java/com/mbd/cmscommon/data/repository",
            "../mobile-shared/src/main/java/com/mbd/cmscommon/data/sync",
        )
        // Android migration classes target Android's SQLite API. Desktop starts with its own
        // Room database and uses the JVM bundled SQLite driver instead.
        kotlin.exclude(
            "CmsDatabase.kt",
            "CmsDatabaseMigrations.kt",
            "Converters.kt",
            "NotificationRepositoryImpl.kt",
            "AdminDataBootstrapper.kt",
            "**/CmsDatabase.kt",
            "**/CmsDatabaseMigrations.kt",
            "**/Converters.kt",
            "**/NotificationRepositoryImpl.kt",
            // Desktop keeps its identical bootstrapper so this source root does not define it twice.
            "**/AdminDataBootstrapper.kt",
        )
    }
}

dependencies {
    api(project(":core"))

    api(compose.desktop.currentOs)
    api(compose.material3)
    api(compose.materialIconsExtended)

    api(libs.supabase.postgrest)
    api(libs.supabase.storage)

    api(libs.dagger)
    ksp(libs.dagger.compiler)

    api(libs.pdfbox)

    api(libs.desktop.room.runtime)
    api(libs.desktop.sqlite.bundled)
    ksp(libs.desktop.room.compiler)

    testImplementation(libs.junit)
}
