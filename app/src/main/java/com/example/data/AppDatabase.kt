package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Int

    // Items (Games, Novels, Videos, Drawings, News, 3D Models)
    @Query("SELECT * FROM items ORDER BY timestamp DESC")
    fun getAllItemsFlow(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE category = :category ORDER BY timestamp DESC")
    fun getItemsByCategoryFlow(category: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    // Favorites
    @Query("SELECT * FROM favorites WHERE username = :username")
    fun getFavoritesOfUserFlow(username: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE username = :username AND itemId = :itemId")
    suspend fun removeFavorite(username: String, itemId: Int)

    @Query("SELECT COUNT(*) FROM favorites WHERE username = :username AND itemId = :itemId")
    suspend fun isFavorite(username: String, itemId: Int): Int

    // Comments
    @Query("SELECT * FROM comments WHERE itemId = :itemId ORDER BY timestamp ASC")
    fun getCommentsForItemFlow(itemId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)

    // Category Chat Messages
    @Query("SELECT * FROM chat_messages WHERE category = :category ORDER BY timestamp DESC LIMIT 50")
    fun getChatMessagesByCategoryFlow(category: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    // Users Moderation & Member Lists
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<UserEntity>

    // Chapters Management
    @Query("SELECT * FROM chapters WHERE itemId = :itemId ORDER BY chapterNumber ASC")
    fun getChaptersForItemFlow(itemId: Int): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapter(chapterId: Int)

    // Auditing Logs
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 150")
    fun getAllLogsFlow(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)
}

@Database(
    entities = [
        UserEntity::class,
        ItemEntity::class,
        FavoriteEntity::class,
        CommentEntity::class,
        ChatMessageEntity::class,
        ChapterEntity::class,
        LogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
