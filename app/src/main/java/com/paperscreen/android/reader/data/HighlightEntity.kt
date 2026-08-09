package com.paperscreen.android.reader.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val fileType: String,
    val positionIdentifier: String, // TXT offset string or EPUB Locator JSON
    val startIndex: Int, // char start index in the chunk/chapter
    val endIndex: Int, // char end index
    val selectedText: String,
    val createdAt: Long = System.currentTimeMillis()
)
