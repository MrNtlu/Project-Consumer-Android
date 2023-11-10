package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent

interface ReviewInteraction: Interaction<Review> {
    fun onEditClicked(item: Review, position: Int)
    fun onDeleteClicked(item: Review, position: Int)
    fun onLikeClicked(item: Review, position: Int)
    fun onProfileClicked(item: Review, position: Int)
}

interface ReviewWithContentInteraction: Interaction<ReviewWithContent> {
    fun onEditClicked(item: ReviewWithContent, position: Int)
    fun onDeleteClicked(item: ReviewWithContent, position: Int)
    fun onLikeClicked(item: ReviewWithContent, position: Int)
    fun onContentClicked(item: ReviewWithContent, position: Int)
}