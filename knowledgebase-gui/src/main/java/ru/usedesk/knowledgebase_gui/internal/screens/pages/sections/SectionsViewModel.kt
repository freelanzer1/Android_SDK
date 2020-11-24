package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

class SectionsViewModel extends DataViewModel<List<UsedeskSection>> {

    private SectionsViewModel(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk) {
        loadData(usedeskKnowledgeBaseSdk.getSectionsRx());
    }

    static class Factory extends ViewModelFactory<SectionsViewModel> {
        private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

        public Factory(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
        }

        @NonNull
        @Override
        protected SectionsViewModel create() {
            return new SectionsViewModel(usedeskKnowledgeBaseSdk);
        }

        @NonNull
        @Override
        protected Class<SectionsViewModel> getClassType() {
            return SectionsViewModel.class;
        }
    }
}
