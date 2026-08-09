package com.paperscreen.android.reader.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderDao {
    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY lastOpenedAt DESC")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY addedAt DESC LIMIT 10")
    fun getRecentlyAddedBooks(): Flow<List<BookEntity>>
    
    @Query("SELECT * FROM books WHERE progressPercentage > 0 AND progressPercentage < 1.0 ORDER BY lastOpenedAt DESC")
    fun getContinueReadingBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE progressPercentage = 1.0 ORDER BY lastOpenedAt DESC")
    fun getCompletedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY lastOpenedAt DESC LIMIT 1")
    fun getRecentBook(): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE uriString = :uriString")
    suspend fun getBookByUri(uriString: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getBookmarksForBook(bookId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    // Highlights
    @Query("SELECT * FROM highlights WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getHighlightsForBook(bookId: Long): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND positionIdentifier = :positionIdentifier")
    fun getHighlightsForPosition(bookId: Long, positionIdentifier: String): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity): Long

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)

    // Notes
    @Query("SELECT * FROM notes WHERE highlightId = :highlightId ORDER BY createdAt DESC")
    fun getNotesForHighlight(highlightId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
