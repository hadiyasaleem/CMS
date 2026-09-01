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
        // Room database and uses the JVM bundled SQLite driver instead. CmsDatabase.kt itself has
        // no Android-specific dependency (just the abstract DAO accessor list + version const, with
        // CMS_DATABASE_MIGRATIONS living in the excluded CmsDatabaseMigrations.kt instead), so it is
        // reused as-is: DesktopDatabase extends it instead of redeclaring its DAO accessors.
        // NOTE: kotlin.exclude filters compileKotlin but NOT the kspKotlin task, so any excluded
        // file here is still fed to KSP (Dagger/Room). Only exclude files that KSP can process
        // without error. NotificationRepositoryImpl is reused as-is (its DataStore dependency is
        // supplied below) rather than excluded, because excluding it left Dagger's
        // InjectProcessingStep generating a factory for a file compileKotlin never compiled.
        kotlin.exclude(
            "CmsDatabaseMigrations.kt",
            "Converters.kt",
            "**/CmsDatabaseMigrations.kt",
            "**/Converters.kt",
            // Desktop keeps its identical bootstrapper so the reused mobile source root does not
            // define it twice. This pattern is intentionally root-anchored (no "**/"): the mobile
            // copy sits at the root of the added data/sync srcDir, while the desktop copy is nested
            // under com/mbd/cmscommon/data/sync in src/main/kotlin. A "**/AdminDataBootstrapper.kt"
            // pattern would match at any depth and wrongly exclude the desktop copy too.
            "AdminDataBootstrapper.kt",
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

    // The reused mobile NotificationRepositoryImpl persists the notifications "last viewed" marker
    // via DataStore<Preferences>. Desktop binds DesktopNotificationRepository instead, so this is
    // only needed so the reused file (and the Dagger factory KSP generates for it) resolves the
    // DataStore type. datastore-preferences-core is the pure-JVM/KMP core (no Android runtime).
    implementation("androidx.datastore:datastore-preferences-core:1.1.1")

    testImplementation(libs.junit)
}
