package com.paperscreen.android.dictionary.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "dictionary_entries",
    foreignKeys = [
        ForeignKey(
            entity = DictionaryPackageEntity::class,
            parentColumns = ["languageCode"],
            childColumns = ["languageCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["languageCode", "word"])
    ]
)
data class DictionaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val languageCode: String,
    val word: String,
    val partOfSpeech: String?,
    val definition: String,
    val pronunciation: String?,
    val example: String?
)
