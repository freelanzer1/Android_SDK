package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

public class SetEmailResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/SET";

    private State state;
    private boolean reset;

    public SetEmailResponse() {
        super(TYPE);
    }

    public State getState() {
        return state;
    }

    public boolean isReset() {
        return reset;
    }
}