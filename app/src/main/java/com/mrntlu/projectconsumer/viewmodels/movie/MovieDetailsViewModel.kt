package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.main.userlist.MovieWatchListBody
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val repository: UserListRepository,
): ViewModel() {

    private val _movieWatchList = MutableLiveData<NetworkResponse<String>>()
    val movieWatchList: LiveData<NetworkResponse<String>> = _movieWatchList

    fun createMovieWatchList(body: MovieWatchListBody) = viewModelScope.launch(Dispatchers.IO) {
        repository.createMovieWatchList(body).collect { response ->
            withContext(Dispatchers.Main) {
                _movieWatchList.value = response
            }
        }
    }
}