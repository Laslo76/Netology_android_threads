package ru.netology.nmedia.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao): PostRepository {
    override val data = dao.getAll().map { it.map { it.toDto()}}
    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            val response = PostApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }.catch { e -> throw AppError.from(e) }

    override suspend fun getById(id: Long) {
        try {
            val response = PostApi.service.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun makeVisible()   {

            dao.updateVisible()
    }

    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val postToRestore = dao.getById(id) ?: throw Exception("Post not found in DB")

        try {
            dao.removeById(id)
            // 1. Отправляем запрос на удаление поста на сервер
            val response = PostApi.service.removeById(id)
            if (!response.isSuccessful) {
                // Если запрос вернулся с ошибкой, выбрасываем исключение с кодом и сообщением об ошибке
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            // Обработка сетевых ошибок (например, нет подключения к интернету)
            dao.insert(postToRestore)
            throw NetworkError
        } catch (e: Exception) {
            // Обработка всех остальных непредвиденных ошибок
            dao.insert(postToRestore)
            throw UnknownError
        }
    }

    override suspend fun toggleLikeById(id: Long) {
        val currentEntity = dao.getById(id) ?: throw Exception("Post not found in DB")
        val currentPost = currentEntity.toDto()

        // Решаем, какую операцию выполнить, на основе текущего состояния
        if (currentPost.likedByMe) {
            dislikeById(id)
        } else {
            likeById(id)
        }
    }

    override suspend fun likeById(id: Long) {
        val currentEntity = dao.getById(id) ?: throw Exception("Post not found in DB")
        val currentPost = currentEntity.toDto()

        val updatedPost = currentPost.copy(likedByMe = true, likes = currentPost.likes + 1)

        try {
            dao.insert(PostEntity.fromDto(updatedPost, true))

            val response = PostApi.service.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            // Откат в случае сетевой ошибки
            dao.insert(currentEntity)
            throw NetworkError
        } catch (e: Exception) {
            // Откат в случае любой другой ошибки (например, ApiError)
            dao.insert(currentEntity)
            throw e // Пробрасываем ошибку дальше, чтобы ViewModel узнал о ней
        }
    }

    override suspend fun dislikeById(id: Long) {
        val currentEntity = dao.getById(id) ?: throw Exception("Post not found in DB")
        val currentPost = currentEntity.toDto()

        // 2. Создаем новую версию поста
        val updatedPost = currentPost.copy(likedByMe = false, likes = maxOf(0, currentPost.likes - 1))

        try {
            // 3. Оптистично обновляем локальную БД
            dao.insert(PostEntity.fromDto(updatedPost, true))

            // 4. Отправляем запрос на сервер
            val response = PostApi.service.dislikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            // Если успешно, возвращаем результат


        } catch (e: IOException) {
            // 5а. Откат при сетевой ошибке
            dao.insert(currentEntity)
            throw NetworkError
        } catch (e: Exception) {
            // 5б. Откат при другой ошибке
            dao.insert(currentEntity)
            throw e
        }
    }
}