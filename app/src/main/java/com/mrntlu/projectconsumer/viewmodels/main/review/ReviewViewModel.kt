package com.mrntlu.projectconsumer.viewmodels.main.review

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.repository.ReviewRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.setData
import com.mrntlu.projectconsumer.utils.setPaginationLoading
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val REVIEW_SORT_KEY = "rv.review.sort"
const val REVIEW_PAGE_KEY = "rv.review.page"
const val REVIEW_SCROLL_POSITION_KEY = "rv.review.scroll_position"

class ReviewViewModel @AssistedInject constructor(
    private val reviewRepository: ReviewRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted("ci") private val contentId: String,
    @Assisted("cei") private val contentExternalId: String?,
    @Assisted private val contentExternalIntId: Int?,
    @Assisted("ct") private val contentType: String,
): ViewModel() {
    // Variable for detecting orientation change
    var didOrientationChange = false
    private var currentOrientation: Orientation = Orientation.Idle

    fun setNewOrientation(newOrientation: Orientation) {
        if (currentOrientation == Orientation.Idle) {
            currentOrientation = newOrientation
        } else if (newOrientation != currentOrientation) {
            didOrientationChange = true

            currentOrientation = newOrientation
        }
    }

    var sort: String = savedStateHandle[REVIEW_SORT_KEY] ?: Constants.SortReviewRequests[0].request
        private set
    private var page: Int = savedStateHandle[REVIEW_PAGE_KEY] ?: 1
    var scrollPosition: Int = savedStateHandle[REVIEW_SCROLL_POSITION_KEY] ?: 0
        private set

    private val _reviewList = MutableLiveData<NetworkListResponse<List<Review>>>()
    val reviewList: LiveData<NetworkListResponse<List<Review>>> = _reviewList

    init {
        getReviews()
    }

    fun getReviews(shouldRefresh: Boolean = false) {
        if (shouldRefresh) {
            setPagePosition(1)
        }

        val prevList = arrayListOf<Review>()
        if (_reviewList.value?.data != null && page != 1) {
            prevList.addAll(_reviewList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.getReviewsByContent(
                contentId, contentExternalId, contentExternalIntId,
                contentType, page, sort,
            ).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _reviewList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _reviewList.value = setPaginationLoading(prevList)
                    } else
                        _reviewList.value = response
                }
            }
        }
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

    fun setSort(newSort: String) {
        if (sort != newSort) {
            sort = newSort
            savedStateHandle[REVIEW_SORT_KEY] = sort
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[REVIEW_PAGE_KEY] = page
    }

    fun setScrollPosition(newPosition: Int) {
        if (!didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[REVIEW_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            savedStateHandle: SavedStateHandle,
            @Assisted("ci") contentId: String,
            @Assisted("cei") contentExternalId: String?,
            contentExternalIntId: Int?,
            @Assisted("ct") contentType: String,
        ): ReviewViewModel
    }

    companion object {
        fun provideReviewViewModelFactory(
            factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, contentId: String,
            contentExternalId: String?, contentExternalIntId: Int?, contentType: String,
        ): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, contentId, contentExternalId, contentExternalIntId, contentType) as T
                }
            }
        }
    }
}