package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository.PostRepository
import java.io.IOException


class PostRepositoryImpl: PostRepository {
    override fun getAll(): List<Post> {
        return PostApi.service.getAll()
            .execute()
            .body()
            .orEmpty()
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body() ?: run {
                            callback.onError(RuntimeException("Body is empty"))
                        }
                        callback.onSuccess(body as List<Post>)
                    }


                }

                override fun onFailure(
                    call: Call<List<Post>>,
                    t: Throwable)
                {
                    callback.onError(t)
                }
            })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApi.service.save(post).enqueue(createRetrofitCallback(callback))
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApi.service.deleteById(id).enqueue(createRetrofitCallback(callback))
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApi.service.likeById(id).enqueue(createRetrofitCallback(callback))
    }

    override fun dislikeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApi.service.dislikeById(id).enqueue(createRetrofitCallback(callback))
    }

    // Вспомогательная функция для избежания дублирования кода
    private fun <T> createRetrofitCallback(callback: PostRepository.Callback<T>): retrofit2.Callback<T> {
        return object : retrofit2.Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        callback.onSuccess(body)
                    } else {
                        callback.onError(IOException("Пустой body в ответе"))
                    }
                } else {
                    callback.onError(IOException("Ошибка сервера: ${response.code()}"))
                }
            }
            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onError(t)
            }
        }
    }
}

private fun Call<Unit>.enqueue(p0: retrofit2.Callback<ru.netology.nmedia.dto.Post>) {}
