package ru.usedesk.knowledgebase_sdk.data.repository.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.CreateTicket

internal interface ApiRetrofit {
    @GET("support/{account_id}/list")
    fun getSections(
        @Path(value = "account_id", encoded = true) accountId: String,
        @Query("api_token") token: String
    ): Call<ResponseBody>

    @GET("support/{account_id}/articles/{article_id}")
    fun getArticleContent(
        @Path(value = "account_id", encoded = true) accountId: String,
        @Path(value = "article_id", encoded = true) articleId: Long,
        @Query("api_token") token: String
    ): Call<ResponseBody>

    @GET("support/{account_id}/articles/list")
    fun getArticles(
        @Path(value = "account_id", encoded = true) accountId: String,
        @Query("api_token") token: String,
        @Query("query") searchQuery: String,
        @Query("count") count: Int?,
        @Query("collection_ids") collectionIds: String?,
        @Query("category_ids") categoryIds: String?,
        @Query("article_ids") articleIds: String?,
        @Query("page") page: Long?,
        @Query("type") type: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?
    ): Call<ResponseBody>

    @GET("support/{account_id}/articles/{article_id}/add-views")
    fun addViews(
        @Path(value = "account_id", encoded = true) accountId: String,
        @Path(value = "article_id", encoded = true) articleId: Long,
        @Query("api_token") token: String,
        @Query("count") count: Int
    ): Call<ResponseBody>

    @GET("support/{account_id}/articles/{article_id}/change-rating")
    fun changeRating(
        @Path(value = "account_id", encoded = true) accountId: String,
        @Path(value = "article_id", encoded = true) articleId: Long,
        @Query("count_positive") positive: Int,
        @Query("count_negative") negative: Int
    ): Call<ResponseBody>

    @POST("create/ticket")
    fun createTicket(@Body request: CreateTicket.Request): Call<ResponseBody>
}