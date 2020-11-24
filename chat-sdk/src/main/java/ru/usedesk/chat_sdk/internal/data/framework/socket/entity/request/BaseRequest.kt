package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

public class BaseRequest {

    private final String type;
    private final String token;

    BaseRequest(String type, String token) {
        this.type = type;
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }
}