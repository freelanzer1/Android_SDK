package ru.usedesk.knowledgebase_gui.external;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentView;
import ru.usedesk.knowledgebase_gui.internal.screens.helper.FragmentSwitcher;
import ru.usedesk.knowledgebase_gui.internal.screens.main.KnowledgeBaseViewModel;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.article.ArticleFragment;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody.ArticlesBodyFragment;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody.IOnArticleBodyClickListener;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo.ArticlesInfoFragment;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo.IOnArticleInfoClickListener;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.categories.CategoriesFragment;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.categories.IOnCategoryClickListener;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.sections.IOnSectionClickListener;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.sections.SectionsFragment;

public class UsedeskKnowledgeBaseFragment extends FragmentView<KnowledgeBaseViewModel>
        implements IOnSectionClickListener, IOnCategoryClickListener, IOnArticleInfoClickListener,
        IOnArticleBodyClickListener,
        IUsedeskOnBackPressedListener, IUsedeskOnSearchQueryListener {

    public static UsedeskKnowledgeBaseFragment newInstance() {
        return new UsedeskKnowledgeBaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = UsedeskViewCustomizer.getInstance()
                .createView(inflater, R.layout.usedesk_fragment_knowledge_base, container, false, R.style.Usedesk_Theme_KnowledgeBase);

        Button supportButton = view.findViewById(R.id.btn_support);
        supportButton.setOnClickListener(this::onSupportClick);

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(getViewLifecycleOwner(), this::showSearchQuery);

        if (savedInstanceState == null) {
            FragmentSwitcher.switchFragment(this, SectionsFragment.newInstance(), R.id.container);
        }

        return view;
    }

    private void showSearchQuery(@NonNull String query) {
        Fragment fragment = FragmentSwitcher.getLastFragment(this);
        if (fragment instanceof ArticlesBodyFragment) {
            ((ArticlesBodyFragment) fragment).onSearchQueryUpdate(query);
        } else {
            FragmentSwitcher.switchFragment(this, ArticlesBodyFragment.newInstance(query),
                    R.id.container);
        }
    }

    private void onSupportClick(View view) {
        FragmentActivity activity = getActivity();
        if (activity instanceof IUsedeskOnSupportClickListener) {
            ((IUsedeskOnSupportClickListener) activity).onSupportClick();
        }
    }

    private void switchFragment(@NonNull Fragment fragment) {
        FragmentSwitcher.switchFragment(this, fragment, R.id.container);
    }

    @Override
    public void onArticleInfoClick(long articleId) {
        switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onArticleBodyClick(long articleId) {
        switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onCategoryClick(long categoryId) {
        switchFragment(ArticlesInfoFragment.newInstance(categoryId));
    }

    @Override
    public void onSectionClick(long sectionId) {
        switchFragment(CategoriesFragment.newInstance(sectionId));

    }

    @Override
    public void onSearchQuery(String query) {
        if (query != null && !query.isEmpty()) {
            getViewModel().onSearchQuery(query);
        }
    }

    @Override
    public boolean onBackPressed() {
        return FragmentSwitcher.onBackPressed(this);
    }
}
