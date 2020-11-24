package ru.usedesk.chat_sdk.internal.data.repository.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;


public interface IUserInfoRepository {

    @NonNull
    String getToken() throws UsedeskDataNotFoundException;

    void setToken(@Nullable String token);

    @NonNull
    UsedeskChatConfiguration getConfiguration() throws UsedeskDataNotFoundException;

    void setConfiguration(@Nullable UsedeskChatConfiguration configuration);
}
