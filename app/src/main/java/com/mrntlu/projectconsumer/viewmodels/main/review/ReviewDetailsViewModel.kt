package com.mrntlu.projectconsumer.viewmodels.main.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.models.main.review.ReviewDetails
import com.mrntlu.projectconsumer.repository.ReviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReviewDetailsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
): ViewModel() {

    private val _reviewDetails = MutableLiveData<NetworkResponse<DataResponse<ReviewDetails>>>()
    val reviewDetails: LiveData<NetworkResponse<DataResponse<ReviewDetails>>> = _reviewDetails

    fun getReviewDetails(id: String) = networkResponseFlowCollector(
        reviewRepository.getReviewDetails(id)
    ) { response ->
        _reviewDetails.value = response
    }

    fun deleteReview(body: IDBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.deleteReview(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun voteReview(body: IDBody): LiveData<NetworkResponse<DataResponse<Review>>> {
        val liveData = MutableLiveData<NetworkResponse<DataResponse<Review>>>()

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.voteReview(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}