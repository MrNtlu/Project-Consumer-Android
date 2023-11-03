package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.review.Review

interface ReviewInteraction: Interaction<Review> {
    fun onEditClicked(item: Review, position: Int)
    fun onDeleteClicked(item: Review, position: Int)
    fun onLikeClicked(item: Review, position: Int)
}