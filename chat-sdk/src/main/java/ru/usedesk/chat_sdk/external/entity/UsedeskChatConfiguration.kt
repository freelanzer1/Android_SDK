package ru.usedesk.chat_sdk.external.entity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.common_sdk.external.entity.exceptions.Validators;

public class UsedeskChatConfiguration {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String URL_KEY = "urlKey";
    private static final String OFFLINE_FORM_URL_KEY = "offlineFormUrlKey";
    private static final String NAME_KEY = "nameKey";
    private static final String PHONE_KEY = "phoneKey";
    private static final String ADDITIONAL_ID_KEY = "additionalIdKey";

    private final String companyId;
    private final String email;
    private final String url;
    private final String offlineFormUrl;

    private final String clientName;
    private final Long clientPhoneNumber;
    private final Long clientAdditionalId;

    private final String initClientMessage;

    public UsedeskChatConfiguration(@NonNull String companyId, @NonNull String email,
                                    @NonNull String url, @NonNull String offlineFormUrl) {
        this(companyId, email, url, offlineFormUrl, null, null, null);
    }

    public UsedeskChatConfiguration(@NonNull String companyId, @NonNull String email,
                                    @NonNull String url, @NonNull String offlineFormUrl,
                                    @Nullable String clientName, @Nullable Long clientPhoneNumber,
                                    @Nullable Long clientAdditionalId) {
        this(companyId, email, url, offlineFormUrl, clientName, clientPhoneNumber, clientAdditionalId, null);
    }

    public UsedeskChatConfiguration(@NonNull String companyId, @NonNull String email,
                                    @NonNull String url, @NonNull String offlineFormUrl,
                                    @Nullable String clientName, @Nullable Long clientPhoneNumber,
                                    @Nullable Long clientAdditionalId, @Nullable String initClientMessage) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
        this.offlineFormUrl = offlineFormUrl;
        this.clientName = clientName;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAdditionalId = clientAdditionalId;
        this.initClientMessage = initClientMessage;
    }

    @NonNull
    public static UsedeskChatConfiguration deserialize(@NonNull Intent intent) {
        Long additionalId = null;
        Long phone = null;
        if (intent.getExtras() != null) {
            phone = intent.getExtras().getLong(PHONE_KEY);
            additionalId = intent.getExtras().getLong(ADDITIONAL_ID_KEY);
        }
        return new UsedeskChatConfiguration(intent.getStringExtra(COMPANY_ID_KEY),
                intent.getStringExtra(EMAIL_KEY),
                intent.getStringExtra(URL_KEY),
                intent.getStringExtra(OFFLINE_FORM_URL_KEY),
                intent.getStringExtra(NAME_KEY),
                phone,
                additionalId);
    }

    private static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 != null && obj2 != null) {
            return obj1.equals(obj2);
        }
        return obj1 == obj2;
    }

    public void serialize(@NonNull Intent intent) {
        intent.putExtra(COMPANY_ID_KEY, companyId);
        intent.putExtra(EMAIL_KEY, email);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(OFFLINE_FORM_URL_KEY, offlineFormUrl);
        intent.putExtra(NAME_KEY, clientName);
        intent.putExtra(PHONE_KEY, clientPhoneNumber);
        intent.putExtra(ADDITIONAL_ID_KEY, clientAdditionalId);
    }

    public boolean isValid() {
        String phoneNumber = clientPhoneNumber != null
                ? clientPhoneNumber.toString()
                : null;
        return Validators.isValidPhonePhone(phoneNumber)
                && Validators.isValidEmailNecessary(email);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskChatConfiguration) {
            UsedeskChatConfiguration configuration = (UsedeskChatConfiguration) obj;
            return equals(this.companyId, configuration.companyId) &&
                    equals(this.email, configuration.email) &&
                    equals(this.url, configuration.url) &&
                    equals(this.offlineFormUrl, configuration.offlineFormUrl) &&
                    equals(this.clientName, configuration.clientName) &&
                    equals(this.clientPhoneNumber, configuration.clientPhoneNumber) &&
                    equals(this.clientAdditionalId, configuration.clientAdditionalId) &&
                    equals(this.initClientMessage, configuration.initClientMessage);
        }
        return false;
    }

    @NonNull
    public String getCompanyId() {
        return companyId;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getOfflineFormUrl() {
        return offlineFormUrl;
    }

    @Nullable
    public String getClientName() {
        return clientName;
    }

    @Nullable
    public Long getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    @Nullable
    public Long getClientAdditionalId() {
        return clientAdditionalId;
    }

    @Nullable
    public String getInitClientMessage() {
        return initClientMessage;
    }
}