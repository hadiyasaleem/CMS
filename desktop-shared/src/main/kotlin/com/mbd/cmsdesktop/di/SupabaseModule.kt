package com.mbd.cmsdesktop.di

import dagger.Module
import dagger.Provides
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.cio.CIO
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * Desktop reads the Supabase URL/anon key from JVM system properties (`cms.supabase.url` /
 * `cms.supabase.anonKey`), set via each app's `compose.desktop.application.jvmArgs` at build time —
 * there's no `BuildConfig` on a plain-JVM module. No `Realtime` plugin: `realtime-kt-jvm` isn't
 * available in this offline build environment and nothing here uses live subscriptions (desktop is
 * "always-online, explicit `sync()`" by design, see [[cmsdesktop-project]]).
 */
@Module
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = System.getProperty("cms.supabase.url").orEmpty(),
        supabaseKey = System.getProperty("cms.supabase.anonKey").orEmpty(),
    ) {
        httpEngine = CIO.create()
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
                explicitNulls = false
            },
        )
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Functions)
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth = client.auth

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage = client.storage

    @Provides
    @Singleton
    fun provideFunctions(client: SupabaseClient): Functions = client.functions
}
