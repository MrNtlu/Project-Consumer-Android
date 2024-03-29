package com.mrntlu.projectconsumer.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ConsumeLaterPreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.ui.BaseProfileFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants.BASE_DOMAIN_URL
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setInvisible
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.main.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@ExperimentalBadgeUtils
@AndroidEntryPoint
class ProfileFragment : BaseProfileFragment<FragmentProfileBinding>() {
    private val viewModel: ProfileViewModel by viewModels()

    private var isResponseFailed = false

    private var confirmDialog: AlertDialog? = null
    private var consumeLaterAdapter: ConsumeLaterPreviewAdapter? = null
    private lateinit var dialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setToolbar()
        setConsumeLaterRV()
        setObservers()
    }

    private fun setToolbar() {
        binding.profileToolbar.apply {
            title = getString(R.string.profile)
            setOnMenuItemClickListener {
                hideKeyboard()

                when(it.itemId) {
                    R.id.settingsMenu -> {
                        //TODO Move to a normal navigation
                        navController.navigate(R.id.action_global_settingsFragment)
                    }
                    R.id.shareMenu -> {
                        if (userInfo != null) {
                            val shareURL = "$BASE_DOMAIN_URL/profile/${userInfo!!.username}"

                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, getString(R.string.share_profile))
                                putExtra(Intent.EXTRA_TEXT, shareURL)
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    }
                }

                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.userInfoResponse.value != null) {
            binding.loadingLayout.setVisible()
            viewModel.getUserInfo()
        }

        if (userInfo?.image != null)
            setImage(
                userInfo?.image ?: "",
                binding.profileIV,
                binding.profilePlaceHolderIV,
                binding.profileImageProgressBar,
            )
    }

    override fun onStop() {
        if (userInfo?.image != null)
            Glide.with(this).clear(binding.profileIV)
        super.onStop()
    }

    private fun setObservers() {
        if (!(viewModel.userInfoResponse.hasObservers() || viewModel.userInfoResponse.value is NetworkResponse.Success || viewModel.userInfoResponse.value is NetworkResponse.Loading))
            viewModel.getUserInfo()

        viewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                if (response is NetworkResponse.Loading)
                    loadingLayout.setVisible()
                else if (response is NetworkResponse.Failure)
                    loadingLayout.setGone()

                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)
                legendContentRV.setVisibilityByCondition(response is NetworkResponse.Failure)

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
                                profileIV, profilePlaceHolderIV,
                                profileImageProgressBar, profileUsernameTV, movieStatTV, tvStatTV,
                                gameStatTV, animeStatTV, movieWatchedTV, tvWatchedTV,
                                animeWatchedTV, gamePlayedTV, profileLevelBar, profileLevelTV,
                            )
                            setUI()
                            setListeners()
                            setRecyclerView(
                                legendContentRV,
                                reviewRV,
                                onClick = { item ->
                                    if (navController.currentDestination?.id == R.id.navigation_profile) {
                                        val navWithAction = when(ContentType.fromStringRequest(item.contentType)) {
                                            ContentType.ANIME -> ProfileFragmentDirections.actionNavigationProfileToAnimeDetailsFragment(item.id)
                                            ContentType.MOVIE -> ProfileFragmentDirections.actionNavigationProfileToMovieDetailsFragment(item.id)
                                            ContentType.TV -> ProfileFragmentDirections.actionNavigationProfileToTvDetailsFragment(item.id)
                                            ContentType.GAME -> ProfileFragmentDirections.actionNavigationProfileToGameDetailsFragment(item.id)
                                        }
                                        navController.navigate(navWithAction)
                                    }
                                },
                                onReviewClick = { item ->
                                    if (navController.currentDestination?.id == R.id.navigation_profile) {
                                        val navWithAction = ProfileFragmentDirections.actionNavigationProfileToReviewDetailsFragment(item.id)
                                        navController.navigate(navWithAction)
                                    }
                                }
                            )
                            consumeLaterAdapter?.setData(userInfo?.watchLater?.toCollection(ArrayList()) ?: arrayListOf())
                            loadingLayout.setGone()
                        }
                    }
                    else -> {}
                }
            }
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getUserInfo()
        }
    }

    private fun setUI() {
        binding.apply {
            if (userInfo?.watchLater?.isNotEmpty() == true)
                seeAllButtonFirst.setVisible()
            else
                seeAllButtonFirst.setInvisible()

            if (userInfo?.reviews?.isNotEmpty() == true)
                reviewButton.setVisible()
            else
                reviewButton.setInvisible()

            if (userInfo != null) {
                val badgeDrawable = BadgeDrawable.create(root.context)

                badgeDrawable.number = userInfo!!.friendRequestCount
                badgeDrawable.isVisible = userInfo!!.friendRequestCount > 0

                BadgeUtils.attachBadgeDrawable(badgeDrawable, profileFriendsButton)
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            profileFriendsButton.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_profile) {
                    navController.navigate(R.id.action_navigation_profile_to_socialHubFragment)
                }
            }

            profileUserListButton.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_profile) {
                    navController.navigate(R.id.action_navigation_profile_to_navigation_user_list)
                }
            }

            seeAllButtonFirst.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_profile) {
                    navController.navigate(R.id.action_navigation_profile_to_navigation_later)
                }
            }

            reviewButton.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_profile) {
                    val navWithAction = ProfileFragmentDirections.actionNavigationProfileToReviewListUserFragment(userInfo!!.id)
                    navController.navigate(navWithAction)
                }
            }

            errorLayoutInc.refreshButton.setSafeOnClickListener {
                viewModel.getUserInfo()
            }

            profileChangeImageButton.setSafeOnClickListener {
                activity?.let {
                    val bottomSheet = ProfileEditBottomSheet(userInfo?.image ?: "") { image ->
                        userSharedViewModel.updateUserImage(UpdateUserImageBody(image)).observe(viewLifecycleOwner) { response ->
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

                                    (activity as? MainActivity)?.setBottomNavProfile(image)
                                    userSharedViewModel.getBasicInfo()
                                    viewModel.getUserInfo()
                                }
                            }
                        }
                    }
                    bottomSheet.show(it.supportFragmentManager, ProfileEditBottomSheet.TAG)
                }
            }

            //TODO Implement later
//            profileDiaryButton.setSafeOnClickListener {
//                if (navController.currentDestination?.id == R.id.navigation_profile) {
//                    navController.navigate(R.id.action_navigation_profile_to_diaryFragment)
//                }
//            }

            legendInfoButton.setSafeOnClickListener {
                context?.showInfoDialog(getString(R.string.legend_content_info))
            }
        }
    }

    private fun setConsumeLaterRV() {
        binding.watchLaterRV.apply {
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager

            consumeLaterAdapter = ConsumeLaterPreviewAdapter(binding.root.context.dpToPxFloat(6f), object: ConsumeLaterInteraction {
                override fun onDeletePressed(item: ConsumeLaterResponse, position: Int) {
                    confirmDialog = context?.showConfirmationDialog(getString(R.string.remove_from_later)) {
                        val deleteConsumerLiveData = viewModel.deleteConsumeLater(IDBody(item.id))

                        deleteConsumerLiveData.observe(viewLifecycleOwner) { response ->
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

                                    consumeLaterAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                }
                            }
                        }
                    }
                }

                override fun onAddToListPressed(item: ConsumeLaterResponse, position: Int) {}

                override fun onDiscoverButtonPressed() {
                    (activity as? MainActivity)?.navigateToHome()
                }

                override fun onItemSelected(item: ConsumeLaterResponse, position: Int) {
                    confirmDialog?.dismiss()

                    if (navController.currentDestination?.id == R.id.navigation_profile) {
                        when(ContentType.fromStringRequest(item.contentType)) {
                            ContentType.ANIME -> {
                                val navWithAction = ProfileFragmentDirections.actionNavigationProfileToAnimeDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            ContentType.MOVIE -> {
                                val navWithAction = ProfileFragmentDirections.actionNavigationProfileToMovieDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            ContentType.TV -> {
                                val navWithAction = ProfileFragmentDirections.actionNavigationProfileToTvDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            ContentType.GAME -> {
                                val navWithAction = ProfileFragmentDirections.actionGlobalGameDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                        }
                    }
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getUserInfo()
                }

                override fun onCancelPressed() {}

                override fun onExhaustButtonPressed() {}
            })
            adapter = consumeLaterAdapter
        }
    }

    override fun onDestroyView() {
        legendContentAdapter = null
        consumeLaterAdapter = null
        reviewPreviewAdapter = null

        viewLifecycleOwner.apply {
            sharedViewModel.networkStatus.removeObservers(this)
            viewModel.userInfoResponse.removeObservers(this)
        }

        confirmDialog?.dismiss()
        confirmDialog = null
        super.onDestroyView()
    }
}