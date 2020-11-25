package ru.usedesk.knowledgebase_gui.internal.screens.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

class DelayedQuery {
    private final PublishSubject<String> queryPublishSubject = PublishSubject.create();
    private final Disposable disposable;

    DelayedQuery(@NonNull final MutableLiveData<String> searchQueryLiveData, int delayMilliseconds) {
        disposable = queryPublishSubject.debounce(delayMilliseconds, TimeUnit.MILLISECONDS)
                .subscribe(searchQueryLiveData::postValue);
    }

    void dispose() {
        disposable.dispose();
    }

    void onNext(String query) {
        queryPublishSubject.onNext(query);
    }
}
