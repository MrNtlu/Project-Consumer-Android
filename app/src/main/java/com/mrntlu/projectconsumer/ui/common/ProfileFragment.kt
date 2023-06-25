package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.main.ProfileViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private companion object {
        private const val KEY_VALUE = "content_type"
    }

    private val viewModel: ProfileViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    private var gridCount = 3
    private var isResponseFailed = false
    private var userInfo: UserInfo? = null
    private var contentType: Constants.ContentType = Constants.ContentType.MOVIE
    private var contentAdapter: ContentAdapter<ContentModel>? = null
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

        savedInstanceState?.let {
            contentType = Constants.ContentType.fromStringValue(
                it.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setObservers()
    }

    override fun onStart() {
        super.onStart()

        binding.loadingLayout.setVisible()
        viewModel.getUserInfo()
    }

    private fun setObservers() {
        if (!(viewModel.userInfoResponse.hasObservers() || viewModel.userInfoResponse.value is NetworkResponse.Success || viewModel.userInfoResponse.value is NetworkResponse.Loading))
            viewModel.getUserInfo()

        viewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)
                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)
                legendContentRV.setVisibilityByCondition(response is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            errorLayoutInc.cancelButton.setGone()

                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        userInfo = response.data.data

                        setUI()
                        setListeners()
                        contentAdapter?.setData(getLegendContentList())
                    }
                    else -> {}
                }
            }
        }

        sharedViewModel.windowSize.observe(viewLifecycleOwner) {
            val widthSize: WindowSizeClass = it

            gridCount = when(widthSize) {
                WindowSizeClass.COMPACT -> 2
                WindowSizeClass.MEDIUM -> 3
                WindowSizeClass.EXPANDED -> 5
            }

            setRecyclerView()
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getUserInfo()
        }
    }

    private fun getLegendContentList() = when(contentType) {
        Constants.ContentType.ANIME -> userInfo!!.legendAnimeList
        Constants.ContentType.MOVIE -> userInfo!!.legendMovieList
        Constants.ContentType.TV -> userInfo!!.legendTVList
        Constants.ContentType.GAME -> userInfo!!.legendGameList
    }.toCollection(ArrayList())

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

                profileContentTabLayout.apply {
                    if (profileContentTabLayout.tabCount < Constants.TabList.size) {
                        for (tab in Constants.TabList) {
                            addTab(
                                profileContentTabLayout.newTab().setText(tab),
                                tab == contentType.value
                            )
                        }
                    }
                }


                val levelStr = "${it.level} lv."
                profileLevelBar.progress = it.level
                profileLevelTV.text = levelStr
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            profileChangeImageButton.setOnClickListener {
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

            profileMyListButton.setOnClickListener {

            }

            profileDiaryButton.setOnClickListener {

            }

            legendInfoButton.setOnClickListener {
                context?.showInfoDialog(getString(R.string.legend_content_info))
            }

            profileContentTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position) {
                        0 -> {
                            contentType = Constants.ContentType.MOVIE
                        }
                        1 -> {
                            contentType = Constants.ContentType.TV
                        }
                        2 -> {
                            contentType = Constants.ContentType.ANIME
                        }
                        3 -> {
                            contentType = Constants.ContentType.GAME
                        }
                        else -> {}
                    }

                    contentAdapter?.setLoadingView()
                    contentAdapter?.setData(getLegendContentList())
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setRecyclerView() {
        binding.legendContentRV.apply {
            val gridLayoutManager = GridLayoutManager(this.context, gridCount)

            gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemViewType = contentAdapter?.getItemViewType(position)
                    return if (
                        itemViewType == RecyclerViewEnum.View.value ||
                        itemViewType == RecyclerViewEnum.Loading.value
                    ) 1 else gridCount
                }
            }

            layoutManager = gridLayoutManager
            contentAdapter = ContentAdapter(
                gridCount = gridCount,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                interaction = object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        when(contentType) {
                            Constants.ContentType.ANIME -> TODO()
                            Constants.ContentType.MOVIE -> {
                                val navWithAction = ProfileFragmentDirections.actionNavigationProfileToMovieDetailsFragment(item.id)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.TV -> {
                                val navWithAction = ProfileFragmentDirections.actionNavigationProfileToTvDetailsFragment(item.id)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.GAME -> TODO()
                        }
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.getUserInfo()
                    }

                    override fun onCancelPressed() {}

                    override fun onExhaustButtonPressed() {}

                }
            )
            adapter = contentAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        contentAdapter = null
        viewLifecycleOwner.apply {
            sharedViewModel.networkStatus.removeObservers(this)
            sharedViewModel.windowSize.removeObservers(this)
            viewModel.userInfoResponse.removeObservers(this)
        }

        super.onDestroyView()
    }
}