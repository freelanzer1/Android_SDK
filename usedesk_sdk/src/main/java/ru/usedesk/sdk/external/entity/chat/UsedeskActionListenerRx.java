package ru.usedesk.sdk.external.entity.chat;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class UsedeskActionListenerRx implements UsedeskActionListener {

    private final Subject<EmptyItem> connectedSubject = BehaviorSubject.create();
    private final Subject<Message> messageSubject = BehaviorSubject.create();
    private final Subject<Message> newMessageSubject = BehaviorSubject.create();
    private final Subject<EmptyItem> offlineFormExpectedSubject = BehaviorSubject.create();
    private final Subject<EmptyItem> disconnectedSubject = BehaviorSubject.create();
    private final Subject<Integer> errorResIdSubject = BehaviorSubject.create();
    private final Subject<Exception> errorSubject = BehaviorSubject.create();

    @Inject
    public UsedeskActionListenerRx() {
    }

    private void onMessage(Message message) {
        if (message != null) {
            messageSubject.onNext(message);
        }
    }

    private void onNewMessage(Message message) {
        if (message != null) {
            newMessageSubject.onNext(message);
        }
    }

    public Observable<EmptyItem> getConnectedObservable() {
        return connectedSubject;
    }

    public Observable<Message> getMessageObservable() {
        return messageSubject;
    }

    public Observable<Message> getNewMessageObservable() {
        return newMessageSubject;
    }

    public Observable<EmptyItem> getOfflineFormExpectedObservable() {
        return offlineFormExpectedSubject;
    }

    public Observable<EmptyItem> getDisconnectedSubject() {
        return disconnectedSubject;
    }

    public Observable<Integer> getErrorResIdSubject() {
        return errorResIdSubject;
    }

    public Observable<Exception> getErrorSubject() {
        return errorSubject;
    }

    @Override
    public void onConnected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    public void onMessageReceived(Message message) {
        onMessage(message);
        onNewMessage(message);
    }

    @Override
    public void onMessagesReceived(List<Message> messages) {
        if (messages != null) {
            for (Message message : messages) {
                onMessage(message);
            }
        }
    }

    @Override
    public void onServiceMessageReceived(Message message) {
        onMessage(message);
    }

    @Override
    public void onOfflineFormExpected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    public void onDisconnected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    public void onError(int errorResId) {
        errorResIdSubject.onNext(errorResId);
    }

    @Override
    public void onError(Exception e) {
        if (e != null) {
            errorSubject.onError(e);
        }
    }

    enum EmptyItem {
        IGNORE_ME
    }
}
