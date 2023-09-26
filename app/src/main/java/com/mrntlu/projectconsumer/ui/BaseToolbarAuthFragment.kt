package com.mrntlu.projectconsumer.ui

import androidx.fragment.app.activityViewModels
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.shared.HomeDiscoverSharedViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.logInWith

abstract class BaseToolbarAuthFragment<T>: BaseFragment<T>() {
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()
    protected val viewModel: HomeDiscoverSharedViewModel by activityViewModels()

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

                if (Purchases.sharedInstance.isAnonymous) {
                    Purchases.sharedInstance.logInWith(
                        response.data.data.email,
                        onError = {},
                        onSuccess = { customerInfo, _ ->
                            handleCustomerInfo(customerInfo, response.data.data.isPremium)
                        }
                    )
                } else {
                    Purchases.sharedInstance.getCustomerInfoWith(onError = {}, onSuccess = {
                        handleCustomerInfo(it, response.data.data.isPremium)
                    })
                }

                (activity as? MainActivity)?.setBottomNavProfile(response.data.data.image ?: "")
            }
        }
    }

    private fun handleCustomerInfo(customerInfo: CustomerInfo, isPremium: Boolean) {
        val isMembershipActive = customerInfo.entitlements["premium_membership"]?.isActive

        if (isMembershipActive == true && !isPremium) {
            userSharedViewModel.updateMembership(UpdateMembershipBody(
                true,
                if (customerInfo.entitlements["premium_membership"]?.productIdentifier?.equals("watchlistfy_premium_1mo") == true)
                    1
                else
                    2
            )).observe(viewLifecycleOwner) { response ->
                if (response is NetworkResponse.Success)
                    userSharedViewModel.getBasicInfo()
            }
        } else if (
            (isMembershipActive == false || isMembershipActive == null) && isPremium
        ) {
            userSharedViewModel.updateMembership(UpdateMembershipBody(false, 0)).observe(viewLifecycleOwner) { response ->
                if (response is NetworkResponse.Success)
                    userSharedViewModel.getBasicInfo()
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            userSharedViewModel.userInfoResponse.removeObservers(this)
            viewModel.selectedTabIndex.removeObservers(this)
        }

        super.onDestroyView()
    }
}