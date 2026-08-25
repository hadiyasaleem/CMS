import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
}

// Same local.properties/env-var source as :app/build.gradle.kts, read at Gradle config time.
// Passed via the compose application's jvmArgs (not just the run task) so the packaged MSI carries
// them too — DesktopSupabaseModule reads these as system properties at startup.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val supabaseUrl: String = localProps.getProperty("supabase.url") ?: System.getenv("SUPABASE_URL") ?: ""
val supabaseAnonKey: String = localProps.getProperty("supabase.anonKey") ?: System.getenv("SUPABASE_ANON_KEY") ?: ""

dependencies {
    // Everything (DI, repos, theme/components, shared screens, AdminNavHost) lives in the shared
    // cmsdesktop library module — this app module is just its own launcher + role-locked login.
    implementation(project(":desktop-shared"))
}

compose.desktop {
    application {
        mainClass = "com.mbd.cmsdesktopadmin.MainKt"
        jvmArgs += listOf(
            "-Dcms.supabase.url=$supabaseUrl",
            "-Dcms.supabase.anonKey=$supabaseAnonKey",
            "-Dcms.desktop.appId=admin",
        )

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "CMS Admin Desktop"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                shortcut = true
                menu = true
                menuGroup = "CMS"
                upgradeUuid = "5d58ff4d-d313-4182-9b19-45fbb1f892ea"
            }
        }
    }
}
