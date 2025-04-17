
package ru.usedesk.chat_sdk.domain

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

interface IUsedeskPreparation {
    fun createChat(
        apiToken: String,
        onResult: (CreateChatResult) -> Unit
    )

    fun updateConfig(
        configuration: UsedeskChatConfiguration,
    )

    sealed interface CreateChatResult {
        class Done(val clientToken: String) : CreateChatResult
        object Error : CreateChatResult
    }
}