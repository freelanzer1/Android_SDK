package ru.usedesk.chat_sdk.internal.data.framework.info

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException

abstract class DataLoader<T> {

    private var data: T? = null

    protected abstract fun loadData(): T?

    protected abstract fun saveData(data: T)

    @Throws(UsedeskDataNotFoundException::class)
    fun getData(): T {
        if (data == null) {
            data = loadData()
        }
        return data ?: throw UsedeskDataNotFoundException("Data not found")
    }

    fun setData(data: T?) {
        this.data = data
        if (data != null) {
            saveData(data)
        } else {
            clearData()
        }
    }

    open fun clearData() {
        data = null
    }
}