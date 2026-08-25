plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    api(platform(libs.supabase.bom))
    api(libs.supabase.auth)
    api(libs.supabase.functions)
    api(libs.ktor.client.cio)

    api(libs.javax.inject)

    testImplementation(libs.junit)
}
