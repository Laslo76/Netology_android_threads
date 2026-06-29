package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentMediaBinding


class FragmentMedia : Fragment() {

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaBinding.inflate(inflater, container, false)

        // Получаем аргументы через safe args (лучший вариант)
        val imageUrl = arguments?.getString("imageUrl")
        // Или если вы используете плагин 'androidx.navigation.safeargs':
        // val imageUrl = FragmentMediaArgs.fromBundle(requireArguments()).imageUrl

        // Теперь используйте imageUrl, например, загрузите её в ImageView
        if (!imageUrl.isNullOrEmpty()) {
            // Пример использования Glide для загрузки картинки
            Glide.with(this)
                .load(imageUrl)
                .into(binding.mediaFullScreen)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
