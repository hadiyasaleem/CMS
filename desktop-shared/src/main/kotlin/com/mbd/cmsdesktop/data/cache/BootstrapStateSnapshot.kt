package com.mbd.cmsdesktop.data.cache

import kotlinx.serialization.Serializable

@Serializable
data class BootstrapStateSnapshot(
    val completedKeys: Set<String> = emptySet(),
)
