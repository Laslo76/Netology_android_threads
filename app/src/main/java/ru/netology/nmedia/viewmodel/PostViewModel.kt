package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _postEdited = SingleLiveEvent<Post>()
    val postEdited: LiveData<Post>
        get() = _postEdited


    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Throwable) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it,object : PostRepository.Callback<Post> {
                override fun onSuccess(posts: Post) {}
                override fun onError(e: Throwable) {
                    // Ошибка - откатываем изменения в UI
                }
            })
            _postCreated.value = Unit
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        _postEdited.value = post
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {

        val currentState = _data.value
        val currentPosts = currentState?.posts.orEmpty().toMutableList()

        val index = currentPosts.indexOfFirst { it.id == id }

        val oldPost = currentPosts[index]
        val newPost = oldPost.copy(
            likedByMe = !oldPost.likedByMe,
            likes = if (oldPost.likedByMe) oldPost.likes - 1 else oldPost.likes + 1
        )

        currentPosts[index] = newPost
        _data.postValue(currentState?.copy(posts = currentPosts))

        if (newPost.likedByMe) {
            repository.likeByIdAsync(id, object : PostRepository.Callback<Unit> {
                override fun onSuccess(posts: Unit) {}
                override fun onError(e: Throwable) {
                    // Ошибка - откатываем изменения в UI
                    currentPosts[index] = oldPost
                    _data.postValue(currentState?.copy(posts = currentPosts))
                }
            })
        } else {
            repository.dislikeByIdAsync(id, object : PostRepository.Callback<Unit> {
                override fun onSuccess(posts: Unit) {}
                override fun onError(e: Throwable) {
                    // Ошибка - откатываем изменения в UI
                    currentPosts[index] = oldPost
                    _data.postValue(currentState?.copy(posts = currentPosts))
                }
            })
        }
    }

    fun removeById(id: Long) {
        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
        _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(posts: Unit) {}
            override fun onError(e: Throwable) {
                // Ошибка - откатываем изменения в UI
                _data.postValue(_data.value?.copy(posts = old))
            }
        })
    }
}
