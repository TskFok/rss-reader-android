package com.example.rssreader.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rssreader.core.model.Feed

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val title: String,
    val url: String,
    val lastFetchedAt: String?,
    val createdAt: String,
) {
    fun toModel(): Feed = Feed(
        id = id,
        userId = userId,
        categoryId = null,
        url = url,
        title = title,
        lastFetchedAt = lastFetchedAt,
        createdAt = createdAt,
    )

    companion object {
        fun fromModel(feed: Feed): FeedEntity = FeedEntity(
            id = feed.id,
            userId = feed.userId,
            title = feed.title,
            url = feed.url,
            lastFetchedAt = feed.lastFetchedAt,
            createdAt = feed.createdAt,
        )
    }
}
