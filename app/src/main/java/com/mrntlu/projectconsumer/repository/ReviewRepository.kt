package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.models.main.review.retrofit.ReviewBody
import com.mrntlu.projectconsumer.models.main.review.retrofit.UpdateReviewBody
import com.mrntlu.projectconsumer.models.main.review.retrofit.VoteReviewBody
import com.mrntlu.projectconsumer.service.retrofit.ReviewApiService
import com.mrntlu.projectconsumer.utils.networkBoundResourceWithoutCache
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import kotlinx.coroutines.delay
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val reviewApiService: ReviewApiService,
) {
    fun createReview(body: ReviewBody) = networkResponseFlow {
        reviewApiService.createReview(body)
    }

    fun getReviewsByContent(
        contentId: String,
        contentExternalId: String?,
        contentExternalIntId: Int?,
        contentType: String,
        page: Int,
        sort: String,
    ) = networkBoundResourceWithoutCache(
        isPaginating = page != 1,
        fetchNetwork = {
            reviewApiService.getReviewsByContent(contentId, contentExternalId, contentExternalIntId, contentType, page, sort)
        },
        emptyObjectCreator = {
            listOf<Review>()
        },
        handleResponse = { response ->
            Pair(
                response.data,
                page >= response.pagination.totalPage,
            )
        }
    )

    fun updateReview(body: UpdateReviewBody) = networkResponseFlow {
        reviewApiService.updateReview(body)
    }

    fun voteReview(body: VoteReviewBody) = networkResponseFlow {
        reviewApiService.voteReview(body)
    }

    fun deleteReview(body: IDBody) = networkResponseFlow {
        reviewApiService.deleteReview(body)
    }
}