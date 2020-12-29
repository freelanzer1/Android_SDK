package ru.usedesk.knowledgebase_gui.pages.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showInstead
import ru.usedesk.knowledgebase_gui.R

internal class ArticlesFragment : UsedeskFragment() {

    private val viewModel: ArticlesViewModel by viewModels()

    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_fragment_list,
                    R.style.Usedesk_KnowledgeBase
            ) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            val categoryId = argsGetLong(CATEGORY_ID_KEY)
            if (categoryId != null) {
                init(categoryId)
            }
        }

        return binding.rootView
    }

    fun init(categoryId: Long) {
        viewModel.init(categoryId)

        ArticlesAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) {
            getParentListener<IOnArticleClickListener>()?.onArticleInfoClick(it)
        }

        showInstead(binding.pbLoading, binding.rvItems)
        viewModel.articleInfoListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                showInstead(binding.rvItems, binding.pbLoading)
            }
        }
    }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"

        fun newInstance(categoryId: Long): ArticlesFragment {
            return ArticlesFragment().apply {
                arguments = Bundle().apply {
                    putLong(CATEGORY_ID_KEY, categoryId)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }
}