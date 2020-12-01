package ru.usedesk.knowledgebase_gui.internal.screens.pages.article

import android.annotation.SuppressLint
import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticleViewModel : DataViewModel<UsedeskArticleBody>() {

    @SuppressLint("CheckResult")
    override fun onData(data: UsedeskArticleBody) {
        super.onData(data)
        UsedeskKnowledgeBaseSdk.getInstance().addViewsRx(data.id)
                .subscribe({}) { it.printStackTrace() }
    }

    fun init(articleId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticleRx(articleId))
    }
}