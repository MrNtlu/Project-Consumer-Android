package com.mrntlu.projectconsumer.ui

import androidx.fragment.app.activityViewModels
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel

abstract class BaseToolbarAuthFragment<T>: BaseFragment<T>() {
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    protected fun onUserIncClicked() {
        (activity as? MainActivity)?.navigateToProfile()
    }

    protected fun onAnonymousIncClicked() {
        navController.navigate(R.id.action_global_authFragment)
    }

    protected fun setSharedObservers() {
        userSharedViewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            if (response is NetworkResponse.Success) {
                userSharedViewModel.userInfo = response.data.data

                (activity as? MainActivity)?.setBottomNavProfile(response.data.data.image ?: "")
            }
        }
    }

    override fun onDestroyView() {
        userSharedViewModel.userInfoResponse.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}