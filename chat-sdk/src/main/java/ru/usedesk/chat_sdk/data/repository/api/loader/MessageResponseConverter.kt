package ru.usedesk.chat_sdk.data.repository.api.loader

import android.util.Patterns
import ru.usedesk.chat_sdk.data.repository._extra.Converter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.api.UsedeskApiRepository.Companion.valueOrNull
import ru.usedesk.common_sdk.utils.UsedeskDateUtil.Companion.getLocalCalendar
import javax.inject.Inject

internal class MessageResponseConverter @Inject constructor() :
    Converter<MessageResponse.Message?, List<UsedeskMessage>> {

    private val emailRegex = Patterns.EMAIL_ADDRESS.toRegex()
    private val phoneRegex = Patterns.PHONE.toRegex()
    private val urlRegex = Patterns.WEB_URL.toRegex()
    private val mdUrlRegex = """\[[^\[\]\(\)]+\]\(${urlRegex.pattern}/?\)""".toRegex()
    private val badUrlRegex = """<${urlRegex.pattern}/>""".toRegex()
    private val objectRegex = """\{\{[^\{\}:]*:([^\{\};]*;){2}[^\{\};]*\}\}""".toRegex()
    private val imageRegexp = """!\[[^]]*]\((.*?)\s*(\"(?:.*[^\"])\")?\s*\)""".toRegex()

    fun convertText(text: String): String = try {
        text.replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
            .replace("</strong>", "</b>")
            .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
            .replace("</em>", "</i>")
            .replace("</p>", "")
            .removePrefix("<p>")
            .trim('\n', '\r', ' ', '\u200B')
            .split('\n')
            .joinToString("\n") { line ->
                line.trim('\r', ' ', '\u200B')
                    .replace(badUrlRegex) { it.value.drop(1).dropLast(2) }
                    .convertMarkdownUrls()
                    .convertMarkdownText()
            }
            .replace("\n\n", "\n")
            .replace("\n", "<br>")
    } catch (e: Exception) {
        e.printStackTrace()
        text
    }

    override fun convert(from: MessageResponse.Message?): List<UsedeskMessage> = valueOrNull {
        val fromClient = when (from!!.type) {
            MessageResponse.TYPE_CLIENT_TO_OPERATOR,
            MessageResponse.TYPE_CLIENT_TO_BOT -> true
            MessageResponse.TYPE_OPERATOR_TO_CLIENT,
            MessageResponse.TYPE_BOT_TO_CLIENT -> false
            else -> null
        }!!

        val createdAt = from.createdAt!!

        val messageDate = try {
            getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss'Z'", createdAt)
        } catch (e: Exception) {
            getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", createdAt)
        }

        val id = from.id!!
        val localId = from.payload?.messageId ?: id
        val name = from.name ?: ""
        val avatar = from.payload?.avatar ?: ""

        val fileMessage = valueOrNull {
            val file = UsedeskFile.create(
                from.file!!.content!!,
                from.file!!.type,
                from.file!!.size!!,
                from.file!!.name!!
            )

            when {
                fromClient -> when {
                    file.isImage() -> UsedeskMessageClientImage(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    file.isVideo() -> UsedeskMessageClientVideo(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    file.isAudio() -> UsedeskMessageClientAudio(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    else -> UsedeskMessageClientFile(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                }
                else -> when {
                    file.isImage() -> UsedeskMessageAgentImage(
                        id,
                        messageDate,
                        file,
                        name,
                        avatar
                    )
                    file.isVideo() -> UsedeskMessageAgentVideo(
                        id,
                        messageDate,
                        file,
                        name,
                        avatar
                    )
                    file.isAudio() -> UsedeskMessageAgentAudio(
                        id,
                        messageDate,
                        file,
                        name,
                        avatar
                    )
                    else -> UsedeskMessageAgentFile(
                        id,
                        messageDate,
                        file,
                        name,
                        avatar
                    )
                }
            }
        }

        valueOrNull {
            val objects: List<MessageObject>
            val feedbackNeeded: Boolean
            val feedback: UsedeskFeedback?
            if (!fromClient) {
                objects = from.text?.toMessageObjects() ?: listOf()
                feedback = when (from.payload?.userRating) {
                    "LIKE" -> UsedeskFeedback.LIKE
                    "DISLIKE" -> UsedeskFeedback.DISLIKE
                    else -> null
                }
                feedbackNeeded = feedback == null && from.payload?.buttons?.any {
                    it?.data == "GOOD_CHAT" ||
                            it?.data == "BAD_CHAT" ||
                            it?.icon == "like" ||
                            it?.icon == "dislike"
                } ?: false
            } else {
                objects = listOf(MessageObject.Text(from.text ?: ""))
                feedbackNeeded = false
                feedback = null
            }
            val buttons = objects.filterIsInstance<MessageObject.Button>()
                .map(MessageObject.Button::button)

            val fields = objects.filterIsInstance<MessageObject.Field>()
                .map(MessageObject.Field::field)

            val fileMessages = objects.filterIsInstance<MessageObject.Image>()
                .map {
                    when {
                        fromClient -> UsedeskMessageClientImage(
                            id,
                            messageDate,
                            it.file,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        else -> UsedeskMessageAgentImage(
                            id,
                            messageDate,
                            it.file,
                            name,
                            avatar
                        )
                    }
                }

            val convertedText = convertText(
                objects.filterIsInstance<MessageObject.Text>()
                    .joinToString(separator = "", transform = MessageObject.Text::text)
            )

            listOf(
                when {
                    convertedText.isEmpty() &&
                            buttons.isEmpty() &&
                            fields.isEmpty() -> null
                    fromClient -> UsedeskMessageClientText(
                        id,
                        messageDate,
                        from.text!!,
                        convertedText,
                        UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    else -> UsedeskMessageAgentText(
                        id,
                        messageDate,
                        from.text!!,
                        convertedText,
                        buttons,
                        fields,
                        feedbackNeeded,
                        feedback,
                        name,
                        avatar
                    )
                }
            ) + fileMessage + fileMessages
        }?.filterNotNull()
    } ?: listOf()

    private fun String.convertMarkdownText() = StringBuilder().also { builder ->
        var i = 0
        var boldOpen = true
        var italicOpen = true
        while (i < this.length) {
            builder.append(
                when (this[i]) {
                    '*' -> when {
                        this.getOrNull(i + 1) == '*' -> {
                            i++
                            boldOpen = !boldOpen
                            if (boldOpen) "</b>"
                            else "<b>"
                        }
                        else -> {
                            italicOpen = !italicOpen
                            if (italicOpen) "</i>"
                            else "<i>"
                        }
                    }
                    '\n' -> "<br>"
                    else -> this[i]
                }
            )
            i++
        }
    }.toString()

    private fun Regex.findAll(
        text: String,
        includedRanges: List<IntRange>
    ) = includedRanges.flatMap { part ->
        findAll(text.substring(part))
            .map { (it.range.first + part.first)..(it.range.last + part.first) }
    }

    private fun String.getExcludeRanges(includedRanges: List<IntRange>): List<IntRange> {
        val ranges = includedRanges.sortedBy(IntRange::first)
        return (sequenceOf(
            0 until (ranges.firstOrNull()?.first ?: length),
            (ranges.lastOrNull()?.last?.inc() ?: 0) until length
        ) + ranges.indices.mapNotNull { i ->
            when {
                i < ranges.size - 1 -> ranges[i].last + 1 until ranges[i + 1].first
                else -> null
            }
        }.asSequence())
            .filter { it.first <= it.last && it.first in this.indices && it.last in this.indices }
            .toSet()
            .toList()
    }

    private fun String.convertMarkdownUrls(): String {
        val withMdUrlsRanges = mdUrlRegex.findAll(this, listOf(this.indices))

        val noMdUrlsRanges = getExcludeRanges(withMdUrlsRanges)

        val emails = emailRegex.findAll(this, noMdUrlsRanges)

        val withEmailsRanges = withMdUrlsRanges + emails
        val noEmailsRanges = getExcludeRanges(withEmailsRanges)

        val urls = urlRegex.findAll(this, noEmailsRanges)

        val withUrlsRanges = withEmailsRanges + urls
        val noUrlsRanges = getExcludeRanges(withUrlsRanges)

        val phones = phoneRegex.findAll(this, noUrlsRanges)

        val withPhonesRanges = withUrlsRanges + phones
        val noPhones = getExcludeRanges(withPhonesRanges)

        val builder = StringBuilder()

        (withPhonesRanges + noPhones).toSet()
            .sortedBy(IntRange::first)
            .forEach {
                val part = this.substring(it)
                builder.append(when (it) {
                    in withMdUrlsRanges -> {
                        val parts = part.trim('[', ')')
                            .split("](")
                        val url = parts[1]
                        val title = parts[0].ifEmpty { url }
                        makeHtmlUrl(url, title)
                    }
                    in urls -> makeHtmlUrl(part)
                    in emails -> makeHtmlUrl("mailto:$part", part)
                    in phones -> makeHtmlUrl("tel:$part", part)
                    else -> part
                })
            }

        return builder.toString()
    }

    private fun makeHtmlUrl(url: String, title: String = url) = "<a href=\"$url\">$title</a>"

    sealed interface MessageObject {
        class Text(val text: String) : MessageObject
        class Button(val button: UsedeskMessageButton) : MessageObject
        class Field(val field: UsedeskMessageField) : MessageObject
        class Image(val file: UsedeskFile) : MessageObject
    }

    private fun <PARENT, OUT : PARENT, IN : PARENT> String.parts(
        regex: Regex,
        outConverter: String.() -> List<OUT>,
        inConverter: String.() -> List<IN>
    ): List<PARENT> {
        val ranges = regex.findAll(this)
            .map(MatchResult::range)
            .toList()
        val indexes = ranges.flatMap { sequenceOf(it.first, it.last + 1) } +
                sequenceOf(length)
        var i = 0
        return indexes.toSet().map { index ->
            val part = when (index) {
                i -> ""
                else -> substring(i, index)
            }
            when (i until index) {
                in ranges -> part.inConverter()
                else -> part.outConverter()
            }.apply { i = index }
        }.flatten()
    }

    private fun String.toMessageImage(): MessageObject {
        val section = drop(2).dropLast(1)
        val fileName = section.substringBefore("](")
        val fileUrl = section.substringAfter("](")

        val image = UsedeskFile.create(
            fileUrl,
            "image/*",
            "0",
            fileName
        )

        return MessageObject.Image(image)
    }

    private fun String.toMessageObjects() = parts(
        objectRegex,
        inConverter = { listOf(toMessageObject()) },
        outConverter = {
            parts(
                imageRegexp,
                outConverter = { listOf(MessageObject.Text(this)) },
                inConverter = { listOf(toMessageImage()) }
            )
        }
    )

    private fun String.toMessageObject(): MessageObject {
        val isButton = startsWith("{{button:")
        if (isButton || startsWith("{{field:")) {
            val parts = substringAfter(':')
                .dropLast(2)
                .split(";")
            if (parts.size == 4) {
                return when {
                    isButton -> MessageObject.Button(
                        UsedeskMessageButton(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3] == "show"
                        )
                    )
                    else -> MessageObject.Field(
                        UsedeskMessageField(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3] == "show"
                        )
                    )
                }
            }
        }

        return MessageObject.Text(this)
    }
}