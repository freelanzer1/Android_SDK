package ru.usedesk.knowledgebase_gui.pages.articlebody

import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

internal class ArticlesBodyViewModel : DataViewModel<List<UsedeskArticleBody>>() {

    fun init(searchQuery: String) {
        onSearchQueryUpdate(searchQuery)
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticlesRx(searchQuery))
    }
}