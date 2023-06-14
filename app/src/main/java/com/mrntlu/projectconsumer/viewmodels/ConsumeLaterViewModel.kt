package com.mrntlu.projectconsumer.viewmodels

import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConsumeLaterViewModel @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository,
): ViewModel(){


}