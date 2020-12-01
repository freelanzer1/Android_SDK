package ru.usedesk.chat_sdk.internal.data.repository.api

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.old.UsedeskFeedback
import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.InitChatRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.SendFeedbackRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.SendMessageRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.SetEmailRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.response.ChatInitedResponse
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.response.MessageResponse
import ru.usedesk.chat_sdk.internal.data.repository.api.loader.ChatInitedConverter
import ru.usedesk.chat_sdk.internal.data.repository.api.loader.ChatItemConverter
import ru.usedesk.chat_sdk.internal.data.repository.api.loader.SocketApi
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.io.IOException
import java.net.URL

@InjectConstructor
internal class ApiRepository(
        private val socketApi: SocketApi,
        private val httpApiLoader: IHttpApiLoader,
        private val fileInfoLoader: IFileInfoLoader,
        private val chatInitedConverter: ChatInitedConverter,
        private val chatItemConverter: ChatItemConverter
) : IApiRepository {

    private fun isConnected() = socketApi.isConnected()

    @Throws(UsedeskException::class)
    override fun connect(url: String,
                         token: String?,
                         configuration: UsedeskChatConfiguration,
                         eventListener: IApiRepository.EventListener) {
        socketApi.connect(url, token, configuration.companyId, getEventListener(eventListener))
    }

    @Throws(UsedeskException::class)
    override fun init(configuration: UsedeskChatConfiguration, token: String?) {
        socketApi.sendRequest(InitChatRequest(token, configuration.companyId,
                configuration.url))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, feedback: UsedeskFeedback) {
        checkConnection()
        socketApi.sendRequest(SendFeedbackRequest(token, feedback))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, text: String) {
        checkConnection()
        socketApi.sendRequest(SendMessageRequest(token, text = text))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, usedeskFileInfo: UsedeskFileInfo) {
        checkConnection()
        val usedeskFile = fileInfoLoader.getFrom(usedeskFileInfo)
        socketApi.sendRequest(SendMessageRequest(token, usedeskFile = usedeskFile))
    }

    @Throws(UsedeskException::class)
    override fun send(token: String, email: String, name: String?, phone: Long?, additionalId: Long?) {
        socketApi.sendRequest(SetEmailRequest(token, email, name, phone, additionalId))
    }

    @Throws(UsedeskException::class)
    override fun send(configuration: UsedeskChatConfiguration, offlineForm: UsedeskOfflineForm) {
        try {
            val url = URL(configuration.offlineFormUrl)
            val postUrl = String.format(OFFLINE_FORM_PATH, url.host)
            httpApiLoader.post(postUrl, offlineForm)
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        }
    }

    override fun disconnect() {
        socketApi.disconnect()
    }

    private fun checkConnection() {
        if (!isConnected()) {
            throw UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED)
        }
    }

    private fun getEventListener(eventListener: IApiRepository.EventListener): SocketApi.EventListener {
        return object : SocketApi.EventListener {
            override fun onConnected() {
                eventListener.onConnected()
            }

            override fun onDisconnected() {
                eventListener.onDisconnected()
            }

            override fun onTokenError() {
                eventListener.onTokenError()
            }

            override fun onFeedback() {
                eventListener.onFeedback()
            }

            override fun onException(exception: Exception) {
                eventListener.onException(exception)
            }

            override fun onInited(chatInitedResponse: ChatInitedResponse) {
                val chatInited = chatInitedConverter.convert(chatInitedResponse)
                eventListener.onChatInited(chatInited)
            }

            override fun onNew(messageResponse: MessageResponse) {
                val chatItems = chatItemConverter.convert(messageResponse.message)
                eventListener.onNewChatItems(chatItems)
            }
        }
    }

    companion object {
        private const val OFFLINE_FORM_PATH = "https://%1s/widget.js/"
    }
}