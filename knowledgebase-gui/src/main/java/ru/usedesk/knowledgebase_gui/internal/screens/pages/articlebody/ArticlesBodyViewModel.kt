package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;

class ArticlesBodyViewModel extends DataViewModel<List<UsedeskArticleBody>> {

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

    private ArticlesBodyViewModel(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, @NonNull String searchQuery) {
        this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
        onSearchQueryUpdate(searchQuery);
    }

    void onSearchQueryUpdate(@NonNull String searchQuery) {
        loadData(usedeskKnowledgeBaseSdk.getArticlesRx(searchQuery));
    }

    static class Factory extends ViewModelFactory<ArticlesBodyViewModel> {
        private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;
        private final String searchQuery;

        public Factory(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, String searchQuery) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
            this.searchQuery = searchQuery;
        }

        @NonNull
        @Override
        protected ArticlesBodyViewModel create() {
            return new ArticlesBodyViewModel(usedeskKnowledgeBaseSdk, searchQuery);
        }

        @NonNull
        @Override
        protected Class<ArticlesBodyViewModel> getClassType() {
            return ArticlesBodyViewModel.class;
        }
    }
}
