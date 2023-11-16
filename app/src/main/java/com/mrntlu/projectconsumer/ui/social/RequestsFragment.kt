package com.mrntlu.projectconsumer.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.RequestsAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.FriendRequestInteraction
import com.mrntlu.projectconsumer.models.auth.FriendRequest
import com.mrntlu.projectconsumer.models.auth.retrofit.AnswerFriendRequestBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.ui.dialog.SuccessDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.social.RequestsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestsFragment: BaseFragment<FragmentListBinding>() {
    private val viewModel: RequestsViewModel by viewModels()

    private lateinit var dialog: LoadingDialog
    private lateinit var successDialog: SuccessDialog
    private var confirmDialog: AlertDialog? = null

    private var requestsAdapter: RequestsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
            successDialog = SuccessDialog(it)
        }

        binding.topFAB.setGone()

        setToolbar()
        setObservers()
        setRecyclerView()
    }

    override fun onStart() {
        if (viewModel.scrollPosition > 0) {
            binding.listRV.scrollToPosition(viewModel.scrollPosition)
            viewModel.setScrollPosition(0)
        }

        super.onStart()
    }

    private fun setToolbar() {
        binding.listToolbar.apply {
            title = getString(R.string._notifications)

            setNavigationOnClickListener {
                if (!navController.popBackStack()) {
                    navController.navigate(RequestsFragmentDirections.actionGlobalNavigationDiscover())
                }
            }
        }
    }

    private fun setObservers() {
        if (!(viewModel.requestList.hasObservers() || viewModel.requestList.value is NetworkResponse.Success || viewModel.requestList.value is NetworkResponse.Loading))
            viewModel.getFriendRequests()

        viewModel.requestList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    requestsAdapter?.setErrorView(response.errorMessage)
                }
                NetworkResponse.Loading -> {
                    requestsAdapter?.setLoadingView()
                }
                is NetworkResponse.Success -> {

                    viewModel.viewModelScope.launch {
                        val list = response.data.data ?: arrayListOf()

                        requestsAdapter?.setData(list)
                    }
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.listRV.apply {
            val linearLayoutManager = LinearLayoutManager(this.context)

            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
            divider.apply {
                dividerThickness = context.dpToPx(1f)
                isLastItemDecorated = false
            }
            addItemDecoration(divider)

            layoutManager = linearLayoutManager

            requestsAdapter = RequestsAdapter(object: FriendRequestInteraction {
                override fun onAcceptClicked(item: FriendRequest, position: Int) {
                    if (confirmDialog != null && confirmDialog?.isShowing == true) {
                        confirmDialog?.dismiss()
                        confirmDialog = null
                    }

                    confirmDialog = context?.showConfirmationDialog("Do you want to accept ${item.sender.username}'s friend request?") {
                        viewModel.answerFriendRequest(AnswerFriendRequestBody(
                            item.id, 1
                        )).observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    successDialog.showDialog(response.data.message) {}

                                    requestsAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                }
                            }
                        }
                    }
                }

                override fun onIgnoreClicked(item: FriendRequest, position: Int) {
                    if (confirmDialog != null && confirmDialog?.isShowing == true) {
                        confirmDialog?.dismiss()
                        confirmDialog = null
                    }

                    confirmDialog = context?.showConfirmationDialog("Do you want to ignore ${item.sender.username}'s friend request?") {
                        viewModel.answerFriendRequest(AnswerFriendRequestBody(
                            item.id, 2
                        )).observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    successDialog.showDialog(response.data.message) {}

                                    requestsAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                }
                            }
                        }
                    }
                }

                override fun onDenyClicked(item: FriendRequest, position: Int) {
                    if (confirmDialog != null && confirmDialog?.isShowing == true) {
                        confirmDialog?.dismiss()
                        confirmDialog = null
                    }

                    confirmDialog = context?.showConfirmationDialog("Do you want to deny ${item.sender.username}'s friend request?") {
                        viewModel.answerFriendRequest(AnswerFriendRequestBody(
                            item.id, 0
                        )).observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    successDialog.showDialog(response.data.message) {}

                                    requestsAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                }
                            }
                        }
                    }
                }

                override fun onItemSelected(item: FriendRequest, position: Int) {
                    if (navController.currentDestination?.id == R.id.requestsFragment) {
                        viewModel.setScrollPosition(position)

                        val navWithAction = RequestsFragmentDirections.actionRequestsFragmentToProfileDisplayFragment(
                            item.sender.username
                        )
                        navController.navigate(navWithAction)
                    }
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getFriendRequests()
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {}
            })
            adapter = requestsAdapter
        }
    }

    override fun onDestroyView() {
        viewModel.requestList.removeObservers(viewLifecycleOwner)

        if (::successDialog.isInitialized)
            successDialog.dismissDialog()

        confirmDialog = null
        requestsAdapter = null
        super.onDestroyView()
    }
}