package com.paperscreen.android.dictionary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_packages")
data class DictionaryPackageEntity(
    @PrimaryKey val languageCode: String,
    val displayName: String,
    val installedAt: Long
)
