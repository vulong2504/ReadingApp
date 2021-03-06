package vulong.book_app.ui.book_detail_screen.screen.chapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import vulong.book_app.databinding.FragmentBookDetailChapterBinding
import vulong.book_app.model.local_db.ReadBookProgress
import vulong.book_app.ui.book_detail_screen.BookDetailFragmentDirections
import vulong.book_app.ui.book_detail_screen.BookDetailViewModel

class ChapterFragment : Fragment() {

    private lateinit var binding: FragmentBookDetailChapterBinding
    private val viewModel: BookDetailViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBookDetailChapterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = ChapterAdapter(viewModel.currentBook!!.chapterNumber) {
            val action =
                BookDetailFragmentDirections
                    .actionBookDetailFragmentToReadBookFragment(
                        viewModel.currentBook!!,
                        ReadBookProgress(viewModel.currentBook!!.id, it, 0)
                    )
            findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
}