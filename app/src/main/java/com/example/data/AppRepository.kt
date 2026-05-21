package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allPosts: Flow<List<Post>> = appDao.getAllPosts()

    suspend fun registerUser(user: User): Long {
        return appDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return appDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Long): User? {
        return appDao.getUserById(userId)
    }

    suspend fun createPost(post: Post): Long {
        return appDao.insertPost(post)
    }

    suspend fun deletePost(postId: Long) {
        appDao.deletePost(postId)
    }

    suspend fun updatePost(post: Post) {
        appDao.updatePost(post)
    }

    fun getCommentsForPost(postId: Long): Flow<List<Comment>> {
        return appDao.getCommentsForPost(postId)
    }

    suspend fun addComment(comment: Comment) {
        appDao.insertComment(comment)
    }
}
