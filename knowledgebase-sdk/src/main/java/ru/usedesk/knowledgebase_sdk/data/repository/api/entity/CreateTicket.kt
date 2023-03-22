package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface CreateTicket {
    class Request(
        private val apiToken: String,
        private val clientEmail: String? = null,
        @SerializedName("clientName")
        private val clientName: String? = null,
        message: String,
        articleId: Long
    ) {
        private val subject = "Отзыв о статье" //TODO:
        private val tag = "БЗ" //TODO:
        private val message = "ID: $articleId\n$message"
    }

    class Response(
        val status: String? = null,
        val ticketId: Long? = null
    ) : UsedeskApiError()
}