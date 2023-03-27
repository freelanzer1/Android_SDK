package ru.usedesk.knowledgebase_gui.screen.blocks.categories

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.SectionsModel
import ru.usedesk.knowledgebase_gui.screen.blocks.categories.CategoriesViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor,
    private val sectionId: Long
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.sectionsModelFlow.launchCollect { sectionsModel ->
            setModel {
                copy(
                    categories = (sectionsModel.loadingState as? LoadingState.Loaded<SectionsModel.Data>)
                        ?.data
                        ?.sectionsMap
                        ?.get(sectionId)
                        ?.categories
                        ?: categories
                )
            }
        }
    }

    data class State(
        val categories: List<UsedeskCategory> = listOf()
    )
}