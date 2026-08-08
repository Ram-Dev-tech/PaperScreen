package com.paperscreen.android.reader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val position: String,
    val label: String,
    val createdAt: Long = System.currentTimeMillis()
)
