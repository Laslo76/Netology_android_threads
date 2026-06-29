package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer( id: Long): Flow<Int>
    suspend fun getAll()
    suspend fun save(post: Post, file: File? = null)
    suspend fun removeById(id: Long)
    suspend fun toggleLikeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun dislikeById(id: Long)

    suspend fun getById(id: Long)
    suspend fun makeVisible()

    suspend fun upload(upload: MediaUpload): Media
}
