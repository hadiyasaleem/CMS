package com.mbd.cmscommon.di

import com.mbd.cmscommon.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.plugins.HttpTimeout
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @OptIn(ExperimentalSerializationApi::class, SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
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
            scheme = "cms"
            host = "login-callback"
        }
        install(Postgrest)
        install(Storage)
        install(Realtime)
        install(Functions)
        // No timeout is installed by default, so a stalled connection (dead wifi, a server that
        // accepts the socket but never replies) hangs forever instead of failing fast -- the admin
        // bootstrap's institution-wide sync (many sequential paginated requests) is where this is
        // most visible: one hung request silently blocks every page after it until something
        // outside this client (e.g. an overall withTimeoutOrNull) finally gives up.
        httpConfig {
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
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
