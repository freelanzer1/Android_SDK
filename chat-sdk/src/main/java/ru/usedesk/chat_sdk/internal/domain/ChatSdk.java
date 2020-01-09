package ru.usedesk.chat_sdk.internal.domain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.MessageType;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.common_sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class ChatSdk implements IUsedeskChatSdk {

    private Context context;
    private UsedeskChatConfiguration configuration;
    private UsedeskActionListener actionListener;
    private String token;

    private IUserInfoRepository userInfoRepository;
    private IApiRepository apiRepository;

    private boolean needSetEmail = false;

    @Inject
    ChatSdk(@NonNull Context context,
            @NonNull UsedeskChatConfiguration configuration,
            @NonNull UsedeskActionListener actionListener,
            @NonNull IUserInfoRepository userInfoRepository,
            @NonNull IApiRepository apiRepository) throws UsedeskException {
        this.context = context;
        this.userInfoRepository = userInfoRepository;
        this.apiRepository = apiRepository;

        this.configuration = configuration;
        this.actionListener = actionListener;

        init();
    }

    private <T> boolean equals(@Nullable T a, @Nullable T b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    private void init() throws UsedeskException {

        try {
            UsedeskChatConfiguration configuration = userInfoRepository.getConfiguration();
            if (this.configuration.getEmail().equals(configuration.getEmail())
                    && this.configuration.getCompanyId().equals(configuration.getCompanyId())) {
                token = userInfoRepository.getToken();
            }
            if (token != null && (!equals(this.configuration.getClientName(), configuration.getClientName())
                    || !equals(this.configuration.getClientPhoneNumber(), configuration.getClientPhoneNumber())
                    || !equals(this.configuration.getClientAdditionalId(), configuration.getClientAdditionalId()))) {
                needSetEmail = true;
            }
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }

        userInfoRepository.setConfiguration(configuration);

        apiRepository.connect(configuration.getUrl(), actionListener, getOnMessageListener());
    }

    @Override
    public void disconnect() throws UsedeskException {
        apiRepository.disconnect();
    }

    @Override
    public void sendMessage(String text) throws UsedeskException {
        if (text == null || text.isEmpty()) {
            return;
        }

        apiRepository.send(token, text);
    }

    @Override
    public void sendMessage(UsedeskFileInfo usedeskFileInfo) throws UsedeskException {
        if (usedeskFileInfo == null) {
            return;
        }

        apiRepository.send(token, usedeskFileInfo);
    }

    @Override
    public void sendMessage(List<UsedeskFileInfo> usedeskFileInfoList) throws UsedeskException {
        if (usedeskFileInfoList == null) {
            return;
        }

        for (UsedeskFileInfo usedeskFileInfo : usedeskFileInfoList) {
            sendMessage(usedeskFileInfo);
        }
    }

    @Override
    public void sendFeedbackMessage(Feedback feedback) throws UsedeskException {
        if (feedback == null) {
            return;
        }

        apiRepository.send(token, feedback);
    }

    @Override
    public void sendOfflineForm(OfflineForm offlineForm) throws UsedeskException {
        if (offlineForm == null) {
            return;
        }
        apiRepository.send(configuration, offlineForm);
    }

    @Override
    public void onClickButtonWidget(MessageButtons.MessageButton messageButton) throws UsedeskException {
        if (messageButton == null) {
            return;
        }
        if (messageButton.getUrl().isEmpty()) {
            sendMessage(messageButton.getText());
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.getUrl()));//TODO
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }

    private void setUserEmail() throws UsedeskException {
        apiRepository.send(token, configuration.getEmail(),
                configuration.getClientName(),
                configuration.getClientPhoneNumber(),
                configuration.getClientAdditionalId());
    }

    private void parseNewMessageResponse(Message message) {
        if (message != null && message.getChat() != null) {
            boolean hasText = !TextUtils.isEmpty(message.getText());
            boolean hasFile = message.getUsedeskFile() != null;

            if (hasText || hasFile) {
                actionListener.onMessageReceived(message);
            }
        }
    }

    private void parseFeedbackResponse() {
        Message message = new Message(MessageType.SERVICE);
        actionListener.onServiceMessageReceived(message);
    }

    private void parseInitResponse(String token, Setup setup) {
        this.token = token;
        userInfoRepository.setToken(token);

        actionListener.onConnected();

        if (setup != null) {
            if (setup.isWaitingEmail()) {
                setUserEmail();
            }

            if (setup.getMessages() != null && !setup.getMessages().isEmpty()) {
                actionListener.onMessagesReceived(setup.getMessages());
            }

            if (setup.isNoOperators()) {
                onOfflineFormDialog();
            }
        } else {
            setUserEmail();
        }
    }

    private OnMessageListener getOnMessageListener() {
        return new OnMessageListener() {
            @Override
            public void onNew(Message message) {
                try {
                    if (message != null && message.getPayloadAsObject() != null) {
                        Map map = (Map) message.getPayloadAsObject();

                        Boolean noOperators = (Boolean) map.get("noOperators");//TODO: выпилить отсюда и впилить по доке (когда сервер начнёт отдавать что нужно)

                        if (noOperators != null && noOperators) {
                            onOfflineFormDialog();
                            return;
                        }
                    }
                } catch (ClassCastException e) {
                    //nothing
                }
                parseNewMessageResponse(message);
            }

            @Override
            public void onFeedback() {
                parseFeedbackResponse();
            }

            @Override
            public void onInit(String token, Setup setup) {
                parseInitResponse(token, setup);

                if (needSetEmail) {
                    setUserEmail();
                }
            }

            @Override
            public void onInitChat() {
                initChat();
            }

            @Override
            public void onTokenError() {
                userInfoRepository.setToken(null);
                token = null;

                initChat();
            }
        };
    }

    private void onOfflineFormDialog() {//TODO
        Message message = new Message(MessageType.SERVICE);
        actionListener.onServiceMessageReceived(message);
        actionListener.onOfflineFormExpected();
    }
}