package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ConsumeLaterPreviewAdapter
import com.mrntlu.projectconsumer.adapters.LegendContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.main.profile.ProfileViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    @Inject lateinit var tokenManager: TokenManager

    private val viewModel: ProfileViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    private var isResponseFailed = false
    private var userInfo: UserInfo? = null

    private var legendContentAdapter: LegendContentAdapter? = null
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

        setMenu()
        setConsumeLaterRV()
        setObservers()
    }

    override fun onStart() {
        super.onStart()

        binding.loadingLayout.setVisible()
        viewModel.getUserInfo()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                hideKeyboard()

                when(menuItem.itemId) {
                    R.id.settingsMenu -> {
                        navController.navigate(R.id.action_global_settingsFragment)
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

                        setUI()
                        setListeners()
                        setRecyclerView()
                        consumeLaterAdapter?.setData(userInfo?.watchLater?.toCollection(ArrayList()) ?: arrayListOf())
                        loadingLayout.setGone()
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
        userInfo?.let {
            binding.apply {
                profileIV.loadWithGlide(
                    it.image ?: "",
                    profilePlaceHolderIV,
                    profileImageProgressBar,
                ) {
                    centerCrop()
                }

                profileUsernameTV.text = it.username

                movieStatTV.text = it.movieCount.toString()
                tvStatTV.text = it.tvCount.toString()
                gameStatTV.text = it.gameCount.toString()
                animeStatTV.text = it.animeCount.toString()

                val levelStr = "${it.level} lv."
                profileLevelBar.progress = it.level
                profileLevelTV.text = levelStr
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            profileUserListButton.setSafeOnClickListener {
                navController.navigate(R.id.action_navigation_profile_to_navigation_user_list)
            }

            seeAllButtonFirst.setSafeOnClickListener {
                navController.navigate(R.id.action_navigation_profile_to_navigation_later)
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

                                    userSharedViewModel.getBasicInfo()
                                    viewModel.getUserInfo()
                                }
                            }
                        }
                    }
                    bottomSheet.show(it.supportFragmentManager, ProfileEditBottomSheet.TAG)
                }
            }

            profileDiaryButton.setSafeOnClickListener {
                navController.navigate(R.id.action_navigation_profile_to_diaryFragment)
            }

            legendInfoButton.setSafeOnClickListener {
                context?.showInfoDialog(getString(R.string.legend_content_info))
            }
        }
    }

    private fun setRecyclerView() {
        binding.legendContentRV.apply {
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager

            legendContentAdapter = LegendContentAdapter(!sharedViewModel.isLightTheme(), userInfo?.legendContent ?: arrayListOf()) { item ->
                when(Constants.ContentType.fromStringRequest(item.contentType)) {
                    Constants.ContentType.ANIME -> {
                        val navWithAction = ProfileFragmentDirections.actionGlobalAnimeDetailsFragment(item.id)
                        navController.navigate(navWithAction)
                    }
                    Constants.ContentType.MOVIE -> {
                        val navWithAction = ProfileFragmentDirections.actionGlobalMovieDetailsFragment(item.id)
                        navController.navigate(navWithAction)
                    }
                    Constants.ContentType.TV -> {
                        val navWithAction = ProfileFragmentDirections.actionGlobalTvDetailsFragment(item.id)
                        navController.navigate(navWithAction)
                    }
                    Constants.ContentType.GAME -> {
                        val navWithAction = ProfileFragmentDirections.actionGlobalGameDetailsFragment(item.id)
                        navController.navigate(navWithAction)
                    }
                }
            }
            adapter = legendContentAdapter
        }
    }

    private fun setConsumeLaterRV() {
        binding.watchLaterRV.apply {
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager

            consumeLaterAdapter = ConsumeLaterPreviewAdapter(!sharedViewModel.isLightTheme(), object: ConsumeLaterInteraction {
                override fun onDeletePressed(item: ConsumeLaterResponse, position: Int) {
                    context?.showConfirmationDialog(getString(R.string.remove_from_later)) {
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
                    (activity as? MainActivity)?.navigateToDiscover()
                }

                override fun onItemSelected(item: ConsumeLaterResponse, position: Int) {
                    when(Constants.ContentType.fromStringRequest(item.contentType)) {
                        Constants.ContentType.ANIME -> {
                            val navWithAction = ProfileFragmentDirections.actionGlobalAnimeDetailsFragment(item.contentID)
                            navController.navigate(navWithAction)
                        }
                        Constants.ContentType.MOVIE -> {
                            val navWithAction = ProfileFragmentDirections.actionGlobalMovieDetailsFragment(item.contentID)
                            navController.navigate(navWithAction)
                        }
                        Constants.ContentType.TV -> {
                            val navWithAction = ProfileFragmentDirections.actionGlobalTvDetailsFragment(item.contentID)
                            navController.navigate(navWithAction)
                        }
                        Constants.ContentType.GAME -> {
                            val navWithAction = ProfileFragmentDirections.actionGlobalGameDetailsFragment(item.contentID)
                            navController.navigate(navWithAction)
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

        viewLifecycleOwner.apply {
            sharedViewModel.networkStatus.removeObservers(this)
            viewModel.userInfoResponse.removeObservers(this)
        }

        super.onDestroyView()
    }
}