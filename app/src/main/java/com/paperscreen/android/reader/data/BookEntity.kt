package com.paperscreen.android.reader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val title: String,
    val author: String?,
    val fileType: String,
    val coverUriString: String?,
    val isFavorite: Boolean = false,
    val lastOpenedAt: Long = 0,
    val addedAt: Long = System.currentTimeMillis(),
    
    // Progress
    val currentPosition: String = "",
    val progressPercentage: Float = 0f
)
