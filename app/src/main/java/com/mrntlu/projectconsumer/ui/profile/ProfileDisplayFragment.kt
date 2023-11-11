package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentProfileDisplayBinding
import com.mrntlu.projectconsumer.ui.BaseProfileFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setInvisible
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.main.profile.ProfileDisplayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileDisplayFragment : BaseProfileFragment<FragmentProfileDisplayBinding>() {
    private val viewModel: ProfileDisplayViewModel by viewModels()

    private lateinit var dialog: LoadingDialog
    private var isResponseFailed = false

    private val args: ProfileDisplayFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setToolbar()
        setObservers()
    }

    private fun setToolbar() {
        binding.profileDisplayToolbar.apply {
            title = getString(R.string.profile)
            setOnMenuItemClickListener {
                hideKeyboard()

                when(it.itemId) {
                    R.id.shareMenu -> {
                        //TODO Share URL
                    }
                }

                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.userProfileResponse.value != null) {
            binding.loadingLayout.setVisible()
            viewModel.getUserProfile(args.profileUsername)
        }

        if (userInfo?.image != null)
            setImage(
                userInfo?.image ?: "",
                binding.profileDisplayIV,
                binding.profileDisplayPlaceHolderIV,
                binding.profileDisplayImageProgressBar,
            )
    }

    override fun onStop() {
        if (userInfo?.image != null)
            Glide.with(this).clear(binding.profileDisplayIV)
        super.onStop()
    }

    private fun setObservers() {
        if (!(viewModel.userProfileResponse.hasObservers() || viewModel.userProfileResponse.value is NetworkResponse.Success || viewModel.userProfileResponse.value is NetworkResponse.Loading))
            viewModel.getUserProfile(args.profileUsername)

        viewModel.userProfileResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                if (response is NetworkResponse.Loading)
                    loadingLayout.setVisible()
                else if (response is NetworkResponse.Failure)
                    loadingLayout.setGone()

                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)
                legendContentRV.setVisibilityByCondition(response is NetworkResponse.Failure)
                reviewRV.setVisibilityByCondition(response is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            cancelButton.setGone()

                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        userInfo = response.data.data

                        viewModel.viewModelScope.launch {
                            setUI(
                                profileDisplayIV, profileDisplayPlaceHolderIV,
                                profileDisplayImageProgressBar, profileDisplayUsernameTV, movieStatTV, tvStatTV,
                                gameStatTV, animeStatTV, movieWatchedTV, tvWatchedTV,
                                animeWatchedTV, gamePlayedTV, profileDisplayLevelBar, profileDisplayLevelTV,
                            )
                            setUI()
                            setListeners()
                            setRecyclerView(
                                legendContentRV,
                                reviewRV,
                                onClick = { item ->
                                    if (navController.currentDestination?.id == R.id.profileDisplayFragment) {
                                        val navWithAction = when(ContentType.fromStringRequest(item.contentType)) {
                                            ContentType.MOVIE -> ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToMovieDetailsFragment(item.id)
                                            ContentType.TV -> ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToTvDetailsFragment(item.id)
                                            ContentType.ANIME -> ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToAnimeDetailsFragment(item.id)
                                            ContentType.GAME -> ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToGameDetailsFragment(item.id)
                                        }
                                        navController.navigate(navWithAction)
                                    }
                                },
                                onReviewClick = { item ->
                                    if (navController.currentDestination?.id == R.id.profileDisplayFragment) {
                                        val navWithAction = ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToReviewDetailsFragment(item.id)
                                        navController.navigate(navWithAction)
                                    }
                                }
                            )
                            loadingLayout.setGone()
                        }
                    }
                    else -> {}
                }
            }
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getUserProfile(args.profileUsername)
        }
    }

    private fun setUI() {
        binding.apply {
            profileFriendRequestButton.setVisibilityByCondition(args.isSelf)
            premiumAnimation.setVisibilityByCondition(userInfo?.isPremium == false)
            profileDisplayToolbar.subtitle = userInfo?.username
            if (userInfo?.reviews?.isNotEmpty() == true)
                seeAllButtonFirst.setVisible()
            else
                seeAllButtonFirst.setInvisible()
        }
    }

    private fun setListeners() {
        binding.apply {
            profileDisplayToolbar.setNavigationOnClickListener { navController.popBackStack() }

            seeAllButtonFirst.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.profileDisplayFragment) {
                    val navWithAction = ProfileDisplayFragmentDirections.actionProfileDisplayFragmentToReviewListUserFragment(userInfo!!.id)
                    navController.navigate(navWithAction)
                }
            }

            profileFriendRequestButton.setSafeOnClickListener {
                //TODO Implement
            }

            errorLayoutInc.refreshButton.setSafeOnClickListener {
                viewModel.getUserProfile(args.profileUsername)
            }

            legendInfoButton.setSafeOnClickListener {
                context?.showInfoDialog(getString(R.string.legend_content_info))
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.userProfileResponse.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }

        legendContentAdapter = null
        reviewPreviewAdapter = null

        super.onDestroyView()
    }
}