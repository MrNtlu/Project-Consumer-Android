package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.adapters.PremiumAdapter
import com.mrntlu.projectconsumer.databinding.LayoutPremiumBinding
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import dagger.hilt.android.AndroidEntryPoint

interface OnPremiumDismissCallback {
    fun onDismissed(isPurchased: Boolean)
}

@AndroidEntryPoint
class PremiumBottomSheet(
    private val onPremiumDismissCallback: OnPremiumDismissCallback
): BottomSheetDialogFragment() {
    constructor(): this(object: OnPremiumDismissCallback{ override fun onDismissed(isPurchased: Boolean) {} })

    companion object {
        const val TAG = "PremiumBottomSheet"
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    private var _binding: LayoutPremiumBinding? = null
    private val binding get() = _binding!!

    private var premiumAdapter: PremiumAdapter? = null
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutPremiumBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            loadingDialog = LoadingDialog(it)
        }

        dialog?.setOnShowListener {
            isCancelable = false

            (it as? BottomSheetDialog)?.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }
        }

        Purchases.sharedInstance.getOfferingsWith(onError = { error ->
            dismiss()
            context?.showErrorDialog(error.message)
        },
        onSuccess = { offerings ->
            val packageList = offerings.current?.availablePackages?.takeUnless { it.isEmpty() }

            setRecyclerView(packageList ?: arrayListOf())
            setListeners()
        })
    }

    private fun setRecyclerView(packageList: List<Package>) {
        binding.offeringsRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            premiumAdapter = PremiumAdapter(packageList) { pckg ->
                activity?.let {
                    if (::loadingDialog.isInitialized)
                        loadingDialog.showLoadingDialog()

                    Purchases.sharedInstance.purchaseWith(
                        PurchaseParams.Builder(it, pckg).build(),
                        onError = { error, userCancelled ->
                            if (::loadingDialog.isInitialized)
                                loadingDialog.dismissDialog()

                            context?.showErrorDialog(if (userCancelled) "Cancelled" else error.message)
                        },
                        onSuccess = { _, customerInfo ->
                            val premiumEntitlement = customerInfo.entitlements["premium_membership"]
                            val isMembershipActive = premiumEntitlement?.isActive

                            if (isMembershipActive == true) {
                                userSharedViewModel.updateMembership(UpdateMembershipBody(
                                    true,
                                    if (premiumEntitlement.productIdentifier == "watchlistfy_premium_1mo")
                                        1
                                    else
                                        2
                                )).observe(viewLifecycleOwner) { response ->
                                    when(response) {
                                        is NetworkResponse.Failure -> {
                                            if (::loadingDialog.isInitialized)
                                                loadingDialog.dismissDialog()

                                            dismiss()
                                            context?.showErrorDialog(response.errorMessage)
                                        }
                                        is NetworkResponse.Success -> {
                                            if (::loadingDialog.isInitialized)
                                                loadingDialog.dismissDialog()

                                            userSharedViewModel.getBasicInfo()
                                            onPremiumDismissCallback.onDismissed(true)
                                            dismiss()
                                        }
                                        else -> {}
                                    }
                                }
                            } else
                                dismiss()
                        }
                    )
                }
            }
            adapter = premiumAdapter
        }
    }

    private fun setListeners() {
        binding.notNowButton.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        premiumAdapter = null

        super.onDestroyView()
    }
}