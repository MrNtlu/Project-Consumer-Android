package com.mrntlu.projectconsumer.viewmodels.main.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.models.main.review.retrofit.ReviewBody
import com.mrntlu.projectconsumer.models.main.review.retrofit.UpdateReviewBody
import com.mrntlu.projectconsumer.repository.ReviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReviewCreateViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
): ViewModel() {

    fun createReview(body: ReviewBody): LiveData<NetworkResponse<DataResponse<Review>>> {
        val liveData = MutableLiveData<NetworkResponse<DataResponse<Review>>>()

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.createReview(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun updateReview(body: UpdateReviewBody): LiveData<NetworkResponse<DataResponse<Review>>> {
        val liveData = MutableLiveData<NetworkResponse<DataResponse<Review>>>()

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.updateReview(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}