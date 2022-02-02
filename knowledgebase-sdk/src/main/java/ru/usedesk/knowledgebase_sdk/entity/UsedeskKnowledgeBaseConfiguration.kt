package ru.usedesk.knowledgebase_sdk.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
class UsedeskKnowledgeBaseConfiguration @JvmOverloads constructor(
    val urlApi: String = "https://api.usedesk.ru/",
    val accountId: String,
    val token: String,
    val clientEmail: String? = null,
    val clientName: String? = null
) : Parcelable {
    fun validate(): Validation {
        return Validation(
            validUrlApi = UsedeskValidatorUtil.isValidUrlNecessary(urlApi),
            validAccountId = accountId.isNotEmpty(),
            validToken = token.isNotEmpty(),
            validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail)
        )
    }

    class Validation(
        val validUrlApi: Boolean = false,
        val validAccountId: Boolean = false,
        val validToken: Boolean = false,
        val validClientEmail: Boolean = false
    ) {
        fun isAllValid(): Boolean {
            return validUrlApi
                    && validAccountId
                    && validToken
                    && validClientEmail
        }
    }
}