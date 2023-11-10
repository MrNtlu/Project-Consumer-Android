package com.mrntlu.projectconsumer.viewmodels.main.review

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent
import com.mrntlu.projectconsumer.repository.ReviewRepository
import com.mrntlu.projectconsumer.utils.Constants.SortReviewRequests
import com.mrntlu.projectconsumer.utils.NetworkListResponse
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

const val REVIEWLIST_SORT_KEY = "rv_list.review.sort"
const val REVIEWLIST_PAGE_KEY = "rv_list.review.page"
const val REVIEWLIST_SCROLL_POSITION_KEY = "rv_list.review.scroll_position"

class ReviewListUserViewModel @AssistedInject constructor(
    private val reviewRepository: ReviewRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val userId: String,
): ViewModel() {
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

    var sort: String = savedStateHandle[REVIEWLIST_SORT_KEY] ?: SortReviewRequests[0].request
        private set
    private var page: Int = savedStateHandle[REVIEWLIST_PAGE_KEY] ?: 1
    var scrollPosition: Int = savedStateHandle[REVIEWLIST_SCROLL_POSITION_KEY] ?: 0
        private set

    private val _reviewList = MutableLiveData<NetworkListResponse<List<ReviewWithContent>>>()
    val reviewList: LiveData<NetworkListResponse<List<ReviewWithContent>>> = _reviewList

    init {
        getReviews()
    }

    fun getReviews(shouldRefresh: Boolean = false) {
        if (shouldRefresh) {
            setPagePosition(1)
        }

        val prevList = arrayListOf<ReviewWithContent>()
        if (_reviewList.value?.data != null && page != 1) {
            prevList.addAll(_reviewList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            reviewRepository.getReviewsByUserId(
                userId, page, sort,
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

    fun setSort(newSort: String) {
        if (sort != newSort) {
            sort = newSort
            savedStateHandle[REVIEWLIST_SORT_KEY] = sort
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[REVIEWLIST_PAGE_KEY] = page
    }

    fun setScrollPosition(newPosition: Int) {
        if (!didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[REVIEWLIST_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            savedStateHandle: SavedStateHandle,
            userId: String
        ): ReviewListUserViewModel
    }

    companion object {
        fun provideReviewListUserViewModelFactory(
            factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, userId: String,
        ): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, userId) as T
                }
            }
        }
    }
}