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

dependencies {
    api(project(":core"))

    api(compose.desktop.currentOs)
    api(compose.material3)
    api(compose.materialIconsExtended)

    api(libs.supabase.postgrest)
    api(libs.supabase.storage)

    api(libs.dagger)
    ksp(libs.dagger.compiler)

    api(libs.datastore.preferences)
    api(libs.multiplatform.settings)
    api(libs.multiplatform.settings.no.arg)

    api(libs.pdfbox)

    testImplementation(libs.junit)
}
