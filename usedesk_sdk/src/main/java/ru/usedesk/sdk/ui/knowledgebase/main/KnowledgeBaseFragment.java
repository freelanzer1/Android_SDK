package ru.usedesk.sdk.ui.knowledgebase.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;
import ru.usedesk.sdk.ui.knowledgebase.pages.article.ArticleFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlebody.ArticlesBodyFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlebody.IOnArticleBodyClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.ArticlesInfoFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.IOnArticleInfoClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.CategoriesFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.IOnCategoryClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.IOnSectionClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.SectionsFragment;

interface IOnFragmentStackSizeListener {
    void onFragmentStackSize(int size);
}

public class KnowledgeBaseFragment extends FragmentView<KnowledgeBaseViewModel>
        implements IOnSectionClickListener, IOnCategoryClickListener, IOnArticleInfoClickListener,
        IOnArticleBodyClickListener, IOnSearchQueryListener {

    private Button supportButton;
    private IOnFragmentStackSizeListener onFragmentStackSizeListener;
    private IOnSupportClickListener onSupportClickListener;

    public static KnowledgeBaseFragment newInstance() {
        return new KnowledgeBaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge_base, container, false);

        supportButton = view.findViewById(R.id.btn_support);
        supportButton.setOnClickListener(this::onSupportClick);

        getChildFragmentManager().removeOnBackStackChangedListener(this::onFragmentStackSize);
        getChildFragmentManager().addOnBackStackChangedListener(this::onFragmentStackSize);

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(this, this::showSearchQuery);

        if (savedInstanceState == null) {
            switchFragment(SectionsFragment.newInstance());
        }

        return view;
    }

    private void onFragmentStackSize() {
        if (onFragmentStackSizeListener != null) {
            onFragmentStackSizeListener.onFragmentStackSize(
                    getChildFragmentManager().getBackStackEntryCount());
        }
    }

    private void showSearchQuery(String query) {
        switchFragment(ArticlesBodyFragment.newInstance(query));
    }

    private void onSupportClick(View view) {
        if (onSupportClickListener != null)
            onSupportClickListener.onSupportClick();
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .addToBackStack("cur")
                .replace(R.id.container, fragment)
                .commit();
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
    public void onSearchQuery(@NonNull String query) {
        getViewModel().onSearchQuery(query);
    }

    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    public void setOnFragmentStackSizeListener(IOnFragmentStackSizeListener onFragmentStackSizeListener) {
        this.onFragmentStackSizeListener = onFragmentStackSizeListener;
    }

    public void setOnSupportClickListener(IOnSupportClickListener onSupportButtonListener) {
        this.onSupportClickListener = onSupportButtonListener;
    }
}
