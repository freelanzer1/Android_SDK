package ru.usedesk.knowledgebase_gui.screens.article.item

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.*
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnWebUrlListener
import ru.usedesk.knowledgebase_gui.screens.article.ArticlePageViewModel
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import kotlin.math.max

internal class ArticleItem : UsedeskFragment() {

    private lateinit var binding: Binding

    private val viewModel: ArticleItemViewModel by viewModels()
    private val parentViewModel: ArticlePageViewModel by viewModels(
        ownerProducer = this@ArticleItem::requireParentFragment
    )

    private lateinit var messageStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var yesStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var noStyleValues: UsedeskResourceManager.StyleValues
    private lateinit var loadingAdapter: UsedeskCommonViewLoadingAdapter

    private var scrollY = 0

    private var currentArticleId: Long? = null

    private var canUpdateFab = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            layoutInflater,
            container,
            R.layout.usedesk_page_item_article_content,
            R.style.Usedesk_KnowledgeBase_Article_Content_Page_Item,
            ::Binding
        ).apply {
            messageStyleValues = styleValues
                .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_title_text)

            yesStyleValues = styleValues
                .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_yes_text)

            noStyleValues = styleValues
                .getStyleValues(R.attr.usedesk_knowledgebase_article_content_page_rating_no_text)

            lRating.visibility = visibleGone(argsGetBoolean(WITH_ARTICLE_RATING_KEY, true))
            lRatingYes.visibility = View.GONE
            lRatingNo.visibility = View.GONE
            etRating.visibility = View.GONE
            tvRatingTitle.visibility = View.GONE

            val previousTitle = argsGetString(PREVIOUS_TITLE_KEY)
            if (previousTitle != null) {
                tvPrevious.text = previousTitle
                lPrevious.setOnClickListener {
                    findParent<IOnArticlePagesListener>()?.onPrevious()
                }
            } else {
                lPrevious.visibility = View.INVISIBLE
            }

            val nextTitle = argsGetString(NEXT_TITLE_KEY)
            if (nextTitle != null) {
                tvNext.text = nextTitle
                lNext.setOnClickListener {
                    findParent<IOnArticlePagesListener>()?.onNext()
                }
            } else {
                lNext.visibility = View.INVISIBLE
            }

            lContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                this@ArticleItem.scrollY = scrollY
                updateFab()
            })

            lContent.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val minHeight = rootView.height - lBottomNavigation.measuredHeight
                if (lContentScrollable.minimumHeight != minHeight) {
                    lContentScrollable.minimumHeight = minHeight
                }
                updateFab()
            }
        }

        argsGetLong(ARTICLE_ID_KEY)?.also { articleId ->
            currentArticleId = articleId
            viewModel.init(articleId)
        }

        loadingAdapter = UsedeskCommonViewLoadingAdapter(binding.vLoading)

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.state != new.state) {
                loadingAdapter.update(new.state)
                binding.wvContent.visibility = visibleInvisible(new.state == State.LOADED)
                binding.lRating.visibility = visibleInvisible(new.state == State.LOADED)
            }
            if (old?.articleContent != new.articleContent) {
                new.articleContent?.let(this@ArticleItem::onArticleContent)
            }
        }
        parentViewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.selectedArticle != new.selectedArticle) {
                currentArticleId?.let {
                    if (new.selectedArticle?.id != it) {
                        showQuestion(it)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        canUpdateFab = false
    }

    override fun onResume() {
        super.onResume()

        canUpdateFab = true
        updateFab()
    }

    private fun updateFab() {
        if (canUpdateFab) {
            binding.run {
                val fabMargin = max(
                    0,
                    (scrollY + lContent.height) - max(
                        lContentScrollable.height,
                        lContentScrollable.minimumHeight
                    )
                )
                findParent<UsedeskKnowledgeBaseScreen>()?.onSupportButtonBottomMargin(fabMargin)
            }
        }
    }

    private fun onArticleContent(articleContent: UsedeskArticleContent) {
        binding.wvContent.run {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(webView: WebView?, url: String?) {
                    super.onPageFinished(webView, url)

                    showQuestion(articleContent.id)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ) = findParent<IUsedeskOnWebUrlListener>()?.onWebUrl(url) == true
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                loadData(articleContent.text, "text/html; charset=utf-8", "UTF-8")
            } else {
                loadData(articleContent.text, "text/html", null)
            }
            setBackgroundColor(Color.TRANSPARENT)
            showInstead(this, binding.vLoading.rootView, gone = false)
        }
    }

    private fun showQuestion(articleId: Long) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_1)
        binding.tvRatingNo.text = noStyleValues.getString(R.attr.usedesk_text_1)

        binding.tvRatingYes.setOnClickListener {
            viewModel.sendArticleRating(articleId, true)

            showThanks()
        }
        binding.tvRatingNo.setOnClickListener {
            viewModel.sendArticleRating(articleId, false)

            showWhatsWrong(articleId)
        }

        binding.tvRatingTitle.visibility = View.VISIBLE
        binding.lRatingYes.visibility = View.VISIBLE
        binding.lRatingNo.visibility = View.VISIBLE
        binding.etRating.visibility = View.GONE
    }

    private fun showWhatsWrong(articleId: Long) {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_2)
        binding.tvRatingYes.text = yesStyleValues.getString(R.attr.usedesk_text_2)

        binding.tvRatingYes.setOnClickListener {
            viewModel.sendArticleRating(articleId, binding.etRating.text.toString())

            showThanks()
        }

        binding.lRatingNo.visibility = View.GONE
        binding.tvRatingTitle.visibility = View.VISIBLE
        binding.lRatingYes.visibility = View.VISIBLE
        binding.etRating.visibility = View.VISIBLE
    }

    private fun showThanks() {
        binding.tvRatingTitle.text = messageStyleValues.getString(R.attr.usedesk_text_3)
        binding.lRatingYes.visibility = View.GONE
        binding.lRatingNo.visibility = View.GONE
        binding.etRating.visibility = View.GONE
        binding.tvRatingTitle.visibility = View.VISIBLE
    }

    companion object {
        private const val ARTICLE_ID_KEY = "articleIdKey"
        private const val PREVIOUS_TITLE_KEY = "previousTitleKey"
        private const val NEXT_TITLE_KEY = "nextTitleKey"
        private const val WITH_ARTICLE_RATING_KEY = "withArticleRatingKey"

        fun newInstance(
            withArticleRating: Boolean,
            articleId: Long,
            previousTitle: String?,
            nextTitle: String?
        ): ArticleItem {
            return ArticleItem().apply {
                arguments = Bundle().apply {
                    putLong(ARTICLE_ID_KEY, articleId)
                    putString(PREVIOUS_TITLE_KEY, previousTitle)
                    putString(NEXT_TITLE_KEY, nextTitle)
                    putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lContent: NestedScrollView = rootView.findViewById(R.id.l_content)
        val lContentScrollable: View = rootView.findViewById(R.id.l_content_scrollable)
        val lBottomNavigation: View = rootView.findViewById(R.id.l_bottom_navigation)
        val wvContent: WebView = rootView.findViewById(R.id.wv_content)
        val lRating: ViewGroup = rootView.findViewById(R.id.l_rating)
        val tvRatingTitle: TextView = rootView.findViewById(R.id.tv_rating_title)
        val etRating: EditText = rootView.findViewById(R.id.et_rating_message)
        val lRatingYes: View = rootView.findViewById(R.id.l_rating_yes)
        val tvRatingYes: TextView = rootView.findViewById(R.id.tv_rating_yes)
        val lRatingNo: View = rootView.findViewById(R.id.l_rating_no)
        val tvRatingNo: TextView = rootView.findViewById(R.id.tv_rating_no)
        val lPrevious: View = rootView.findViewById(R.id.l_previous)
        val tvPrevious: TextView = rootView.findViewById(R.id.tv_previous)
        val lNext: View = rootView.findViewById(R.id.l_next)
        val tvNext: TextView = rootView.findViewById(R.id.tv_next)
        val vLoading = UsedeskCommonViewLoadingAdapter.Binding(
            rootView.findViewById(R.id.v_loading),
            defaultStyleId
        )
    }
}
