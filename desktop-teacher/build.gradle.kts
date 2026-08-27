import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val supabaseUrl: String = localProps.getProperty("supabase.url") ?: System.getenv("SUPABASE_URL") ?: ""
val supabaseAnonKey: String = localProps.getProperty("supabase.anonKey") ?: System.getenv("SUPABASE_ANON_KEY") ?: ""

dependencies {
    implementation(project(":desktop-shared"))
}

compose.desktop {
    application {
        mainClass = "com.mbd.cmsdesktopteacher.MainKt"
        jvmArgs += listOf(
            "-Dcms.supabase.url=$supabaseUrl",
            "-Dcms.supabase.anonKey=$supabaseAnonKey",
            "-Dcms.desktop.appId=teacher",
        )

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "CMS Teacher Desktop"
            packageVersion = "1.0.1"

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                shortcut = true
                menu = true
                menuGroup = "CMS"
                upgradeUuid = "6e69ff5e-e424-5293-9c2a-56fac0fa03fb"
            }
        }
    }
}
