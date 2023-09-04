package com.mrntlu.projectconsumer.ui

import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutUserAnonymousBinding
import com.mrntlu.projectconsumer.databinding.LayoutUserBinding
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel

abstract class BaseToolbarAuthFragment<T>: BaseFragment<T>() {
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    protected fun onUserIncClicked() {
        (activity as? MainActivity)?.navigateToProfile()
    }

    protected fun onAnonymousIncClicked() {
        navController.navigate(R.id.action_global_authFragment)
    }

    protected fun setSharedObservers(progressBar: ProgressBar, userInc: LayoutUserBinding, anonymousInc: LayoutUserAnonymousBinding) {
        userSharedViewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            progressBar.setVisibilityByCondition(response != NetworkResponse.Loading)
            userInc.root.setVisibilityByCondition(response !is NetworkResponse.Success)

            if (response is NetworkResponse.Success) {
                userSharedViewModel.userInfo = response.data.data

                userInc.userIV.loadWithGlide(
                    response.data.data.image ?: "",
                    userInc.userPlaceHolderIV,
                    userInc.userIVProgressBar,
                ) {
                    transform(CenterCrop())
                }
            }
        }

        sharedViewModel.isAuthenticated.observe(viewLifecycleOwner) {
            if (!it) {
                userInc.root.setGone()
                progressBar.setGone()
            }

            anonymousInc.root.setVisibilityByCondition(it)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            sharedViewModel.isAuthenticated.removeObservers(this)
            userSharedViewModel.userInfoResponse.removeObservers(this)
        }

        super.onDestroyView()
    }
}