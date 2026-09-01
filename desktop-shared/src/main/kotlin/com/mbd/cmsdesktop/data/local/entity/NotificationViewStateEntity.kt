package com.mbd.cmsdesktop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The desktop notification-read marker is durable application data, kept in Room. */
@Entity(tableName = "notification_view_state")
data class NotificationViewStateEntity(
    @PrimaryKey val id: String = DEFAULT_ID,
    val lastViewedAt: Long,
) {
    companion object {
        const val DEFAULT_ID = "default"
    }
}
