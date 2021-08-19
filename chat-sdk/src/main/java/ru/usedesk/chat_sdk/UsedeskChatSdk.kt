package ru.usedesk.chat_sdk

import android.content.Context
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.InstanceBoxUsedesk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.UsedeskNotificationsServiceFactory

object UsedeskChatSdk {
    private var instanceBox: InstanceBoxUsedesk? = null
    private var chatConfiguration: UsedeskChatConfiguration? = null
    private var notificationsServiceFactory = UsedeskNotificationsServiceFactory()
    private var usedeskMessagesRepository: IUsedeskMessagesRepository? = null
    private var cacheMessagesWithFile: Boolean = true

    @JvmStatic
    fun setConfiguration(chatConfiguration: UsedeskChatConfiguration) {
        val validation = chatConfiguration.validate()
        if (!validation.isAllValid()) {
            throw RuntimeException("Invalid chat configuration: $validation")
        }
        this.chatConfiguration = chatConfiguration
    }

    @JvmStatic
    fun setCacheMessagesWithFile(cacheMessagesWithFile: Boolean) {
        this.cacheMessagesWithFile = cacheMessagesWithFile
    }

    @JvmStatic
    fun requireConfiguration(): UsedeskChatConfiguration {
        return chatConfiguration
            ?: throw RuntimeException("Must call UsedeskChatSdk.setConfiguration(...) before")
    }

    @JvmStatic
    fun init(context: Context): IUsedeskChat {
        return (instanceBox
            ?: InstanceBoxUsedesk(
                context,
                requireConfiguration(),
                usedeskMessagesRepository,
                cacheMessagesWithFile
            ).also {
                instanceBox = it
            }).usedeskChatSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskChat? {
        return instanceBox?.usedeskChatSdk
    }

    @JvmStatic
    fun requireInstance(): IUsedeskChat {
        return getInstance() ?: throw RuntimeException("Must call UsedeskChatSdk.init(...) before")
    }

    /**
     * Завершает работу IUsedeskChat
     * При force == false завершит работу только в том случае, если не осталось слушателей
     */
    @JvmStatic
    @JvmOverloads
    fun release(force: Boolean = true) {
        instanceBox?.also {
            if (force || it.usedeskChatSdk.isNoListeners()) {
                it.release()
                instanceBox = null
            }
        }
    }

    @JvmStatic
    fun setNotificationsServiceFactory(notificationsServiceFactory: UsedeskNotificationsServiceFactory) {
        this.notificationsServiceFactory = notificationsServiceFactory
    }

    @JvmStatic
    fun setUsedeskMessagesRepository(usedeskMessagesRepository: IUsedeskMessagesRepository?) {
        this.usedeskMessagesRepository = usedeskMessagesRepository
    }

    @JvmStatic
    fun startService(context: Context) {
        notificationsServiceFactory.startService(context, requireConfiguration())
    }

    @JvmStatic
    fun stopService(context: Context) {
        notificationsServiceFactory.stopService(context)
    }
}