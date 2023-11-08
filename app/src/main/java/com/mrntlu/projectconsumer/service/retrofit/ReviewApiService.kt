package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.models.main.review.ReviewByContent
import com.mrntlu.projectconsumer.models.main.review.retrofit.ReviewBody
import com.mrntlu.projectconsumer.models.main.review.retrofit.UpdateReviewBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewApiService {
    @POST("review")
    suspend fun createReview(@Body body: ReviewBody): Response<DataResponse<Review>>

    @GET("review")
    suspend fun getReviewsByContent(
        @Query("content_id") contentId: String,
        @Query("content_external_id") contentExternalId: String?,
        @Query("content_external_int_id") contentExternalIntId: Int?,
        @Query("content_type") contentType: String,
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<DataPaginationResponse<Review>>

    @GET("review/user")
    suspend fun getReviewsByUserID(
        @Query("user_id") userId: String,
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<DataPaginationResponse<ReviewByContent>>

    @PATCH("review")
    suspend fun updateReview(@Body body: UpdateReviewBody): Response<DataResponse<Review>>

    @PATCH("review/like")
    suspend fun voteReview(@Body body: IDBody): Response<DataResponse<Review>>

    @HTTP(method = "DELETE", path = "review", hasBody = true)
    suspend fun deleteReview(@Body body: IDBody): Response<MessageResponse>
}