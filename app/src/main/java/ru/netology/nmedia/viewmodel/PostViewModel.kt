package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent


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
    private val repository: PostRepository = PostRepositoryImpl (
        AppDb.getInstance(context = application).postDao()
    )
    val data: LiveData<FeedModel> = repository.data
        .map { FeedModel(it, it.isEmpty())}
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    val newerCount = data.switchMap {
        repository.getNewer( it.posts.firstOrNull()?.id ?: 0)
            .catch { _dataState.postValue(FeedModelState(error=true)) }
            .asLiveData(Dispatchers.Default)
    }

    private val _dataState = MutableLiveData<FeedModelState>()

    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _postEdited = SingleLiveEvent<Post>()
    val postEdited: LiveData<Post>
        get() = _postEdited

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> get() = _isEditing

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun makeVisible() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.makeVisible()
            }
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            // Здесь можно использовать флаг _isEditing, если нужна разная логика для сохранения
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty

    }

    fun startEdit(post: Post) {
        _postEdited.value = post
        //добавил
        edited.value = post.copy() // Создаем копию, чтобы не менять оригинал напрямую
        _isEditing.value = true
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) return
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.toggleLikeById(id) // Вызываем нашу новую функцию
            _dataState.value = FeedModelState() // Успех
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true) // Ошибка
        }
     }

    fun removeById(id: Long)  = viewModelScope.launch {

        try {
            // Устанавливаем состояние загрузки перед началом операции
            _dataState.value = FeedModelState(loading = true)
            // Вызываем метод репозитория для удаления поста по id
            repository.removeById(id)
            // Операция прошла успешно, сбрасываем состояние
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            // В случае ошибки обновляем состояние, чтобы показать ошибку в UI
            _dataState.value = FeedModelState(error = true)
        }
    }
}