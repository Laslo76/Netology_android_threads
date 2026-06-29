package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel

import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import java.io.File

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

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.previewContainer.isGone = true
                return@observe
            }
            binding.previewContainer.isVisible = true
            binding.previewImage.setImageURI(photo.uri)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.setPhoto(null)
        }

        val photoContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    viewModel.changePhoto(uri, uri?.toFile())
                }
            }
        }


        // слушаем создать картинку
        binding.takePhoto.setOnClickListener {
            ImagePicker.with( this)
                .cameraOnly()
                .crop()
                .createIntent(photoContract::launch)
        }
        // слушаем получить картинку из галереи
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with( this)
                .galleryOnly()
                .crop()
                .createIntent(photoContract::launch)
        }
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when(menuItem.itemId) {
                        R.id.save -> {
                            viewModel.changeContent(binding.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        } else -> false
                }
            },
            viewLifecycleOwner,
        )


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