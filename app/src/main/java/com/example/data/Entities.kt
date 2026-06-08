package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val name: String,
    val password: String,
    val avatarUrl: String,
    val isDeveloper: Boolean = false,
    val isBanned: Boolean = false,
    val isBannedFromChat: Boolean = false,
    val isBannedFromWriting: Boolean = false,
    val warningMessage: String = "",
    val googleEmail: String = ""
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // IMAGES, NOVELS, VIDEOS, GAMES, NEWS, MODELS
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val downloadUrl: String = "",
    val videoUrl: String = "",
    val chaptersJson: String = "[]", // Serialized list of chapters (backwards compatibility)
    val externalLink: String = "",
    val publisher: String,
    val publisherName: String,
    val publisherAvatar: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isExclusive: Boolean = false,
    val likesCount: Int = 0
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val chapterNumber: Int,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // e.g. "حساب", "دخول", "حذف", "إضافة", "حظر", "تحذير"
    val user: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val itemId: Int
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val username: String,
    val userDisplay: String,
    val userAvatar: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Chat room for specific category
    val username: String,
    val userDisplay: String,
    val userAvatar: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)
