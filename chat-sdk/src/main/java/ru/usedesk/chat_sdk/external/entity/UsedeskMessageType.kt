package ru.usedesk.chat_sdk.external.entity

import com.google.gson.annotations.SerializedName

enum class UsedeskMessageType {
    @SerializedName("operator_to_client")
    OPERATOR_TO_CLIENT,

    @SerializedName("client_to_operator")
    CLIENT_TO_OPERATOR,

    @SerializedName("client_to_bot")
    CLIENT_TO_BOT,

    @SerializedName("bot_to_client")
    BOT_TO_CLIENT,

    @SerializedName("service")
    SERVICE
}