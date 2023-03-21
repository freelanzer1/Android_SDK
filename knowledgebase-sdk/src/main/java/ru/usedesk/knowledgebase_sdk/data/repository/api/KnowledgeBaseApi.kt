package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.*
import ru.usedesk.knowledgebase_sdk.entity.*
import javax.inject.Inject

internal class KnowledgeBaseApi @Inject constructor(
    private val configuration: UsedeskKnowledgeBaseConfiguration,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(
    apiFactory,
    multipartConverter,
    gson,
    ApiRetrofit::class.java
), IKnowledgeBaseApi {

    private fun Array<CategoryResponse?>.convert() = mapNotNull { categoryResponse ->
        valueOrNull {
            val categoryId = categoryResponse!!.id!!
            val articles = categoryResponse.articles?.mapNotNull { articleResponse ->
                valueOrNull {
                    UsedeskArticleInfo(
                        articleResponse!!.id!!,
                        articleResponse.title ?: "",
                        categoryId,
                        articleResponse.views ?: 0
                    )
                }
            } ?: listOf()
            listOf(
                UsedeskCategory(
                    categoryId,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 10000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 1000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 100000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 10000000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                )
            )
        }
    }.flatten()

    private fun Array<SectionResponse?>.convert() = mapNotNull { sectionResponse ->
        valueOrNull {
            val categories = sectionResponse!!.categories?.convert() ?: listOf()
            listOf(
                UsedeskSection(
                    sectionResponse.id!!,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 10000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 100000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 10000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 1000000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 100000000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                )
            )
        }
    }.flatten()

    override suspend fun getSections(): GetSectionsResponse {
        val request = LoadSections.Request(
            configuration.token,
            configuration.accountId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            LoadSections.Response::class.java
        ) {
            getSections(it.accountId, it.token)
        }
        return when (response?.items) {
            null -> GetSectionsResponse.Error(response?.code)
            else -> GetSectionsResponse.Done(response.items.convert())
        }
    }

    override suspend fun getArticle(articleId: Long): GetArticleResponse {
        val request = GetArticleContent.Request(
            configuration.accountId,
            articleId,
            configuration.token
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            GetArticleContent.Response::class.java
        ) {
            getArticleContent(
                it.accountId,
                it.articleId,
                it.token
            )
        }
        return when (val articleContent = response?.convert()) {
            null -> GetArticleResponse.Error(response?.code)
            else -> GetArticleResponse.Done(articleContent)
        }
    }

    override fun getArticles(searchQueryRequest: SearchQueryRequest): List<UsedeskArticleContent> {
        val articlesSearchResponse = doRequest(
            configuration.urlApi,
            ArticlesSearchResponse::class.java
        ) {
            getArticles(
                configuration.accountId,
                configuration.token,
                searchQueryRequest.query,
                searchQueryRequest.count,
                searchQueryRequest.sectionIds?.joinToString(","),
                searchQueryRequest.categoryIds?.joinToString(","),
                searchQueryRequest.articleIds?.joinToString(","),
                searchQueryRequest.page,
                searchQueryRequest.type?.name?.lowercase(),
                searchQueryRequest.sort?.name?.lowercase(),
                searchQueryRequest.order?.name?.lowercase()
            )
        }

        return (articlesSearchResponse.articles ?: arrayOf())
            .mapNotNull { it?.convert() }
    }

    private fun GetArticleContent.Response.convert() = valueOrNull {
        UsedeskArticleContent(
            id!!,
            title ?: "",
            text ?: "",
            categoryId?.toLongOrNull()!!
        )
    }

    override fun addViews(articleId: Long) {
        doRequest(
            configuration.urlApi,
            AddViewsResponse::class.java
        ) {
            addViews(
                configuration.accountId,
                articleId,
                configuration.token,
                1
            )
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ) {
        doRequest(
            configuration.urlApi,
            ChangeRatingResponse::class.java
        ) {
            changeRating(
                configuration.accountId,
                articleId,
                if (good) 1 else 0,
                if (good) 0 else 1
            )
        }
    }

    override fun sendReview(
        articleId: Long,
        message: String
    ): SendReviewResponse {
        val request = CreateTicket.Request(
            configuration.token,
            configuration.clientEmail,
            configuration.clientName,
            message,
            articleId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            CreateTicket.Response::class.java
        ) {
            createTicket(request)
        }
        return when (response?.status) {
            "success" -> SendReviewResponse.Done
            else -> SendReviewResponse.Error(response?.code)
        }
    }
}