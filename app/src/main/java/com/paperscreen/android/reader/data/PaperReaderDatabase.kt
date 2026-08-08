package com.paperscreen.android.reader.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BookEntity::class, BookmarkEntity::class], version = 1, exportSchema = false)
abstract class PaperReaderDatabase : RoomDatabase() {
    abstract fun readerDao(): ReaderDao

    companion object {
        @Volatile
        private var INSTANCE: PaperReaderDatabase? = null

        fun getDatabase(context: Context): PaperReaderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaperReaderDatabase::class.java,
                    "paper_reader_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
