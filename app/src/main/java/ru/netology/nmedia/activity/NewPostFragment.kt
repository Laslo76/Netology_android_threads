package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentNewPostBinding? = null // Используем безопасную ссылку
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        // ADD --- НОВОЕ: Подписка на событие редактирования ---
        // Мы подписываемся на событие, которое говорит, что нужно открыть экран для редактирования
        viewModel.postEdited.observe(viewLifecycleOwner) { postToEdit ->
            // При получении события, заполняем поле ввода текстом из поста
            binding.edit.setText(postToEdit.content)
            // Перемещаем курсор в конец текста для удобства пользователя
            binding.edit.setSelection(binding.edit.text.length)
        }
        // --- КОНЕЦ НОВОГО КОДА ---


        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.ok.setOnClickListener {
            val content = binding.edit.text.toString().trim()

            // Обновляем содержимое в ViewModel
            viewModel.changeContent(content)

            // Вызываем универсальный метод сохранения.
            // Внутри него ViewModel сам решит, делать POST или PUT запрос,
            // основываясь на том, есть ли у объекта 'edited.value' ненулевой id.
            viewModel.save()
        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        // ADD Это нужно, чтобы вернуться назад после редактирования
        viewModel.postEdited.observe(viewLifecycleOwner) { _ ->
            // После успешного редактирования просто возвращаемся назад
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Теперь подписка ставится здесь.
        // Она активируется, когда фрагмент становится видимым.
        viewModel.postEdited.observe(viewLifecycleOwner) { postToEdit ->
            // Проверка нужна на случай, если мы открыли экран для создания нового поста,
            // а не для редактирования. Тогда postToEdit будет null.
            if (postToEdit != null) {
                binding.edit.setText(postToEdit.content)
                binding.edit.setSelection(binding.edit.text.length)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем ссылку, чтобы избежать утечек памяти
    }
}