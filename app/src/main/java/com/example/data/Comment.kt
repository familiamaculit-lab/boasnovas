package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val postId: Long,
    val userName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
