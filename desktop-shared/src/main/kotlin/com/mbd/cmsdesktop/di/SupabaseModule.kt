package com.mbd.cmsdesktop.di

import com.mbd.cmsdesktop.auth.RoomAuthCodeVerifierCache
import com.mbd.cmsdesktop.auth.RoomAuthSessionManager
import com.mbd.cmsdesktop.data.local.DesktopDatabase
import dagger.Module
import dagger.Provides
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Desktop reads the Supabase URL/anon key from JVM system properties (`cms.supabase.url` /
 * `cms.supabase.anonKey`, falling back to `SUPABASE_URL`/`SUPABASE_ANON_KEY` env vars) — there's no
 * `BuildConfig` on a plain-JVM module. The Auth session/PKCE-verifier caches are keyed per
 * `cms.desktop.appId` so admin/teacher/student desktop apps don't clobber each other's login state
 * when run side by side on the same machine. The durable session and PKCE state are stored in Room.
 */
@Module
object SupabaseModule {

    private fun appId(): String {
        val property = System.getProperty("cms.desktop.appId").orEmpty()
        return property.ifBlank { "shared" }
    }

    private fun readConfig(): Pair<String, String> {
        val url = System.getProperty("cms.supabase.url").orEmpty()
            .ifBlank { System.getenv("SUPABASE_URL").orEmpty() }
        val key = System.getProperty("cms.supabase.anonKey").orEmpty()
            .ifBlank { System.getenv("SUPABASE_ANON_KEY").orEmpty() }
        check(url.isNotBlank() && key.isNotBlank()) {
            "Missing Supabase URL/anon key — set supabase.url/supabase.anonKey in local.properties or SUPABASE_URL/SUPABASE_ANON_KEY env vars"
        }
        return url to key
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(database: DesktopDatabase): SupabaseClient {
        val (url, key) = readConfig()
        return createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            defaultSerializer = KotlinXSerializer(
                Json {
                    // DB columns are snake_case (dept_id, hod_email, …) while every DTO property is
                    // camelCase and carries no @SerialName; without this strategy every multi-word
                    // column silently deserializes to null (e.g. Department.deptId == "").
                    namingStrategy = JsonNamingStrategy.SnakeCase
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                    explicitNulls = false
                },
            )
            install(Auth) {
                val appId = this@SupabaseModule.appId()
                sessionManager = RoomAuthSessionManager(database.desktopAuthSessionDao())
                codeVerifierCache = RoomAuthCodeVerifierCache(database.desktopAuthCodeVerifierDao())
            }
            install(Postgrest)
            install(Storage)
            install(Functions)
        }
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
