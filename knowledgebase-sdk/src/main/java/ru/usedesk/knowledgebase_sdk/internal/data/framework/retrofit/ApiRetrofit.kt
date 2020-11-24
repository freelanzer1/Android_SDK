package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;

public interface ApiRetrofit {
    @GET("{account_id}/list")
    Call<String> getSections(@Path(value = "account_id", encoded = true) String accountId,
                             @Query("api_token") String token);

    @GET("{account_id}/articles/{article_id}")
    Call<String> getArticleBody(@Path(value = "account_id", encoded = true) String accountId,
                                @Path(value = "article_id", encoded = true) String articleId,
                                @Query("api_token") String token);

    @GET("{account_id}/articles/list")
    Call<String> getArticlesBody(@Path(value = "account_id", encoded = true) String accountId,
                                 @Query("api_token") String token,
                                 @Query("query") String searchQuery,
                                 @Query("count") String count,
                                 @Query("collection_ids") String collectionIds,
                                 @Query("category_ids") String categoryIds,
                                 @Query("article_ids") String articleIds,
                                 @Query("page") String page,
                                 @Query("type") UsedeskSearchQuery.Type type,
                                 @Query("sort") UsedeskSearchQuery.Sort sort,
                                 @Query("order") UsedeskSearchQuery.Order order);

    @GET("{account_id}/articles/{article_id}/add-views")
    Call<String> addViews(@Path(value = "account_id", encoded = true) String accountId,
                          @Path(value = "article_id", encoded = true) long articleId,
                          @Query("api_token") String token,
                          @Query("count") int count);
}
