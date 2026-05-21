package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val userName: String,
    val userAvatar: String = "",
    val title: String,
    val text: String,
    val imageUrl: String = "", // Local image resource name, static uri, or general URL
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val category: String = "Geral" // e.g., "Culto", "Evento", "Testemunho", "Geral"
)
