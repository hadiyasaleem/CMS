package com.mbd.cmscommon.di

import com.mbd.cmscommon.util.LogSink
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Accessor for [LogSink] from each app's Application.onCreate(), used instead of `@Inject
 * lateinit var` field injection into the Application class itself. This project's Dagger/Kotlin
 * toolchain combination fails hiltJavaCompile with "Unable to read Kotlin metadata due to
 * unsupported metadata version" for field-injected sites specifically (a Dagger/kotlinx-metadata
 * validation path that no other class in this codebase exercises) — EntryPointAccessors sidesteps
 * it entirely since it resolves through the generated component's provider methods instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LogSinkEntryPoint {
    fun logSink(): LogSink
}
