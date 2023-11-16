package com.mrntlu.projectconsumer.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.FriendsAdapter
import com.mrntlu.projectconsumer.databinding.FragmentSocialHubBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.viewmodels.main.social.SocialHubViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SocialHubFragment: BaseFragment<FragmentSocialHubBinding>() {
    private val viewModel: SocialHubViewModel by viewModels()

    private var friendsAdapter: FriendsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setObservers()
        setRecyclerView()
    }

    private fun setToolbar() {
        binding.socialHubToolbar.apply {
            setNavigationOnClickListener { navController.popBackStack() }

            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.addFriendMenu -> {
                        activity?.let { activity ->
                            val bottomSheet = AddFriendBottomSheet { username ->
                                if (navController.currentDestination?.id == R.id.socialHubFragment) {
                                    navController.navigate(SocialHubFragmentDirections.actionSocialHubFragmentToProfileDisplayFragment(username))
                                }
                            }
                            bottomSheet.show(activity.supportFragmentManager, AddFriendBottomSheet.TAG)
                        }
                    }
                    R.id.notificationMenu -> {
                        if (navController.currentDestination?.id == R.id.socialHubFragment) {
                            navController.navigate(R.id.action_socialHubFragment_to_requestsFragment)
                        }
                    }
                }

                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.friendList.value != null) {
            friendsAdapter?.setLoadingView()
            viewModel.getFriends()
        }
    }

    private fun setObservers() {
        if (!(viewModel.friendList.hasObservers() || viewModel.friendList.value is NetworkResponse.Success || viewModel.friendList.value is NetworkResponse.Loading))
            viewModel.getFriends()

        viewModel.friendList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    friendsAdapter?.setErrorView(response.errorMessage)
                }
                NetworkResponse.Loading -> {
                    friendsAdapter?.setLoadingView()
                }
                is NetworkResponse.Success -> {
                    viewModel.viewModelScope.launch {
                        friendsAdapter?.setData(response.data.data)
                    }
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.socialHubRV.apply {
            val linearLayoutManager = LinearLayoutManager(this.context)

            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
            divider.apply {
                dividerThickness = context.dpToPx(1f)
                isLastItemDecorated = true
            }
            addItemDecoration(divider)

            layoutManager = linearLayoutManager

            friendsAdapter = FriendsAdapter(object: Interaction<BasicUserInfo> {
                override fun onItemSelected(item: BasicUserInfo, position: Int) {
                    if (navController.currentDestination?.id == R.id.socialHubFragment) {
                        navController.navigate(SocialHubFragmentDirections.actionSocialHubFragmentToProfileDisplayFragment(item.username))
                    }
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getFriends()
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {}
            })
            adapter = friendsAdapter
        }
    }

    override fun onDestroyView() {
        viewModel.friendList.removeObservers(viewLifecycleOwner)

        friendsAdapter = null
        super.onDestroyView()
    }
}